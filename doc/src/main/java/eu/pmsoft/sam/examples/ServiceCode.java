package eu.pmsoft.sam.examples;

import eu.pmsoft.sam.binding.BindingAnnotationExample;

import javax.inject.Inject;

public class ServiceCode {

    @Inject
    @BindingAnnotationExample
    private ExternalServiceAPI externalService;

    public boolean changeUserName(String username, Integer userId) {
        ExternalUserModel userModel = externalService.userModel(userId);
        boolean changeOk = userModel.setUserName(username);
        return changeOk;
    }
}
