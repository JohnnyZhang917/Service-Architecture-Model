package eu.pmsoft.sam.impl.user.one;

import javax.inject.Inject;

import eu.pmsoft.sam.service.core.CoreServiceExample;
import eu.pmsoft.sam.service.user.UserDataComplexAPI;
import eu.pmsoft.sam.service.user.UserDataSimpleAPI;

public class ImplementationOneSimple implements UserDataSimpleAPI {

	private Integer internalCounter = 0;
	
	@Inject
	private CoreServiceExample coreService;

	public boolean simpleMethod() {
		coreService.resetProcess();
		for (int i = 0; i < internalCounter; i++) {
			coreService.putData(i);
		}
		return coreService.isProcessStatusOk();
	}

	public UserDataComplexAPI complexInteractionAPI(Integer basicType) {
		internalCounter += basicType;
		return new ImplementationOneComplex(internalCounter);
	}
}
