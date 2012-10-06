package pmsoft.sam.module.see.local.transaction;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pmsoft.sam.architecture.model.SamArchitecture;
import pmsoft.sam.architecture.model.SamService;
import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.canonical.deprecated.service.CanonicalProtocolExecutionService;
import pmsoft.sam.inject.wrapper.deprecated.ServiceBindingDefinition;
import pmsoft.sam.inject.wrapper.deprecated.WrappingInjectorBuilder;
import pmsoft.sam.inject.wrapper.deprecated.WrappingInjectorLoader;
import pmsoft.sam.inject.wrapper.deprecated.WrappingInjectorMainLoader;
import pmsoft.sam.module.see.ServiceExecutionEnviroment;
import pmsoft.sam.module.see.local.ServiceExecutionEnviromentInternalAPI;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;

public abstract class AbstractInjectionConfiguration implements InjectionConfigurationInternal {

	private final ImmutableList<ServiceKey> injectionPoints;
	private final Map<ServiceKey, InjectionConfigurationInternal> injectionConfiguration = Maps.newHashMap();
	private final ServiceKey serviceSpecification;

	public AbstractInjectionConfiguration(ServiceKey serviceSpecification, List<ServiceKey> injectionPoints) {
		super();
		if (injectionPoints == null) {
			this.injectionPoints = ImmutableList.of();
		} else {
			this.injectionPoints = ImmutableList.copyOf(injectionPoints);
		}
		this.serviceSpecification = serviceSpecification;
	}

	public ServiceKey getServiceSpecification() {
		return serviceSpecification;
	}

	protected final void addInjectionConfiguration(ServiceKey specification, InjectionConfigurationInternal configuration) {
		Preconditions.checkState(injectionPoints.contains(specification));
		Preconditions.checkState(!injectionConfiguration.containsKey(specification));
		injectionConfiguration.put(specification, configuration);
	}
	

	private void verifyInjectionPoints() {
		for (ServiceKey expected : injectionPoints) {
			Preconditions.checkState(injectionConfiguration.containsKey(expected),"Injection point %s for serviceSpecification %s is not configured for %s",expected,serviceSpecification,this);
		}
	}

	public Injector createTransactionInjector(ServiceExecutionEnviromentInternalAPI seeInternalApi, SamArchitecture architecture,
			CanonicalProtocolExecutionService canonicalExecutionService, URL transactionURL) {

		ServiceBindingDefinition rootServiceDefinition = getBindingDefinition(seeInternalApi, architecture);
		WrappingInjectorMainLoader rootLoader = WrappingInjectorBuilder.createLoader(rootServiceDefinition);
		loadInjectionConfiguration(seeInternalApi, architecture, rootLoader);
		Module rootTransactionModule = rootLoader.createWrappingInjector(canonicalExecutionService,transactionURL);
		return Guice.createInjector(rootTransactionModule);
	}

	public void loadInjectionConfiguration(ServiceExecutionEnviroment see, SamArchitecture architecture,
			WrappingInjectorLoader parentLoader) {
		verifyInjectionPoints();
		List<ServiceBindingDefinition> bindingDefinitionList = Lists.newArrayList();
		for (ServiceKey injectedService : injectionPoints) {
			InjectionConfigurationInternal internalConfigurationElement = injectionConfiguration.get(injectedService);
			bindingDefinitionList.add(internalConfigurationElement.getBindingDefinition(see, architecture));
		}
		List<WrappingInjectorLoader> levelDownListOfLoaders = parentLoader.bindService(bindingDefinitionList);
		for (int i = 0; i < injectionPoints.size(); i++) {
			injectionConfiguration.get(injectionPoints.get(i)).loadInjectionConfiguration(see, architecture,
					levelDownListOfLoaders.get(i));
		}
	}


	protected final List<Key<?>> getServiceSpecificationBindings(ServiceExecutionEnviroment see, SamArchitecture architecture,
			ServiceKey serviceSpecification) {
		SamService rootServiceSpecification = architecture.getService(serviceSpecification);
		List<Key<?>> serviceKeys = Lists.newArrayList();
		Set<Key<?>> keys = rootServiceSpecification.getServiceContractAPI();
		for (Key<?> serviceApi : keys) {
			serviceKeys.add(serviceApi);
		}
		return serviceKeys;
	}

}
