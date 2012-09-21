package pmsoft.sam.module.see.local;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pmsoft.sam.architecture.model.SamArchitecture;
import pmsoft.sam.architecture.model.SamService;
import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.inject.free.FreeVariableBindingBuilder;
import pmsoft.sam.model.instance.SIID;
import pmsoft.sam.model.instance.ServiceImplementation;
import pmsoft.sam.model.instance.ServiceImplementationKey;
import pmsoft.sam.module.see.ExternalServiceReference;
import pmsoft.sam.module.see.ServiceInstance;
import pmsoft.sam.module.see.local.transaction.TransactionDomainFactory;
import pmsoft.sam.module.see.local.transaction.TransactionInstance;
import pmsoft.sam.module.see.serviceRegistry.SamServiceRegistry;
import pmsoft.sam.module.see.transaction.SamTransaction;
import pmsoft.sam.module.see.transaction.TransactionConfigurator;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;

public class ServiceExecutionEnviromentLocal implements ServiceExecutionEnviromentInternalAPI {

	@Inject
	private SamArchitecture architectureService;

	@Inject
	private SamServiceRegistry serviceRegistry;

	@Inject
	private TransactionDomainFactory domainFactory;

	Map<SIID, Injector> runningServiceInstance = Maps.newHashMap();
	Map<SIID, ServiceImplementationKey> runningServiceKeys = Maps.newHashMap();
	Multimap<ServiceImplementationKey, SIID> runningServiceByKey = HashMultimap.create();

	//FIXME create weak reference infrastructure and configuration clean based on finalize methods
	Map<URL,TransactionInstance> runningTransactionMap = Maps.newHashMap();
	
	private int transactionNr=0;
	public URL generateTransactionURL(TransactionInstance transactionInstance) {
		try {
			int transactionNumer = transactionNr++;
			URL transactionURL = new URL("http", "localhost", "/transaction"+transactionNumer);
			runningTransactionMap.put(transactionURL, transactionInstance);
			return transactionURL;
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public SamTransaction getSamTransaction(URL url) {
		return runningTransactionMap.get(url);
	}
	
	public SIID executeServiceInstance(ServiceImplementationKey implementationKey) {
		ServiceKey serviceKey = implementationKey.getServiceSpecificationKey();
		SamService serviceSpec = architectureService.getService(serviceKey);
		Preconditions.checkNotNull(serviceSpec,"Service definition %s is not registered on the architecture",serviceKey);

		ServiceImplementation impl = serviceRegistry.getImplementation(implementationKey);
		
		Preconditions.checkNotNull(impl,"Can not find mplementation for key %s",implementationKey);
		Class<? extends Module> implModule = impl.getRootModule();
		SIID implIID = new SIID(serviceKey,implementationKey,impl.getInjectedServices());
		Module implModuleInstance;
		try {
			implModuleInstance = implModule.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
		
		List<ServiceKey> injectServices = impl.getInjectedServices();
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
		runningServiceInstance.put(implIID, implInjector);
		runningServiceByKey.put(implementationKey, implIID);
		runningServiceKeys.put(implIID, implementationKey);
		return implIID;
	}

	public ServiceImplementationKey getImplementationKeyForSIID(SIID key) {
		return runningServiceKeys.get(key);
	}

	public List<ServiceInstance> getInstanceForService(SamService service) {
		List<ServiceInstance> instance = Lists.newArrayList();
		List<ServiceImplementationKey> serviceImplementations = serviceRegistry.getImplementationsForSpecification(service
				.getServiceKey());
		for (ServiceImplementationKey serviceImplementationKey : serviceImplementations) {
			Collection<SIID> siid = runningServiceByKey.get(serviceImplementationKey);
			for (SIID instanceKey : siid) {
				ServiceInstanceModel model = new ServiceInstanceModel(runningServiceInstance.get(instanceKey), instanceKey);
				instance.add(model);
			}
		}
		return instance;
	}

	public ServiceInstance getServiceInstance(SIID instanceKey) {
		ServiceInstanceModel model = new ServiceInstanceModel(runningServiceInstance.get(instanceKey), instanceKey);
		return model;
	}
	
	public TransactionConfigurator createTransactionConfiguration(SIID rootInstance) {
		return domainFactory.createTransactionConfigurator(rootInstance);
	}
	
	public ExternalServiceReference getExternalServiceReference(URL targetURL) {
		// FIXME: Implement a real distributed system for SEE.
		Preconditions.checkState(runningTransactionMap.containsKey(targetURL),"Prototype implementation only look for local transactions and this transaction is not locally known.");
		return runningTransactionMap.get(targetURL);
	}

}