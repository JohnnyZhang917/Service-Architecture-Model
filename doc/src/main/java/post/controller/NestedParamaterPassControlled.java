package post.controller;

import com.google.inject.Inject;
import com.google.inject.Provider;
import post.controller.model.BusinessData;
import post.controller.model.Context;
import post.controller.model.Manager;
import post.controller.model.Person;

public class NestedParamaterPassControlled {
    @Inject
    private BusinessContextControl controller;
    @Inject
    private Provider<BusinessData> dataProvider;
    @Inject
    private SomeHelperClass helper;

    public void businessOperation(BusinessData data, Person person, Manager manager, Context context) {
        controller.setupBusinessContext(data, person, manager, context);
        // some operation
        firstNestedCall();

        controller.clearBusinessContext();
    }


    private void firstNestedCall() {
        secondNestedCall();
        BusinessData currentData = dataProvider.get();
    }

    private void secondNestedCall() {
        // just keep going
        helper.passMePleaseTheBusinessObjects();
    }
}
