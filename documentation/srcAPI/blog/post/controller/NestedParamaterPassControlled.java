package blog.post.controller;

import blog.post.controller.model.BusinessData;
import blog.post.controller.model.Context;
import blog.post.controller.model.Manager;
import blog.post.controller.model.Person;
import com.google.inject.Inject;
import com.google.inject.Provider;

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
