package pmsoft.sam.see.api.transaction;

import pmsoft.sam.architecture.model.ServiceKey;

public abstract class BindPoint implements Comparable<BindPoint>, SamTransactionModelVisitable {

	private final ServiceKey contract;

	BindPoint(ServiceKey contract) {
		this.contract = contract;
	}

	@Override
	public int compareTo(BindPoint that) {
		return this.contract.getServiceDefinitionSignature().compareTo(that.contract.getServiceDefinitionSignature());
	}

	public ServiceKey getContract() {
		return contract;
	}
	
}
