package pmsoft.sam.module.definition.implementation;

import java.util.Arrays;
import java.util.List;

import pmsoft.sam.module.definition.architecture.SamServiceDefinition;
import pmsoft.sam.module.definition.implementation.grammar.SamServiceImplementationLoader;
import pmsoft.sam.module.model.ServiceKey;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Module;

/**
 * Definition of Service Implementations.
 * 
 * Multiple implementations may be defined in one class.
 * 
 * @author Paweł Cesar Sanjuan Szklarz
 * 
 */
public abstract class AbstractSamServiceImplementationDefinition implements SamServiceImplementationDefinition {

	SamServiceImplementationLoader loaderReference = null;

	public final void loadOn(SamServiceImplementationLoader loader) {
		Preconditions.checkState(this.loaderReference == null);
		try {
			this.loaderReference = loader;
			implementationDefinition();
		} finally {
			this.loaderReference = null;
		}
	}

	public abstract void implementationDefinition();

	protected ModuleServiceImplementationStatement registerImplementationOf(ServiceKey service) {
		return new DefinitionStatement(service);
	}

	protected ModuleServiceImplementationStatement registerImplementationOf(String serviceSignature) {
		return new DefinitionStatement(new ServiceKey(serviceSignature));
	}

	protected ModuleServiceImplementationStatement registerImplementationOf(
			Class<? extends SamServiceDefinition> serviceDefinitionClass) {
		return new DefinitionStatement(new ServiceKey(serviceDefinitionClass));
	}

	public class DefinitionStatement implements ModuleServiceImplementationStatement, FinalPrototypeDeclarationStatement {

		private final ServiceKey service;
		private Class<? extends Module> module;
		private List<ServiceKey> injectedServices = Lists.newArrayList();
		boolean ready = true;

		public DefinitionStatement(ServiceKey service) {
			super();
			this.service = service;
		}

		public FinalPrototypeDeclarationStatement givenByModule(Class<? extends Module> module) {
			Preconditions.checkState(this.module == null);
			this.module = module;
			return this;
		}

		public void done() {
			Preconditions.checkState(ready, " Only one call to service implementation definition is allowed");
			ready = false;
			loaderReference.registerImplementation(service, module, injectedServices);
		}

		public FinalPrototypeDeclarationStatement accessTo(ServiceKey... externals) {
			injectedServices.addAll(Arrays.asList(externals));
			return this;
		}

		public FinalPrototypeDeclarationStatement accessTo(String... externals) {
			ServiceKey[] keys = new ServiceKey[externals.length];
			for (int i = 0; i < externals.length; i++) {
				keys[i] = new ServiceKey(externals[i]);
			}
			return accessTo(keys);
		}

		public FinalPrototypeDeclarationStatement accessTo(Class<? extends SamServiceDefinition> external) {
			return accessTo(new ServiceKey(external));
		}

	}

}
