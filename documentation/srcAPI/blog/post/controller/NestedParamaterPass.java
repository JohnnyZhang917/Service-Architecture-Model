package blog.post.controller;

import blog.post.controller.model.BusinessData;
import blog.post.controller.model.Context;
import blog.post.controller.model.Manager;
import blog.post.controller.model.Person;

public class NestedParamaterPass {

    public void businessOperation(BusinessData data, Person person, Manager manager, Context context) {
        // some operation
        firstNestedCall(data, person, manager, context);
    }

    private void firstNestedCall(BusinessData data, Person person, Manager manager, Context context) {
        secondNestedCall(data, person, manager, context);
    }

    private void secondNestedCall(BusinessData data, Person person, Manager manager, Context context) {
        // just keep going
    }
}
