package pmsoft.sam.canonical.deprecated.api.recording;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndex;
import static com.google.common.base.Preconditions.checkState;

import java.net.URL;
import java.util.List;
import java.util.UUID;

import pmsoft.sam.canonical.deprecated.CanonicalInstanceRecorder;
import pmsoft.sam.canonical.deprecated.RecordingInstanceProvider;
import pmsoft.sam.canonical.deprecated.api.RecordContextManager;
import pmsoft.sam.canonical.deprecated.api.common.AbstractMethodRecordContext;
import pmsoft.sam.canonical.deprecated.api.ir.ClientRecordingInstanceRegistry;
import pmsoft.sam.canonical.deprecated.api.ir.InstanceMergeVisitor;
import pmsoft.sam.canonical.deprecated.api.ir.InstanceRegistry;
import pmsoft.sam.canonical.deprecated.api.ir.model.AbstractInstanceReference;
import pmsoft.sam.canonical.deprecated.model.CanonicalProtocolRequestData;
import pmsoft.sam.canonical.deprecated.model.MethodCall;
import pmsoft.sam.canonical.deprecated.model.ServiceExecutionRequest;
import pmsoft.sam.canonical.deprecated.service.CanonicalProtocolExecutionService;
import pmsoft.sam.protocol.injection.InstanceProvider;

import com.google.common.collect.Lists;

public class RecordingContextImpl extends AbstractMethodRecordContext implements RecordContextManager {

	private final List<InstanceRegistry> serviceSlots = Lists.newArrayList();
	private final List<URL> serviceSlotURL = Lists.newArrayList();
	private final UUID canonicalTransactionIdentificator;
	private final URL clientURL;

	public RecordingContextImpl(CanonicalProtocolExecutionService executionService, UUID canonicalTransactionIdentificator,
			URL clientURL) {
		super(executionService);
		this.canonicalTransactionIdentificator = canonicalTransactionIdentificator;
		this.clientURL = clientURL;
	}

	public int createExternalInstanceBinding(int targetSlot, CanonicalInstanceRecorder<?> externalSlotRecordReference) {
		checkPositionIndex(targetSlot, serviceSlots.size());
		InstanceRegistry targetInstanceRegistry = serviceSlots.get(targetSlot);
		int externalInstanceNr = targetInstanceRegistry.createExternalInstanceBinding(externalSlotRecordReference.getKey(),
				externalSlotRecordReference.getInstance());
		// Is it necessary to save more information about the mapping?? the slot
		// context have a reference to the record proxy to make any call so for execution it should be enough
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
		URL targetURL = serviceSlotURL.get(targetSlot);
		ServiceExecutionRequest request = new ServiceExecutionRequest(true, canonicalTransactionIdentificator, targetURL,
				clientURL, requestData,targetSlot);
		CanonicalProtocolRequestData response = executionService.executeCanonicalCalls(request);
		List<AbstractInstanceReference> returnInstanceReferences = response.getInstanceReferences();
		InstanceRegistry targetServiceInstanceRegistry = serviceSlots.get(targetSlot);
		mergeInstanceReferenceData(returnInstanceReferences, targetServiceInstanceRegistry);
		List<MethodCall> returnCalls = response.getMethodCalls();
		serviceCallStack.addAll(returnCalls);
	}

	private void mergeInstanceReferenceData(List<AbstractInstanceReference> returnInstanceReferences,
			InstanceRegistry targetServiceInstanceRegistry) {
		InstanceMergeVisitor mergeVisitor = targetServiceInstanceRegistry.getMergeVisitor();
		for (AbstractInstanceReference abstractInstanceReference : returnInstanceReferences) {
			abstractInstanceReference.visitToMergeOnInstanceRegistry(mergeVisitor);
		}
	}

	public CanonicalProtocolRequestData handleRequest(ServiceExecutionRequest request) {
		URL callerServiceURL = request.getSourceURL();
		checkNotNull(callerServiceURL);
		int serviceSlotNr = request.getServiceSlotNr();
		checkPositionIndex(serviceSlotNr, serviceSlotURL.size());
		URL slotURL = serviceSlotURL.get(serviceSlotNr);
		checkState(slotURL.equals(callerServiceURL));
		
		InstanceRegistry slotInstanceRegistry = serviceSlots.get(serviceSlotNr);
		CanonicalProtocolRequestData data = request.getData();
		mergeInstanceReferenceData(data.getInstanceReferences(),slotInstanceRegistry);
		executeCalls(data.getMethodCalls(), slotInstanceRegistry);
		flushExecution();
		CanonicalProtocolRequestData responseData = createResponseRequest(serviceSlotNr);
		return responseData;
	}

	public InstanceProvider registerExternalInstanceProvider(URL externalReference) {
		// It is possible to have many values of external injection
		// configuration bind to the same service.
		int slotNr = serviceSlotURL.size();
		serviceSlotURL.add(externalReference);
		SlotRecordingContext recordContext = new SlotRecordingContext(this, slotNr);
		InstanceRegistry slotInstanceRegistry = new ClientRecordingInstanceRegistry(recordContext, slotNr);
		recordContext.setInstanceRegistry(slotInstanceRegistry);
		serviceSlots.add(slotInstanceRegistry);
		return new RecordingInstanceProvider(slotInstanceRegistry);
	}

}
