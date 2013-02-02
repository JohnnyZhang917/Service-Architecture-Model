package pmsoft.sam.protocol.record;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.slf4j.Logger;
import pmsoft.injectionUtils.logger.InjectLogger;
import pmsoft.sam.protocol.TransactionController;
import pmsoft.sam.protocol.freebinding.ExternalBindingController;
import pmsoft.sam.protocol.freebinding.ExternalInstanceProvider;

import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Realize the switching of ExternalInstanceProvider for all controllers ExternalBindingController on a given transaction.
 * 
 * Thread save logic must be handled by the Canonical Protocol execution process.
 * 
 * @author pawel
 *
 */
class TransactionControllerImpl implements TransactionController {

	@InjectLogger private Logger logger;
	
	private final ImmutableMap<ExternalBindingController, ExternalInstanceProvider> instanceProvidersOnTransaction;
	private final AtomicBoolean contextSetup = new AtomicBoolean(false);

	@Inject
	public TransactionControllerImpl(@Assisted ImmutableMap<ExternalBindingController, ExternalInstanceProvider> instanceProvidersOnTransaction) {
		this.instanceProvidersOnTransaction = instanceProvidersOnTransaction;
	}
	
	@Override
	public boolean enterTransactionContext() {
		logger.trace("try to bind transaction context");
		if( contextSetup.compareAndSet(false, true) ) {
			logger.trace("transaction context bind to thread");
			for (Entry<ExternalBindingController, ExternalInstanceProvider> controlScope : instanceProvidersOnTransaction.entrySet()) {
				ExternalBindingController controller = controlScope.getKey();
				ExternalInstanceProvider instanceProvider = controlScope.getValue();
				controller.bindRecordContext(instanceProvider);
			}
			return true;
		}
		logger.trace("bind to transaction context cancelled");
		return false;
	}
	
	@Override
	public void exitTransactionContext() {
		logger.trace("try to unbind transaction context");
		if( contextSetup.compareAndSet(true, false) ) {
			for (ExternalBindingController controller : instanceProvidersOnTransaction.keySet()) {
				controller.unBindRecordContext();
			}
			logger.trace("unbind done");
		} else {
			logger.error("transaction context is already closed. FATAL ERROR");
			// FIXME exception policy.
			throw new RuntimeException("FATAL LOGIC ERROR");
		}
	}

}
