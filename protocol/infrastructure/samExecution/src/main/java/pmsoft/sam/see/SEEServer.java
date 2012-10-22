package pmsoft.sam.see;

import java.util.List;

import org.testng.collections.Lists;

import pmsoft.sam.architecture.definition.SamArchitectureDefinition;
import pmsoft.sam.architecture.loader.ArchitectureModelLoader;
import pmsoft.sam.architecture.loader.IncorrectArchitectureDefinition;
import pmsoft.sam.architecture.model.SamArchitecture;
import pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;
import pmsoft.sam.protocol.execution.serial.CanonicalProtocolSerializableModule;
import pmsoft.sam.see.api.SamArchitectureManagement;
import pmsoft.sam.see.api.SamExecutionNode;
import pmsoft.sam.see.api.SamServiceRegistry;
import pmsoft.sam.see.execution.localjvm.LocalSeeExecutionModule;
import pmsoft.sam.see.infrastructure.localjvm.LocalSeeInfrastructureModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class SEEServer {

	private final Injector serverInjector;

	public SEEServer(SEEConfiguration configuration) {
		List<Module> serverModules = Lists.newArrayList();
		serverModules.addAll(configuration.pluginModules);
		serverModules.add(new LocalSeeExecutionModule());
		serverModules.add(new LocalSeeInfrastructureModule());
		serverModules.add(new CanonicalProtocolSerializableModule());
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

	public void startUpServer() {

	}
}
