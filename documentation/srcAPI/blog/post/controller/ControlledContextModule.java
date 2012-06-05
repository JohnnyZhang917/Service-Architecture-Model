package blog.post.controller;

import blog.post.controller.model.BusinessData;
import blog.post.controller.model.Context;
import blog.post.controller.model.Manager;
import blog.post.controller.model.Person;

import com.google.inject.PrivateModule;

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
