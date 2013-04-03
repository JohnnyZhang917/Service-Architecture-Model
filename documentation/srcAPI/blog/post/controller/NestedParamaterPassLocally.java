package blog.post.controller;

import blog.post.controller.model.BusinessData;
import blog.post.controller.model.Context;
import blog.post.controller.model.Manager;
import blog.post.controller.model.Person;

public class NestedParamaterPassLocally {

    private ThreadLocal<BusinessData> data;
    private ThreadLocal<Person> person;
    private ThreadLocal<Manager> manager;
    private ThreadLocal<Context> context;

    private void clearBusinessContext() {
        this.data.set(null);
        this.person.set(null);
        this.manager.set(null);
        this.context.set(null);
    }

    private void setupBusinessContext(BusinessData data, Person person, Manager manager, Context context) {
        this.data.set(data);
        this.person.set(person);
        this.manager.set(manager);
        this.context.set(context);
    }

    public void businessOperation(BusinessData data, Person person, Manager manager, Context context) {
        setupBusinessContext(data, person, manager, context);
        // some operation
        firstNestedCall();

        clearBusinessContext();
    }


    private void firstNestedCall() {
        secondNestedCall();
        BusinessData currentData = data.get();
    }

    private void secondNestedCall() {
        // just keep going
        SomeHelperClass helper = new SomeHelperClass();
        helper.passMePleaseTheBusinessObjects();
    }
}
