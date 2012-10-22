package pmsoft.sam.protocol.injection.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.UUID;

import pmsoft.sam.protocol.execution.CanonicalProtocolExecutionServiceClientApi;
import pmsoft.sam.protocol.execution.CanonicalProtocolRequestData;
import pmsoft.sam.protocol.execution.CanonicalProtocolRequest;
import pmsoft.sam.protocol.execution.model.AbstractInstanceReference;
import pmsoft.sam.protocol.execution.model.InstanceMergeVisitor;
import pmsoft.sam.protocol.execution.model.MethodCall;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.inject.Key;

public class ExecutionMethodRecordContext extends AbstractMethodRecordContext {
	private final InstanceRegistry executionInstanceRegistry;
	private final UUID canonicalTransactionIdentificator;

	public ExecutionMethodRecordContext(CanonicalProtocolExecutionServiceClientApi executionService, InstanceRegistry executionInstanceRegistry,
			UUID canonicalTransactionIdentificator) {
		super(executionService);
		this.executionInstanceRegistry = executionInstanceRegistry;
		this.canonicalTransactionIdentificator = canonicalTransactionIdentificator;
		executionInstanceRegistry.setMethodRecordContext(this);
	}

	public int createExternalInstanceBinding(int targetSlot, CanonicalInstanceRecorder<?> externalSlotRecordReference) {
		checkState(false, "Imposible to create ExternalInstance reference in execution context");
		return -1;
	}

	@Override
	protected List<AbstractInstanceReference> getInstanceReferenceToTransfer(int targetSlot) {
		checkArgument(targetSlot == 0);
		Builder<AbstractInstanceReference> executionInstances = ImmutableList.builder();
		executionInstances.addAll(executionInstanceRegistry.getInstanceReferenceToTransfer());
		return executionInstances.build();
	}

	@Override
	protected Object getMethodCallReturnObject(MethodCall call) {
		int serviceSlot = call.getServiceSlotNr();
		checkState(serviceSlot == 0, "Return objects exists only on the execution instance registry");
		return executionInstanceRegistry.getDataInstance(call.getReturnInstanceId());
	}

	private CanonicalProtocolRequest currentRequestOnStack = null;

	@Override
	public void flushExecutionStack() {
		checkState(currentRequestOnStack != null);
		CanonicalProtocolRequestData requestData = createStackRequest();
		if (requestData == null) {
			// NO external calls recorded
			return;
		}
		int targetSlot = requestData.getTargetSlot();
		checkState(targetSlot == 0);
		CanonicalProtocolRequest request = new CanonicalProtocolRequest(false, canonicalTransactionIdentificator, currentRequestOnStack.getSourceLocation(),
				currentRequestOnStack.getTargetLocation(), requestData, currentRequestOnStack.getServiceSlotNr());
		CanonicalProtocolRequestData response = executionService.executeExternalCanonicalRequest(request);
		List<AbstractInstanceReference> returnInstanceReferences = response.getInstanceReferences();
		mergeInstanceReferenceData(returnInstanceReferences);
		List<MethodCall> returnCalls = response.getMethodCalls();
		serviceCallStack.addAll(returnCalls);
	}

	private void mergeInstanceReferenceData(List<AbstractInstanceReference> returnInstanceReferences) {
		InstanceMergeVisitor executionMergeVisitor = executionInstanceRegistry.getMergeVisitor();
		for (AbstractInstanceReference abstractInstanceReference : returnInstanceReferences) {
			abstractInstanceReference.visitToMergeOnInstanceRegistry(executionMergeVisitor);
		}
	}

	@Override
	public void pushMethodCall(MethodCall call) {
		checkState(call.getServiceSlotNr() == 0,
				"On execution context slot 0 is the unique, how it is possible to get a method call to other slot??, critical error");
		super.pushMethodCall(call);
	}

	public CanonicalProtocolRequestData handleRequest(CanonicalProtocolRequest request) {
		CanonicalProtocolRequest requestStackCache = this.currentRequestOnStack;
		this.currentRequestOnStack = request;
		// Slot 0 have the real service instance mapping
		CanonicalProtocolRequestData data = request.getData();
		mergeInstanceReferenceData(data.getInstanceReferences());
		executeCalls(data.getMethodCalls(), executionInstanceRegistry);
		flushExecution();
		CanonicalProtocolRequestData responseData = createResponseRequest(0);
		this.currentRequestOnStack = requestStackCache;
		return responseData;
	}

	@Override
	protected int[] parseArguments(int serviceSlotNrContext, Object[] args) {
		// TODO this must not be used
		checkState(false, "TODO");
		return null;
	}

	@Override
	protected <S> CanonicalInstanceRecorder<S> createRecordInstance(int serviceSlotNr, Key<S> key) {
		Preconditions.checkArgument(serviceSlotNr == 0, "Execution context have only one slot");
		return executionInstanceRegistry.createKeyBinding(key);
	}

	@Override
	protected <S> int createPendingDataBinding(int serviceSlotNr, Class<S> returnType) {
		Preconditions.checkArgument(serviceSlotNr == 0, "Execution context have only one slot");
		return executionInstanceRegistry.createPendingDataBinding(returnType);
	}
}
