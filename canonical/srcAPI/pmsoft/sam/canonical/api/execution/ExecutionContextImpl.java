package pmsoft.sam.canonical.api.execution;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.UUID;

import pmsoft.sam.canonical.CanonicalInstanceRecorder;
import pmsoft.sam.canonical.api.ExecutionContextManager;
import pmsoft.sam.canonical.api.common.AbstractMethodRecordContext;
import pmsoft.sam.canonical.api.ir.InstanceMergeVisitor;
import pmsoft.sam.canonical.api.ir.InstanceRegistry;
import pmsoft.sam.canonical.api.ir.ServerExecutionInstanceRegistry;
import pmsoft.sam.canonical.api.ir.model.AbstractInstanceReference;
import pmsoft.sam.canonical.model.CanonicalProtocolRequestData;
import pmsoft.sam.canonical.model.MethodCall;
import pmsoft.sam.canonical.model.ServiceExecutionRequest;
import pmsoft.sam.canonical.service.CanonicalProtocolExecutionService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.inject.Injector;

public class ExecutionContextImpl extends AbstractMethodRecordContext implements ExecutionContextManager {

	private final InstanceRegistry executionInstanceRegistry;
	private final UUID canonicalTransactionIdentificator;

	public ExecutionContextImpl(CanonicalProtocolExecutionService executionService, UUID canonicalTransactionIdentificator,
			Injector realServiceInjector) {
		super(executionService);
		this.canonicalTransactionIdentificator = canonicalTransactionIdentificator;

		SlotExecutionContext slotContext = new SlotExecutionContext(this);

		this.executionInstanceRegistry = new ServerExecutionInstanceRegistry(slotContext, realServiceInjector);

		slotContext.setExecutionInstanceRegistry(executionInstanceRegistry);
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

	private ServiceExecutionRequest currentRequestOnStack = null;

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
		ServiceExecutionRequest request = new ServiceExecutionRequest(false, canonicalTransactionIdentificator,
				currentRequestOnStack.getSourceURL(), currentRequestOnStack.getTargetURL(), requestData,
				currentRequestOnStack.getServiceSlotNr());
		CanonicalProtocolRequestData response = executionService.executeCanonicalCalls(request);
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

	public CanonicalProtocolRequestData handleRequest(ServiceExecutionRequest request) {
		ServiceExecutionRequest requestStackCache = this.currentRequestOnStack;
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

}
