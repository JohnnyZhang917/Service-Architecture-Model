package pmsoft.sam.see.injectionUtils;

import java.util.List;
import java.util.Set;

import pmsoft.sam.architecture.api.SamArchitectureRegistry;
import pmsoft.sam.architecture.model.SamService;
import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.inject.free.FreeVariableBindingBuilder;
import pmsoft.sam.see.api.model.SamServiceImplementation;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;

public class ServiceInjectionUtils {

	public static Injector createServiceInstanceInjector(SamServiceImplementation serviceImplementation, SamArchitectureRegistry architectureService) {

		Class<? extends Module> implModule = serviceImplementation.getModule();
		Module implModuleInstance;
		try {
			implModuleInstance = implModule.newInstance();
			//TODO handle error
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
		
		Set<ServiceKey> injectServices = serviceImplementation.getBindedServices();
		List<List<Key<?>>> injectedFreeVariableBinding= Lists.newArrayList();
		for (ServiceKey externalServiceKey : injectServices) {
			List<Key<?>> serviceKeys = Lists.newArrayList();
			SamService externalServiceSpec = architectureService.getService(externalServiceKey);
			Set<Class<?>> interfaces = externalServiceSpec.getServiceInterfaces();
			for (Class<?> serviceApi : interfaces) {
				serviceKeys.add(Key.get(serviceApi));
			}
			injectedFreeVariableBinding.add(serviceKeys);
		}
		Module freeVariableModule = FreeVariableBindingBuilder.createFreeBindingModule(injectedFreeVariableBinding);
		Injector implInjector = Guice.createInjector(implModuleInstance,freeVariableModule);
		return implInjector;
	}
}
