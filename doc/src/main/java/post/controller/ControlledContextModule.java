package post.controller;

import com.google.inject.PrivateModule;
import post.controller.model.BusinessData;
import post.controller.model.Context;
import post.controller.model.Manager;
import post.controller.model.Person;

public class ControlledContextModule extends PrivateModule {

    @Override
    protected void configure() {
        ControlledScope scope = new ControlledScope();
        bind(BusinessData.class).in(scope);
        bind(Context.class).in(scope);
        bind(Person.class).in(scope);
        bind(Manager.class).in(scope);
        bind(BusinessContextControl.class).toInstance(scope);
        // additional bindings related to business operation with context given
        // by the controlled scope.
        // Don't forget to expose business API
    }

}
