package pmsoft.sam.module.definition.test.data.service;

public interface Service1a {

	/**
	 * For testing: This method must return the signature of the service implementation key
	 * @return
	 */
	
	public String getImplementationName();
	
	
	/**
	 * Test interconnection with external services
	 * @return
	 */
	public boolean testServiceInteraction();
}
