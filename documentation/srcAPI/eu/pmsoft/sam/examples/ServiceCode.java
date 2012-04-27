package eu.pmsoft.sam.examples;

import javax.inject.Inject;

import eu.pmsoft.sam.binding.BindingAnnotationExample;

public class ServiceCode  {

	@Inject @BindingAnnotationExample
	private ExternalServiceAPI externalService;
	
	public boolean changeUserName(String username,Integer userId){
		ExternalUserModel userModel = externalService.userModel(userId);
		boolean changeOk = userModel.setUserName(username);
		return changeOk;
	}
}
