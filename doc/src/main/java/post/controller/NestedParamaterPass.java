package post.controller;

import post.controller.model.BusinessData;
import post.controller.model.Context;
import post.controller.model.Manager;
import post.controller.model.Person;

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
