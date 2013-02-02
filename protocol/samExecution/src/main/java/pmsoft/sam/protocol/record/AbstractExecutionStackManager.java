package pmsoft.sam.protocol.record;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import pmsoft.execution.ThreadMessage;
import pmsoft.execution.ThreadMessagePipe;
import pmsoft.sam.protocol.transport.data.AbstractInstanceReference;
import pmsoft.sam.protocol.transport.data.CanonicalProtocolRequestData;
import pmsoft.sam.protocol.transport.data.InstanceMergeVisitor;
import pmsoft.sam.protocol.transport.data.MethodCall;
import pmsoft.sam.see.api.model.ExecutionStrategy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Deque;
import java.util.List;
import java.util.Stack;

abstract class AbstractExecutionStackManager {

    protected final ImmutableList<InstanceRegistry> instanceRegistries;
    protected final Logger logger;
    protected ImmutableList<ThreadMessagePipe> pipes;
    private final StackOfStack stack;
    private final ExecutionStrategy executionStrategy;

    AbstractExecutionStackManager(ImmutableList<InstanceRegistry> instanceRegistries, Logger logger, ExecutionStrategy executionStrategy) {
        this.instanceRegistries = instanceRegistries;
        this.executionStrategy = executionStrategy;
        this.stack = new StackOfStack();
        this.logger = logger;
    }

    void bindTransaction() {
        assert stack.threadExecutionStack.empty();
        stack.bindStack(-1);
    }

    void unbindTransaction() {
        assert stack.threadExecutionStack.size() == 1;
        stack.unbindStack();
    }

    public void bindExecutionPipes(ImmutableList<ThreadMessagePipe> pipes) {
        Preconditions.checkNotNull(pipes);
        Preconditions.checkState(instanceRegistries.size() == pipes.size());
        this.pipes = pipes;
    }

    public void unbindPipe() {
        Preconditions.checkNotNull(pipes);
        // after this call, transaction is closed and pipes are closed. Maybe deleted in future is not necessary at all
        // setting pipes to null just to get errors if it is used
        pipes = null;
    }

    private class StackOfStack {
        private final Stack<MethodExecutionStack> threadExecutionStack;

        private StackOfStack() {
            this.threadExecutionStack = new Stack<MethodExecutionStack>();
        }

        void bindStack(int mainTargetSlot) {
            logger.trace("Stack Execution PUSH for slot {}", mainTargetSlot);
            threadExecutionStack.push(new MethodExecutionStack(mainTargetSlot));
        }

        void unbindStack() {
            logger.trace("Stack Execution POP");
            MethodExecutionStack executionStack = threadExecutionStack.pop();
            assert executionStack != null;
            assert executionStack.executionMark == executionStack.recordMark;
            assert executionStack.serviceCallStack.isEmpty();
        }

        MethodExecutionStack getCurrentStack() {
            logger.trace("executionStack level {}", threadExecutionStack.size());
            return threadExecutionStack.peek();
        }

    }

    public abstract void executeCanonicalProtocol();

    public void pushMethodCall(MethodCall call) {
        MethodExecutionStack currentStack = stack.getCurrentStack();
        logger.debug("recording call {}", call);
        boolean slotChange = currentStack.addCall(call);
        switch (executionStrategy) {
            case PROCEDURAL:
//                pushProtocolExecution(false, currentStack);
                flushExecution();
                break;
            case SIMPLE_LAZY:
                if (slotChange) {
                    logger.debug("slotChange");
                    pushProtocolExecution(false, currentStack);
                }
                break;
        }
    }

    void forceResolveCall(MethodCall call) {
        pushMethodCall(call);
        flushExecution();
    }

    protected void flushExecution() {
        MethodExecutionStack executionStackCall = stack.getCurrentStack();
        int expectedExecutionMark = executionStackCall.recordMark;
        while (executionStackCall.executionMark < expectedExecutionMark) {
            logger.trace("force execution. CurrentMark/Expected [{},{}]", executionStackCall.executionMark, expectedExecutionMark);
            pushProtocolExecution(true, executionStackCall);
        }
        logger.trace("flush execution done");
    }


    protected void pushProtocolExecution(boolean forceWait, MethodExecutionStack currentStack) {
        if (currentStack.waitingStatus) {
            ThreadMessagePipe threadMessagePipe = getPipe(currentStack.waitingSlotNr);
            ThreadMessage message;
            if (forceWait) {
                logger.trace("forcing wait response on execution");
                message = threadMessagePipe.waitResponse();
            } else {
                logger.trace("trying poll");
                message = threadMessagePipe.pollMessage();
            }
            if (message != null) {
                logger.trace("handling message back");
                CanonicalProtocolRequestData payload = (CanonicalProtocolRequestData) message.getPayload();
                if (payload == null) {
                    logger.trace("returning thread execution");
                    currentStack.markDone();
                } else {
                    if (payload.isCloseThread()) {
                        logger.trace("handle returning message call - merge only for clossing execution thread");
                        InstanceRegistry executionRegistry = instanceRegistries.get(currentStack.waitingSlotNr);
                        mergeInstances(executionRegistry, payload.getInstanceReferences());
                        currentStack.markDone();
                    } else {
                        logger.trace("handle returning message call");

                        InstanceRegistry executionRegistry = instanceRegistries.get(currentStack.waitingSlotNr);
                        mergeInstances(executionRegistry, payload.getInstanceReferences());
                        // thread line binds to the global thread stack line
                        if (payload.getMethodCalls() != null) {
                            stack.bindStack(currentStack.waitingSlotNr);
                            executeCalls(payload.getMethodCalls(), executionRegistry);
                            stack.unbindStack();
                        }
                        // thread line returns to external slot
                        CanonicalProtocolRequestData returnMessage = new CanonicalProtocolRequestData(executionRegistry.getInstanceReferenceToTransfer(), null, true);
                        ThreadMessage closeMsg = new ThreadMessage();
                        closeMsg.setPayload(returnMessage);
                        threadMessagePipe.sendMessage(closeMsg);
                    }
                }
            } else {
                logger.trace("no message, continue execution");
            }
        } else {
            // send next request
            CanonicalProtocolRequestData stackRequest = currentStack.createStackRequest();
            logger.trace("sending request to slot [{}]", currentStack.waitingSlotNr);

            ThreadMessagePipe threadMessagePipe = getPipe(currentStack.waitingSlotNr);
            ThreadMessage message = new ThreadMessage();
            message.setPayload(stackRequest);
            threadMessagePipe.sendMessage(message);
            assert currentStack.waitingStatus == true;
        }
    }

    private ThreadMessagePipe getPipe(int targetSlotNr) {
        return pipes.get(targetSlotNr);
    }

    protected void mergeInstances(InstanceRegistry registry, List<AbstractInstanceReference> instanceReferences) {
        InstanceMergeVisitor executionMergeVisitor = registry.getMergeVisitor();
        for (AbstractInstanceReference abstractInstanceReference : instanceReferences) {
            abstractInstanceReference.visitToMergeOnInstanceRegistry(executionMergeVisitor);
        }
    }

    protected final void executeCalls(List<MethodCall> serviceCalls, InstanceRegistry instanceRegistry) {
        for (MethodCall methodCall : serviceCalls) {
            int calledInstance = methodCall.getInstanceNr();
            int[] arguments = methodCall.getArgumentReferences();
            int returnInstance = methodCall.getReturnInstanceId();

            Object headObject = instanceRegistry.getInstanceObject(calledInstance);
            Object[] argumentsObjects = null;
            if (arguments != null && arguments.length > 0) {
                argumentsObjects = instanceRegistry.getArguments(arguments);
            }

            // TODO: Not the best code to find methods, change to binary compiled architecture definition.
            Method targetMethod = null;
            Class<?>[] inter = headObject.getClass().getInterfaces();
            for (int i = 0; i < inter.length; i++) {
                Class<?> targetServiceInterface = inter[i];
                Method[] methods = targetServiceInterface.getDeclaredMethods();
                for (int m = 0; m < methods.length; m++) {
                    Method candidate = methods[m];
                    if (methodCall.getMethodSignature() == candidate.hashCode()) {
                        targetMethod = candidate;
                        break;
                    }
                }
            }
            if (targetMethod == null) {
                throw new IllegalStateException("cand find method on service interfaces");
            }

            Object returnObject;
            try {
                logger.trace("execution method call {}", methodCall);
                if (argumentsObjects == null || argumentsObjects.length == 0) {
                    returnObject = targetMethod.invoke(headObject);
                } else {
                    returnObject = targetMethod.invoke(headObject, argumentsObjects);
                }
            } catch (IllegalArgumentException e) {
                // FIXME exception policy.
                // FIXME how to handle errors on invocations to real services??
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getTargetException());
            }
            if (returnInstance >= 0) {
                instanceRegistry.bindReturnObject(returnInstance, returnObject);
            }
        }

    }

    class MethodExecutionStack {
        private final Deque<MethodCall> serviceCallStack = Lists.newLinkedList();
        private final int slotReference;
        private boolean waitingStatus = false;
        private int waitingSlotNr;
        private int executionMark = 0;
        private int sendMark = 0;
        private int recordMark = 0;

        private MethodExecutionStack(int slotReference) {
            this.slotReference = slotReference;
        }

        void markDone() {
            logger.trace("stack status change. Send/Execution=[{},{}]", sendMark, executionMark);
            executionMark = sendMark;
            logger.trace("stack status change. Send/Execution=[{},{}]", sendMark, executionMark);
            waitingStatus = false;
        }

        private CanonicalProtocolRequestData createStackRequest() {
            assert !waitingStatus : "Trying to send request on waiting status";
            waitingStatus = true;
            if (serviceCallStack.isEmpty()) {
                return prepareMergeInstanceRequest();
            }
            List<MethodCall> methodCalls = getAccesibleCalls();
            assert methodCalls.size() > 0 : "the serviceCallStack is not empty and list of method calls is empty???, critical exceptions";
            sendMark += methodCalls.size();
            int targetSlot = methodCalls.get(0).getServiceSlotNr();
            List<AbstractInstanceReference> instanceReference = instanceRegistries.get(targetSlot).getInstanceReferenceToTransfer();
            CanonicalProtocolRequestData data = new CanonicalProtocolRequestData(instanceReference, methodCalls, false);
            waitingSlotNr = targetSlot;
            return data;
        }

        private CanonicalProtocolRequestData prepareMergeInstanceRequest() {
            List<AbstractInstanceReference> instanceReference = instanceRegistries.get(waitingSlotNr).getInstanceReferenceToTransfer();
            if (instanceReference == null || instanceReference.size() == 0) {
                return null;
            }
            CanonicalProtocolRequestData data = new CanonicalProtocolRequestData(instanceReference, null, false);
            return data;
        }

        private List<MethodCall> getAccesibleCalls() {
            ImmutableList.Builder<MethodCall> methodBuilder = ImmutableList.builder();
            MethodCall firstTarget = serviceCallStack.peek();
            int targetSlot = firstTarget.getServiceSlotNr();
            while (true) {
                MethodCall targetCall = serviceCallStack.peek();
                if (targetCall == null || targetCall.getServiceSlotNr() != targetSlot) {
                    break;
                }
                methodBuilder.add(serviceCallStack.poll());
            }
            return methodBuilder.build();
        }

        public boolean addCall(MethodCall call) {
            assert call != null;
            MethodCall last = serviceCallStack.peek();
            serviceCallStack.addLast(call);
            recordMark++;
            return last != null && last.getServiceSlotNr() != call.getServiceSlotNr();
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this).omitNullValues()
                    .add("serviceCallStack", Joiner.on("==").join(serviceCallStack))
                    .add("slotReference", slotReference)
                    .add("waitingStatus", waitingStatus)
                    .add("waitingSlotNr", waitingSlotNr)
                    .add("executionMark", executionMark)
                    .add("sendMark", sendMark)
                    .add("recordMark", recordMark)
                    .toString();
        }
    }
}
