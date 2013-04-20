package eu.pmsoft.sam.see.api.transaction;

import eu.pmsoft.sam.architecture.model.ServiceKey;

public abstract class BindPoint implements Comparable<BindPoint>, SamTransactionModelVisitable {

    private final ServiceKey contract;

    BindPoint(ServiceKey contract) {
        this.contract = contract;
    }

    @Override
    public int compareTo(BindPoint that) {
        return ServiceKey.serviceKeyComparator.compare(this.contract, that.contract);
    }

    public ServiceKey getContract() {
        return contract;
    }

}
