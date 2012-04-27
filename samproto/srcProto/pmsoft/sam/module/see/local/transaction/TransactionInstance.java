package pmsoft.sam.module.see.local.transaction;

import java.net.URL;
import java.util.Map;
import java.util.UUID;

import pmsoft.sam.canonical.api.CanonicalProtocolRequestHandler;
import pmsoft.sam.canonical.api.execution.ExecutionContextImpl;
import pmsoft.sam.canonical.model.CanonicalProtocolRequestData;
import pmsoft.sam.canonical.model.ServiceExecutionRequest;
import pmsoft.sam.canonical.service.CanonicalProtocolExecutionService;
import pmsoft.sam.inject.wrapper.WrappingInjectorController;
import pmsoft.sam.module.model.ExternalServiceReference;
import pmsoft.sam.module.model.SamArchitecture;
import pmsoft.sam.module.see.local.ServiceExecutionEnviromentInternalAPI;
import pmsoft.sam.module.see.transaction.SamTransaction;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;

public class TransactionInstance implements SamTransaction, ExternalServiceReference {
	private final Injector injector;
	private final URL transactionURL;
	private final CanonicalProtocolExecutionService executor;

	@Inject
	public TransactionInstance(@Assisted InjectionConfigurationInternal rootConfiguration, SamArchitecture architecture,
			ServiceExecutionEnviromentInternalAPI seeInternalApi, CanonicalProtocolExecutionService canonicalExecutionService,
			CanonicalProtocolExecutionService executor) {
		this.transactionURL = seeInternalApi.generateTransactionURL(this);
		this.injector = rootConfiguration.createTransactionInjector(seeInternalApi, architecture, canonicalExecutionService,
				transactionURL);
		this.executor = executor;
	}

	public Injector getInjector() {
		return injector;
	}

	public URL getTransactionURL() {
		return transactionURL;
	}

	Map<UUID, CanonicalProtocolRequestHandler> activeClients = Maps.newHashMap();

	public CanonicalProtocolRequestData handleRequest(ServiceExecutionRequest request) {
		if (!request.isForwardCall()) {
			return handleNestedExternalInstanceCalls(request);
		}

		UUID transactionUID = request.getCanonicalTransactionIdentificator();
		CanonicalProtocolRequestHandler context = activeClients.get(transactionUID);
		if (context == null) {
			context = createNewContext(transactionUID);
			activeClients.put(transactionUID, context);
		}
		return context.handleRequest(request);
	}

	private CanonicalProtocolRequestData handleNestedExternalInstanceCalls(ServiceExecutionRequest request) {
		WrappingInjectorController translactionController = injector.getInstance(WrappingInjectorController.class);
		try {
			translactionController.bindExecutionContext();
			CanonicalProtocolRequestHandler transactionRequestHandler = translactionController.getRequestHandler();
			return transactionRequestHandler.handleRequest(request);
		} finally {
			translactionController.unbindExecutionContext();
		}
	}

	private CanonicalProtocolRequestHandler createNewContext(UUID transactionUID) {
		ExecutionContextImpl context = new ExecutionContextImpl(executor, transactionUID, injector);
		return context;
	}
}
