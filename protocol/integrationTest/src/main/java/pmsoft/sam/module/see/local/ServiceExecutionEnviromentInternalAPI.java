package pmsoft.sam.module.see.local;

import java.net.URL;

import pmsoft.sam.module.see.ServiceExecutionEnviroment;
import pmsoft.sam.module.see.local.transaction.TransactionInstance;

public interface ServiceExecutionEnviromentInternalAPI extends ServiceExecutionEnviroment{

	URL generateTransactionURL(TransactionInstance transactionInstance);

}
