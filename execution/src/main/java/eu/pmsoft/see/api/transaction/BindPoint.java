package eu.pmsoft.see.api.transaction;

import eu.pmsoft.sam.architecture.model.ServiceKeyDeprecated;

public abstract class BindPoint implements Comparable<BindPoint>, SamTransactionModelVisitable {

    private final ServiceKeyDeprecated contract;

    BindPoint(ServiceKeyDeprecated contract) {
        this.contract = contract;
    }

    @Override
    public int compareTo(BindPoint that) {
        return ServiceKeyDeprecated.serviceKeyComparator.compare(this.contract, that.contract);
    }

    public ServiceKeyDeprecated getContract() {
        return contract;
    }

}
