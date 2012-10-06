package pmsoft.sam.protocol.injection;

import java.util.Map.Entry;

import pmsoft.sam.protocol.freebinding.ExternalBindingController;
import pmsoft.sam.protocol.freebinding.ExternalInstanceProvider;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;

/**
 * Realize the switching of ExternalInstanceProvider for all controllers ExternalBindingController on a given transaction.
 * 
 * Thread save logic must be handled by the Canonical Protocol execution process.
 * 
 * @author pawel
 *
 */
public class TransactionControllerImpl implements TransactionController {

	private final ImmutableListMultimap<ExternalInstanceProvider, ExternalBindingController> transactionInstanceMapping;
	
	public TransactionControllerImpl(Multimap<ExternalInstanceProvider, ExternalBindingController> controlConfiguration) {
		transactionInstanceMapping = ImmutableListMultimap.copyOf(controlConfiguration);
	}

	@Override
	public void enterTransactionContext() {
		for (Entry<ExternalInstanceProvider, ExternalBindingController> controller : transactionInstanceMapping.entries()) {
			controller.getValue().bindRecordContext(controller.getKey());
		}
	}

	@Override
	public void exitTransactionContext() {
		for (ExternalBindingController controller : transactionInstanceMapping.values()) {
			controller.unBindRecordContext();
		}
	}

}
