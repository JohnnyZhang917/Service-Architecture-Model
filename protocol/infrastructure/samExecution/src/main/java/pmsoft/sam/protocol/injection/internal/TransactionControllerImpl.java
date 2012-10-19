package pmsoft.sam.protocol.injection.internal;

import java.util.Map.Entry;

import pmsoft.sam.protocol.freebinding.ExternalBindingController;
import pmsoft.sam.protocol.freebinding.ExternalInstanceProvider;
import pmsoft.sam.protocol.injection.TransactionController;

import com.google.common.collect.ImmutableMap;

/**
 * Realize the switching of ExternalInstanceProvider for all controllers ExternalBindingController on a given transaction.
 * 
 * Thread save logic must be handled by the Canonical Protocol execution process.
 * 
 * @author pawel
 *
 */
public class TransactionControllerImpl implements TransactionController {

	private final ImmutableMap<ExternalBindingController, ExternalInstanceProvider> instanceProvidersOnTransaction;
	
	
	public TransactionControllerImpl(ImmutableMap<ExternalBindingController, ExternalInstanceProvider> instanceProvidersOnTransaction) {
		this.instanceProvidersOnTransaction = instanceProvidersOnTransaction;
	}

	@Override
	public void enterTransactionContext() {
		for (Entry<ExternalBindingController, ExternalInstanceProvider> controlScope : instanceProvidersOnTransaction.entrySet()) {
			ExternalBindingController controller = controlScope.getKey();
			ExternalInstanceProvider instanceProvider = controlScope.getValue();
			controller.bindRecordContext(instanceProvider);
		}
	}

	@Override
	public void exitTransactionContext() {
		for (ExternalBindingController controller : instanceProvidersOnTransaction.keySet()) {
			controller.unBindRecordContext();
		}
	}

}
