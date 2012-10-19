package pmsoft.sam.protocol.injection.internal;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndex;
import static com.google.common.base.Preconditions.checkState;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;

import pmsoft.sam.protocol.execution.CanonicalProtocolExecutionService;
import pmsoft.sam.protocol.freebinding.ExternalBindingSwitch;
import pmsoft.sam.protocol.injection.internal.model.AbstractInstanceReference;
import pmsoft.sam.protocol.injection.internal.model.InstanceMergeVisitor;
import pmsoft.sam.see.api.model.SIURL;

import com.google.common.collect.ImmutableList;
import com.google.inject.Key;

class RecordMethodRecordContext extends AbstractMethodRecordContext {

	private final ImmutableList<InstanceRegistry> serviceSlots;
	private final ImmutableList<SIURL> serviceSlotURL;
	private final UUID canonicalTransactionIdentificator;
	private final SIURL transactionURL;

	public RecordMethodRecordContext(CanonicalProtocolExecutionService executionService, ImmutableList<InstanceRegistry> serviceSlots,
			ImmutableList<SIURL> serviceSlotURL, UUID canonicalTransactionIdentificator, SIURL transactionURL) {
		super(executionService);
		this.serviceSlots = serviceSlots;
		this.serviceSlotURL = serviceSlotURL;
		this.canonicalTransactionIdentificator = canonicalTransactionIdentificator;
		this.transactionURL = transactionURL;
		for (InstanceRegistry instanceRegistry : this.serviceSlots) {
			instanceRegistry.setMethodRecordContext(this);
		}
	}

	public int createExternalInstanceBinding(int targetSlot, CanonicalInstanceRecorder<?> externalSlotRecordReference) {
		checkPositionIndex(targetSlot, serviceSlots.size());
		InstanceRegistry targetInstanceRegistry = serviceSlots.get(targetSlot);
		int externalInstanceNr = targetInstanceRegistry.createExternalInstanceBinding(externalSlotRecordReference.getKey(),
				externalSlotRecordReference.getInstance());
		// Is it necessary to save more information about the mapping?? the slot
		// context have a reference to the record proxy to make any call so for
		// execution it should be enough
		return externalInstanceNr;
	}

	@Override
	protected Object getMethodCallReturnObject(MethodCall call) {
		int serviceSlotNr = call.getServiceSlotNr();
		InstanceRegistry slotInstanceRegistry = serviceSlots.get(serviceSlotNr);
		return slotInstanceRegistry.getDataInstance(call.getReturnInstanceId());
	}

	public void flushExecution() {
		while (!serviceCallStack.isEmpty()) {
			flushExecutionStack();
		}
	}

	protected List<AbstractInstanceReference> getInstanceReferenceToTransfer(int targetSlot) {
		InstanceRegistry slot = serviceSlots.get(targetSlot);
		return slot.getInstanceReferenceToTransfer();
	}

	public void flushExecutionStack() {
		CanonicalProtocolRequestData requestData = createStackRequest();
		if (requestData == null) {
			return;
		}
		int targetSlot = requestData.getTargetSlot();
		SIURL targetURL = serviceSlotURL.get(targetSlot);
		ServiceExecutionRequest request = new ServiceExecutionRequest(true, canonicalTransactionIdentificator, targetURL, transactionURL, requestData, targetSlot);
		CanonicalProtocolRequestData response = executionService.executeCanonicalCalls(request);
		List<AbstractInstanceReference> returnInstanceReferences = response.getInstanceReferences();
		InstanceRegistry targetServiceInstanceRegistry = serviceSlots.get(targetSlot);
		mergeInstanceReferenceData(returnInstanceReferences, targetServiceInstanceRegistry);
		List<MethodCall> returnCalls = response.getMethodCalls();
		serviceCallStack.addAll(returnCalls);
	}

	private void mergeInstanceReferenceData(List<AbstractInstanceReference> returnInstanceReferences, InstanceRegistry targetServiceInstanceRegistry) {
		InstanceMergeVisitor mergeVisitor = targetServiceInstanceRegistry.getMergeVisitor();
		for (AbstractInstanceReference abstractInstanceReference : returnInstanceReferences) {
			abstractInstanceReference.visitToMergeOnInstanceRegistry(mergeVisitor);
		}
	}

	public CanonicalProtocolRequestData handleRequest(ServiceExecutionRequest request) {
		SIURL callerServiceURL = request.getSourceURL();
		checkNotNull(callerServiceURL);
		int serviceSlotNr = request.getServiceSlotNr();
		checkPositionIndex(serviceSlotNr, serviceSlotURL.size());
		SIURL slotURL = serviceSlotURL.get(serviceSlotNr);
		checkState(slotURL.equals(callerServiceURL));

		InstanceRegistry slotInstanceRegistry = serviceSlots.get(serviceSlotNr);
		CanonicalProtocolRequestData data = request.getData();
		mergeInstanceReferenceData(data.getInstanceReferences(), slotInstanceRegistry);
		executeCalls(data.getMethodCalls(), slotInstanceRegistry);
		flushExecution();
		CanonicalProtocolRequestData responseData = createResponseRequest(serviceSlotNr);
		return responseData;
	}

	@Override
	protected int[] parseArguments(int serviceSlotNrContext, Object[] args) {
		int[] argumentNrs = new int[args.length];
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (arg == null) {
				argumentNrs[i] = 0;
			}
			// Extract the real object from switching proxy's
			if (arg instanceof Proxy) {
				Proxy parg = (Proxy) arg;
				InvocationHandler handler = Proxy.getInvocationHandler(parg);
				if (handler instanceof ExternalBindingSwitch) {
					ExternalBindingSwitch<?> switchProxy = (ExternalBindingSwitch<?>) handler;
					arg = switchProxy.getInternalInstance();
				}
			}

			if (arg instanceof Proxy) {
				Proxy parg = (Proxy) arg;
				InvocationHandler handler = Proxy.getInvocationHandler(parg);
				if (handler instanceof CanonicalInstanceRecorder) {
					CanonicalInstanceRecorder<?> recorder = (CanonicalInstanceRecorder<?>) handler;
					if (recorder.getServiceSlotNr() == serviceSlotNrContext) {
						argumentNrs[i] = recorder.getServiceInstanceNr();
					} else {
						argumentNrs[i] = createExternalInstanceBinding(serviceSlotNrContext, recorder);
					}
				} else {
					throw new RuntimeException("Unknow Proxy type");
				}
			} else if (arg instanceof Serializable) {
				argumentNrs[i] = serviceSlots.get(serviceSlotNrContext).createDataBinding(arg);
			} else {
				throw new RuntimeException("argument not serializable be canonical protocol");
			}
		}
		return argumentNrs;
	}

	@Override
	protected <S> int createPendingDataBinding(int serviceSlotNr, Class<S> returnType) {
		return serviceSlots.get(serviceSlotNr).createPendingDataBinding(returnType);
	}

	@Override
	protected <S> CanonicalInstanceRecorder<S> createRecordInstance(int serviceSlotNr, Key<S> key) {
		return serviceSlots.get(serviceSlotNr).createKeyBinding(key);
	}
}