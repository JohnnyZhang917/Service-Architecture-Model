package eu.pmsoft.sam.see.api.transaction;

import eu.pmsoft.sam.architecture.model.ServiceKey;
import eu.pmsoft.sam.see.injectionUtils.ServiceKeyOrder;

public abstract class BindPoint implements Comparable<BindPoint>, SamTransactionModelVisitable {

    private final ServiceKey contract;

    BindPoint(ServiceKey contract) {
        this.contract = contract;
    }

    @Override
    public int compareTo(BindPoint that) {
        return ServiceKeyOrder.serviceKeyComparator.compare(this.contract, that.contract);
    }

    public ServiceKey getContract() {
        return contract;
    }

}