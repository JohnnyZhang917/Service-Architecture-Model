package blog.post.controller;

import blog.post.controller.model.BusinessData;
import blog.post.controller.model.Context;
import blog.post.controller.model.Manager;
import blog.post.controller.model.Person;

public interface BusinessContextControl {

	void setupBusinessContext(BusinessData data, Person person, Manager manager, Context context);
	
	void clearBusinessContext();
}
