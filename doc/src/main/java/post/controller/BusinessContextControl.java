package post.controller;

import post.controller.model.BusinessData;
import post.controller.model.Context;
import post.controller.model.Manager;
import post.controller.model.Person;

public interface BusinessContextControl {

    void setupBusinessContext(BusinessData data, Person person, Manager manager, Context context);

    void clearBusinessContext();
}
