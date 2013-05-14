package eu.pmsoft.sam.protocol.record;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import eu.pmsoft.execution.ThreadMessage;
import eu.pmsoft.sam.protocol.transport.CanonicalInstanceReference;
import eu.pmsoft.sam.protocol.transport.CanonicalMethodCall;
import eu.pmsoft.sam.protocol.transport.CanonicalRequest;
import eu.pmsoft.sam.protocol.transport.data.CanonicalProtocolRequestData;
import eu.pmsoft.sam.protocol.transport.data.InstanceMergeVisitor;
import eu.pmsoft.see.api.model.ExecutionStrategy;
import eu.pmsoft.sam.see.transport.SamTransportChannel;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Deque;
import java.util.List;
import java.util.Stack;

abstract class AbstractExecutionStackManager {

    protected final ImmutableList<InstanceRegistry> instanceRegistries;
    protected final Logger logger;
    protected ImmutableList<SamTransportChannel> channels;
    private final StackOfStack stack;
    // TODO provide this parameter during execution
    private final ExecutionStrategy executionStrategy = ExecutionStrategy.SIMPLE_LAZY;

    AbstractExecutionStackManager(ImmutableList<InstanceRegistry> instanceRegistries, Logger logger) {
        this.instanceRegistries = instanceRegistries;
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

    public void bindExecutionPipes(ImmutableList<SamTransportChannel> channels) {
        Preconditions.checkNotNull(channels);
        Preconditions.checkState(instanceRegistries.size() == channels.size());
        this.channels = channels;
    }

    public void unbindPipe() {
        Preconditions.checkNotNull(channels);
        // after this call, transaction is closed and channels are closed. Maybe deleted in future is not necessary at all
        // setting channels to null just to get errors if it is used
        channels = null;
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

    public void pushMethodCall(CanonicalMethodCall call) {
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

    void forceResolveCall(CanonicalMethodCall call) {
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
            SamTransportChannel threadMessagePipe = getPipe(currentStack.waitingSlotNr);
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
                byte[] payloadData = message.getPayload();
                if (payloadData == null) {
                    logger.trace("returning thread execution");
                    currentStack.markDone();
                } else {
                    CanonicalProtocolRequestData payload = new CanonicalProtocolRequestData(payloadData);
                    if (payload.isCloseThread()) {
                        logger.trace("handleOperationException returning message call - merge only for clossing execution thread");
                        InstanceRegistry executionRegistry = instanceRegistries.get(currentStack.waitingSlotNr);
                        mergeInstances(executionRegistry, payload.getInstanceReferences());
                        currentStack.markDone();
                    } else {
                        logger.trace("handleOperationException returning message call");

                        InstanceRegistry executionRegistry = instanceRegistries.get(currentStack.waitingSlotNr);
                        mergeInstances(executionRegistry, payload.getInstanceReferences());
                        // thread line binds to the global thread stack line
                        if (payload.getMethodCalls() != null) {
                            stack.bindStack(currentStack.waitingSlotNr);
                            executeCalls(payload.getMethodCalls(), executionRegistry);
                            stack.unbindStack();
                        }
                        // thread line returns to external slot
                        CanonicalRequest data = new CanonicalRequest();
                        data.setCloseThread(true);
                        data.setInstancesList(executionRegistry.getInstanceReferenceToTransfer());
                        CanonicalProtocolRequestData returnMessage = new CanonicalProtocolRequestData(data);
                        ThreadMessage closeMsg = new ThreadMessage();
                        closeMsg.setPayload(returnMessage.getPayload());
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

            SamTransportChannel threadMessagePipe = getPipe(currentStack.waitingSlotNr);
            ThreadMessage message = new ThreadMessage();
            message.setPayload(stackRequest.getPayload());
            threadMessagePipe.sendMessage(message);
            assert currentStack.waitingStatus == true;
        }
    }

    private SamTransportChannel getPipe(int targetSlotNr) {
        assert channels != null;
        return channels.get(targetSlotNr);
    }

    protected void mergeInstances(InstanceRegistry registry, List<CanonicalInstanceReference> instanceReferences) {
        if (instanceReferences == null) return;
        InstanceMergeVisitor executionMergeVisitor = registry.getMergeVisitor();
        for (CanonicalInstanceReference abstractInstanceReference : instanceReferences) {
            executionMergeVisitor.merge(abstractInstanceReference);
        }
    }

    protected final void executeCalls(List<CanonicalMethodCall> serviceCalls, InstanceRegistry instanceRegistry) {
        for (CanonicalMethodCall methodCall : serviceCalls) {
            int calledInstance = methodCall.getInstanceNr();
            // TODO change to int[] on protostuff
            List<Integer> arguments = methodCall.getArgumentsReferenceList();
            int returnInstance = methodCall.getReturnInstanceId();

            Object headObject = instanceRegistry.getInstanceObject(calledInstance);
            Object[] argumentsObjects = null;
            if (arguments != null && arguments.size() > 0) {
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
                logger.debug("execution method call {}", methodCall);
                if (argumentsObjects == null || argumentsObjects.length == 0) {
                    returnObject = targetMethod.invoke(headObject);
                } else {
                    returnObject = targetMethod.invoke(headObject, argumentsObjects);
                }
            } catch (IllegalArgumentException e) {
                // FIXME exception policy.
                // FIXME how to handleOperationException errors on invocations to real services??
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
        private final Deque<CanonicalMethodCall> serviceCallStack = Lists.newLinkedList();
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
            List<CanonicalMethodCall> methodCalls = getAccesibleCalls();
            assert methodCalls.size() > 0 : "the serviceCallStack is not empty and list of method calls is empty???, critical exceptions";
            sendMark += methodCalls.size();
            int targetSlot = methodCalls.get(0).getSlotNr();
            List<CanonicalInstanceReference> instanceReference = instanceRegistries.get(targetSlot).getInstanceReferenceToTransfer();
            CanonicalRequest serialData = new CanonicalRequest();
            serialData.setInstancesList(instanceReference);
            serialData.setCallsList(methodCalls);
            serialData.setCloseThread(false);

            CanonicalProtocolRequestData data = new CanonicalProtocolRequestData(serialData);
            waitingSlotNr = targetSlot;
            return data;
        }

        private CanonicalProtocolRequestData prepareMergeInstanceRequest() {
            List<CanonicalInstanceReference> instanceReference = instanceRegistries.get(waitingSlotNr).getInstanceReferenceToTransfer();
            if (instanceReference == null || instanceReference.size() == 0) {
                return null;
            }
            CanonicalRequest serialData = new CanonicalRequest();
            serialData.setInstancesList(instanceReference);
            serialData.setCloseThread(false);
            CanonicalProtocolRequestData data = new CanonicalProtocolRequestData(serialData);
            return data;
        }

        private List<CanonicalMethodCall> getAccesibleCalls() {
            ImmutableList.Builder<CanonicalMethodCall> methodBuilder = ImmutableList.builder();
            CanonicalMethodCall firstTarget = serviceCallStack.peek();
            int targetSlot = firstTarget.getSlotNr();
            while (true) {
                CanonicalMethodCall targetCall = serviceCallStack.peek();
                if (targetCall == null || targetCall.getSlotNr() != targetSlot) {
                    break;
                }
                methodBuilder.add(serviceCallStack.poll());
            }
            return methodBuilder.build();
        }

        public boolean addCall(CanonicalMethodCall call) {
            assert call != null;
            CanonicalMethodCall last = serviceCallStack.peek();
            serviceCallStack.addLast(call);
            recordMark++;
            return last != null && last.getSlotNr() != call.getSlotNr();
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
