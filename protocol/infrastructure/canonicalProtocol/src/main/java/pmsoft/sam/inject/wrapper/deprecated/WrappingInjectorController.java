package pmsoft.sam.inject.wrapper.deprecated;

import pmsoft.sam.protocol.injection.CanonicalProtocolRequestHandler;

/**
 * Interface to control all ExternalBindingController's of the service instance
 * registered on the transaction.
 * 
 * First monitoring wrapper called get true on configuration binding, then it
 * MUST unbind the configuration. 
 * 
 * @author Paweł Cesar Sanjuan Szklarz
 * 
 */
public interface WrappingInjectorController {

	/**
	 * Bind injection configuration to all service instance.
	 * 
	 * The monitoring wrapper that gets true should unbind the injection
	 * configuration
	 * 
	 * @return true when injection configuration is already injected
	 */
	boolean bindExecutionContext();

	/**
	 * Unbind the injection configuration.
	 * 
	 * In configuration not bind then throw illegal state.
	 * 
	 */
	void unbindExecutionContext();
	
	/**
	 * Access to the internal transaction record instance. FIXME separate to make clear where transaction record context must be visible.
	 * @return
	 */
	public CanonicalProtocolRequestHandler getRequestHandler();

}
