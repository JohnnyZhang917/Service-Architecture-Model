package pmsoft.sam.see.api.transaction;

import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.definition.service.SamServiceDefinition;
import pmsoft.sam.see.api.model.SIID;
import pmsoft.sam.see.api.model.SIURL;

public interface SamInjectionTransactionDefinitionGrammar {

	// A grammar based on ServiceImplementation definition.
	// InjectionConfiguration: ExposedContract BindingConfiguration*
	// ExposedServiceInstance
	// BindingConfiguration: ExtrenalContract -> SIID | ExtrenalContract ->
	// SIURL | InjectionConfiguration
	//

	public SamInjectionTransactionDefinitionGrammar idBinding(Class<? extends SamServiceDefinition> bindedService, SIID instanceId);

	public SamInjectionTransactionDefinitionGrammar urlBinding(Class<? extends SamServiceDefinition> bindedService,
			SIURL instanceUrl);

	public SamInjectionTransactionDefinitionGrammar urlBinding(ServiceKey bindedService, SIURL instanceUrl);

	public SamInjectionTransactionDefinitionGrammar idBinding(ServiceKey bindedService, SIID instanceId);

	public SamInjectionTransactionDefinitionGrammar nestedTransactionBinding(SamInjectionConfiguration transaction);

	public SamInjectionConfiguration providedByServiceInstance(SIID serviceInstance);
}