package pmsoft.sam.canonical.api.common;

import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Deque;
import java.util.List;

import pmsoft.sam.canonical.api.CanonicalProtocolRequestHandler;
import pmsoft.sam.canonical.api.ir.InstanceRegistry;
import pmsoft.sam.canonical.api.ir.model.AbstractInstanceReference;
import pmsoft.sam.canonical.model.CanonicalProtocolRequestData;
import pmsoft.sam.canonical.model.MethodCall;
import pmsoft.sam.canonical.service.CanonicalProtocolExecutionService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;

public abstract class AbstractMethodRecordContext implements CanonicalProtocolRequestHandler {

	protected final Deque<MethodCall> serviceCallStack = Lists.newLinkedList();

	protected final CanonicalProtocolExecutionService executionService;

	public AbstractMethodRecordContext(CanonicalProtocolExecutionService executionService) {
		super();
		this.executionService = executionService;
	}

	public void pushMethodCall(MethodCall call) {
		serviceCallStack.addLast(call);
	}

	public final Object flushMethodCall(MethodCall call) {
		pushMethodCall(call);
		flushExecution();
		return getMethodCallReturnObject(call);
	}

	protected abstract Object getMethodCallReturnObject(MethodCall call);

	public abstract void flushExecutionStack();

	public void flushExecution() {
		while (!serviceCallStack.isEmpty()) {
			flushExecutionStack();
		}
	}

	protected abstract List<AbstractInstanceReference> getInstanceReferenceToTransfer(int targetSlot);

	protected final CanonicalProtocolRequestData createStackRequest() {
		if (serviceCallStack.isEmpty()) {
			return null;
		}
		List<MethodCall> methodCalls = getAccesibleCalls();
		checkState(methodCalls.size() > 0,
				"the serviceCallStack is not empty and list of method calls is empty???, critical error");
		int targetSlot = methodCalls.get(0).getServiceSlotNr();
		List<AbstractInstanceReference> instanceReference = getInstanceReferenceToTransfer(targetSlot);
		CanonicalProtocolRequestData data = new CanonicalProtocolRequestData(instanceReference, methodCalls, targetSlot);
		return data;
	}

	private List<MethodCall> getAccesibleCalls() {
		Builder<MethodCall> methodBuilder = ImmutableList.builder();
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

	protected final CanonicalProtocolRequestData createResponseRequest(int targetSlot) {
		Builder<MethodCall> methodBuilder = ImmutableList.builder();
		if (!serviceCallStack.isEmpty()) {
			MethodCall firstTarget = serviceCallStack.peek();
			int firstCallSlot = firstTarget.getServiceSlotNr();
			if (firstCallSlot == targetSlot) {
				while (true) {
					MethodCall targetCall = serviceCallStack.peek();
					if (targetCall == null || targetCall.getServiceSlotNr() != firstCallSlot) {
						break;
					}
					methodBuilder.add(serviceCallStack.poll());
				}
			}
		}
		ImmutableList<MethodCall> calls = methodBuilder.build();
		List<AbstractInstanceReference> instanceReference = getInstanceReferenceToTransfer(targetSlot);
		CanonicalProtocolRequestData data = new CanonicalProtocolRequestData(instanceReference, calls, targetSlot);
		return data;
	}

	protected final void executeCalls(List<MethodCall> serviceCalls, InstanceRegistry instanceRegistry) {
		for (MethodCall methodCall : serviceCalls) {
			int calledInstance = methodCall.getInstanceNr();
			int[] arguments = methodCall.getArgumentReferences();
			int returnInstance = methodCall.getReturnInstanceId();

			Object headObject = instanceRegistry.getInstance(calledInstance);
			Object[] argumentsObjects = null;
			if (arguments != null && arguments.length > 0) {
				argumentsObjects = instanceRegistry.getArguments(arguments);
			}

			// FIXME: Not the best code to find methods, just for prototype.
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
				if (argumentsObjects == null || argumentsObjects.length == 0) {
					returnObject = targetMethod.invoke(headObject);
				} else {
					returnObject = targetMethod.invoke(headObject, argumentsObjects);
				}
			} catch (IllegalArgumentException e) {
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

}
