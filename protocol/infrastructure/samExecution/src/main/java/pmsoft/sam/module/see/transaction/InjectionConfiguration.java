package pmsoft.sam.module.see.transaction;

import java.net.URL;

import pmsoft.sam.model.architecture.ServiceKey;
import pmsoft.sam.model.instance.SIID;

public interface InjectionConfiguration {

	public InjectionConfiguration bindInstance(SIID instanceIID);
	
	public void bindExternalInstance(ServiceKey serviceSpecification, URL externalService);
}
