package pmsoft.sam.protocol.injection.internal;

import java.util.UUID;

import pmsoft.sam.protocol.execution.CanonicalProtocolExecutionServiceClientApi;
import pmsoft.sam.protocol.execution.CanonicalProtocolRequestData;
import pmsoft.sam.protocol.execution.CanonicalProtocolRequest;
import pmsoft.sam.protocol.injection.CanonicalProtocolExecutionContext;
import pmsoft.sam.protocol.injection.TransactionController;
import pmsoft.sam.see.api.model.SIURL;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;

class CanonicalProtocolExecutionContextObject implements CanonicalProtocolExecutionContext {

	private final MethodRecordContext recordStack;
	private final MethodRecordContext executionStack;
	private final UUID canonicalTransactionIdentificator;
	private final TransactionController controller;
	private final Injector headTransactionInjector;

	public CanonicalProtocolExecutionContextObject(SIURL transactionURL, ImmutableList<SIURL> serviceSlotURL, ImmutableList<InstanceRegistry> serviceSlots,
			CanonicalProtocolExecutionServiceClientApi executionService, InstanceRegistry executionInstanceRegistry, TransactionController controller, Injector headTransactionInjector, UUID canonicalTransactionIdentificator) {
		this.controller = controller;
		this.canonicalTransactionIdentificator = canonicalTransactionIdentificator;
		this.recordStack = new RecordMethodRecordContext(executionService, serviceSlots, serviceSlotURL, canonicalTransactionIdentificator, transactionURL);
		this.executionStack = new ExecutionMethodRecordContext(executionService, executionInstanceRegistry, canonicalTransactionIdentificator);
		this.headTransactionInjector = headTransactionInjector;
	}
	
	@Override
	public CanonicalProtocolRequestData handleRequest(CanonicalProtocolRequest request) {
		if(request.isForwardCall()) {
			return executionStack.handleRequest(request);
		} else {
			return recordStack.handleRequest(request);
		}
	}

	@Override
	public UUID getContextUniqueID() {
		return canonicalTransactionIdentificator;
	}

	@Override
	public TransactionController getTransactionController() {
		return controller;
	}

	@Override
	public Injector getInjector() {
		return headTransactionInjector;
	}

}
