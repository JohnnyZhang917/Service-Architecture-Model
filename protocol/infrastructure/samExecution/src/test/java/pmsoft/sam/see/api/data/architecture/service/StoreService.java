package pmsoft.sam.see.api.data.architecture.service;

import pmsoft.sam.definition.service.AbstractSamServiceDefinition;
import pmsoft.sam.see.api.data.architecture.contract.store.StoreServiceContract;

public class StoreService extends AbstractSamServiceDefinition {

	@Override
	public void loadServiceDefinition() {
		addInterface(StoreServiceContract.class);
	}

}
