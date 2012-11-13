package pmsoft.sam.see;

import java.util.List;

import pmsoft.sam.architecture.definition.SamArchitectureDefinition;
import pmsoft.sam.architecture.loader.ArchitectureModelLoader;
import pmsoft.sam.architecture.loader.IncorrectArchitectureDefinition;
import pmsoft.sam.architecture.model.SamArchitecture;
import pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;
import pmsoft.sam.protocol.execution.ServiceExecutionEnvironment;
import pmsoft.sam.protocol.execution.serial.CanonicalProtocolSerializableModule;
import pmsoft.sam.protocol.injection.CanonicalProtocolExecutionContext;
import pmsoft.sam.protocol.injection.TransactionController;
import pmsoft.sam.see.api.SamArchitectureManagement;
import pmsoft.sam.see.api.SamExecutionNode;
import pmsoft.sam.see.api.SamServiceDiscovery;
import pmsoft.sam.see.api.SamServiceRegistry;
import pmsoft.sam.see.execution.localjvm.LocalSeeExecutionModule;
import pmsoft.sam.see.infrastructure.localjvm.LocalSeeInfrastructureModule;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;

public class SEEServer {

	private final Injector serverInjector;

	public SEEServer(final SEEConfiguration configuration) {
		List<Module> serverModules = Lists.newArrayList();
		serverModules.addAll(configuration.pluginModules);
		serverModules.add(new LocalSeeExecutionModule());
		serverModules.add(new LocalSeeInfrastructureModule());
		serverModules.add(new CanonicalProtocolSerializableModule());
		serverModules.add(new AbstractModule() {
			@Override
			protected void configure() {
				bind(Integer.class).annotatedWith(Names.named(ServiceExecutionEnvironment.SERVICE_PORT_NAMED_BINDING)).toInstance(
						new Integer(configuration.port));
			}
		});
		serverInjector = Guice.createInjector(serverModules);

		SamArchitectureManagement architectureManager = serverInjector.getInstance(SamArchitectureManagement.class);
		SamExecutionNode executionNode = serverInjector.getInstance(SamExecutionNode.class);
		SamServiceRegistry samServiceRegistry = serverInjector.getInstance(SamServiceRegistry.class);

		for (SamArchitectureDefinition architectureDef : configuration.architectures) {
			SamArchitecture architecture;
			try {
				architecture = ArchitectureModelLoader.loadArchitectureModel(architectureDef);
			} catch (IncorrectArchitectureDefinition e) {
				throw new RuntimeException(e);
			}
			architectureManager.registerArchitecture(architecture);
		}

		for (SamServiceImplementationPackageContract implPackage : configuration.implementationPackages) {
			samServiceRegistry.registerServiceImplementationPackage(implPackage);
		}
		for (SEEServiceSetupAction setup : configuration.setupActions) {
			setup.setupService(executionNode);
		}
	}
	
	public void executeSetupAction(SEEServiceSetupAction setup){
		SamExecutionNode executionNode = serverInjector.getInstance(SamExecutionNode.class);
		setup.setupService(executionNode);
	}

	public <T> void executeServiceInteraction(ServiceInteracion<T> interaction) {
		SamExecutionNode executionNode = serverInjector.getInstance(SamExecutionNode.class);
		CanonicalProtocolExecutionContext executionContext = executionNode.createTransactionExecutionContext(interaction.localServiceURL);

		Injector injector = executionContext.getInjector();
		Binding<T> binding = injector.getExistingBinding(interaction.interfaceKey);
		Preconditions.checkState(binding != null,"Service contract dont contains key " + interaction.interfaceKey);
		
		TransactionController transactionController = executionContext.getTransactionController();
		try {
			transactionController.enterTransactionContext();
			T instance = injector.getInstance(interaction.interfaceKey);
			interaction.executeInteraction(instance);
		} finally {
			transactionController.exitTransactionContext();
		}
	}

	public SamServiceDiscovery getServiceDiscovery() {
		return serverInjector.getInstance(SamServiceDiscovery.class);
	}

	public void startUpServer() {
		ServiceExecutionEnvironment serviceExecutionEnvironment = serverInjector.getInstance(ServiceExecutionEnvironment.class);
		serviceExecutionEnvironment.startUpServices();
	}

	public void shutdownServer() {
		ServiceExecutionEnvironment serviceExecutionEnvironment = serverInjector.getInstance(ServiceExecutionEnvironment.class);
		serviceExecutionEnvironment.shutdownServices();
	}

}
