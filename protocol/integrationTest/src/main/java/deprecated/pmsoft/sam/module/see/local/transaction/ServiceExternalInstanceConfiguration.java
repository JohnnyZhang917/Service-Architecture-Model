package deprecated.pmsoft.sam.module.see.local.transaction;

import static com.google.common.base.Preconditions.checkState;

import java.net.URL;

import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.see.api.model.SIID;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import deprecated.pmsoft.sam.module.see.transaction.InjectionConfiguration;

public class ServiceExternalInstanceConfiguration extends AbstractInjectionConfiguration {

	private final URL externalInstanceURL;

	@AssistedInject
	public ServiceExternalInstanceConfiguration(@Assisted ServiceKey serviceSpecification, @Assisted URL externalInstanceURL) {
		super(serviceSpecification, null);
		this.externalInstanceURL = externalInstanceURL;
	}

	public InjectionConfiguration bindInstance(SIID instanceIID) {
		checkState(false, "Use API do not allow to make this call");
		return null;
	}

	public void bindExternalInstance(ServiceKey serviceSpecification, URL externalService) {
		checkState(false, "Use API do not allow to make this call");
	}

//	public ServiceBindingDefinition getBindingDefinition(ServiceExecutionEnviroment see, SamArchitecture architecture) {
//		Collection<Key<?>> serviceKeys = getServiceSpecificationBindings(see, architecture, getServiceSpecification());
//		return new ServiceBindingDefinition(serviceKeys, externalInstanceURL);
//	}

	@Override
	public String toString() {
		return "ServiceExternalInstanceConfiguration [externalInstanceURL=" + externalInstanceURL + "]";
	}
	

}