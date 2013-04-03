package eu.pmsoft.sam.protocol;


public interface TransactionController {

    public boolean enterTransactionContext();

    public void exitTransactionContext();

}
