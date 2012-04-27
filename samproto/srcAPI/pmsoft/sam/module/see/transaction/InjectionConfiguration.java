package pmsoft.sam.module.see.transaction;

import java.net.URL;

import pmsoft.sam.module.model.SIID;
import pmsoft.sam.module.model.ServiceKey;

public interface InjectionConfiguration {

	public InjectionConfiguration bindInstance(SIID instanceIID);
	
	public void bindExternalInstance(ServiceKey serviceSpecification, URL externalService);
}
