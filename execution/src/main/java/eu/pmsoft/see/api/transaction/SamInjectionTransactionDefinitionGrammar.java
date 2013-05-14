package eu.pmsoft.see.api.transaction;

import eu.pmsoft.sam.architecture.model.ServiceKeyDeprecated;
import eu.pmsoft.sam.definition.service.SamServiceDefinition;
import eu.pmsoft.see.api.model.SIID;
import eu.pmsoft.see.api.model.SIURL;

public interface SamInjectionTransactionDefinitionGrammar {

    // A grammar based on ServiceImplementation definition.
    // InjectionConfiguration: ExposedContract BindingConfiguration*
    // ExposedServiceInstance
    // BindingConfiguration: ExtrenalContract -> SIID | ExtrenalContract ->
    // SIURL | InjectionConfiguration
    //

    public SamInjectionTransactionDefinitionGrammar idBinding(Class<? extends SamServiceDefinition> bindService, SIID instanceId);

    public SamInjectionTransactionDefinitionGrammar urlBinding(Class<? extends SamServiceDefinition> bindService,
                                                               SIURL instanceUrl);

    public SamInjectionTransactionDefinitionGrammar urlBinding(ServiceKeyDeprecated bindService, SIURL instanceUrl);

    public SamInjectionTransactionDefinitionGrammar idBinding(ServiceKeyDeprecated bindService, SIID instanceId);

    public SamInjectionTransactionDefinitionGrammar nestedTransactionBinding(SamInjectionConfiguration transaction);

    public SamInjectionConfiguration providedByServiceInstance(SIID serviceInstance);
}