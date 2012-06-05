package blog.post.controller;

import com.google.inject.Inject;
import com.google.inject.Provider;

import blog.post.controller.model.Manager;
import blog.post.controller.model.Person;

public class SomeHelperClass {

	@Inject
	private Provider<Person> person;
	@Inject
	private Provider<Manager> manager;
	
	public void passMePleaseTheBusinessObjects(){
		Person currentPerson = person.get();
		Manager currentManager = manager.get();
		// make some business operation on Person and Manager only.
	}
	
}
