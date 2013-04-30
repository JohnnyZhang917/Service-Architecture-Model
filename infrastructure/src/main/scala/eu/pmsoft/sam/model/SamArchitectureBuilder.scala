package eu.pmsoft.sam.model

import eu.pmsoft.sam.architecture.definition.SamArchitectureDefinition
import eu.pmsoft.sam.definition.implementation.{SamServiceImplementationDefinitionLoader, SamServiceImplementationPackageContract}
import java.lang.annotation.Annotation
import com.google.inject.Key
import eu.pmsoft.sam.definition.service.SamServiceDefinitionLoader
import eu.pmsoft.sam.definition.service.SamServiceDefinitionLoader.SamServiceDefinitionInterfacesLoader
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationDefinitionLoader.ContractAndModule


object SamArchitectureDefinitionLoader {

  def loadServiceDefinition(serviceDefinition: ServiceDefinition): SamServiceObject = {
    val loader = new SamServiceLoader
    serviceDefinition.loadServiceDefinition(loader)
    loader.expression.build
  }

  def loadImplementationPackage(implementationPackage: ServiceImplementationDefinition): ServiceImplementationObject = {
    val loader = new SamServiceImplementationLoader
    implementationPackage.loadServiceImplementationDefinition(loader)
    loader.expression.build
  }

}

object SamArchitectureBuilder {

  def serviceBuilder: SamServiceDefinitionBuilder = SamServiceDefinitionBuilder(SamServiceObject(ServiceKey(null), Set.empty))

  def nodeConfiguration: SEEConfigurationGrammar = SEEConfigurationBuilder(SEEConfiguration(Set.empty, Set.empty))

  def implementationBuilder: ServiceImplementationBuilder = {
    ServiceImplementationBuilder(
      ServiceImplementationObject(
        ServiceImplementationKey(null),
        ServiceKey(null),
        Set.empty
      ))
  }

}


class SamServiceImplementationLoader extends SamServiceImplementationDefinitionLoader with SamServiceImplementationDefinitionLoader.ContractAndModule {
  var expression: ServiceImplementationBuilder = SamArchitectureBuilder.implementationBuilder

  def withBindingsTo(userService: ServiceContract): ContractAndModule = {
    expression = expression.withBindingsTo(userService)
    this
  }

  def signature(contract: ServiceContract, serviceImplementationModule: GuiceModule): ContractAndModule = {
    expression = expression.signature(contract, serviceImplementationModule)
    this
  }
}

case class ServiceImplementationBuilder(implementation: ServiceImplementationObject) {
  def withBindingsTo(userService: ServiceContract) = {
    ServiceImplementationBuilder(
      implementation.copy(
        bindedServices = implementation.bindedServices + ServiceKey(userService)
      ))
  }

  def signature(contract: ServiceContract, serviceImplementationModule: GuiceModule) = {
    ServiceImplementationBuilder(
      implementation.copy(
        key = ServiceImplementationKey(serviceImplementationModule),
        contract = ServiceKey(contract)
      ))
  }

  def build = implementation

}


class SamServiceLoader extends SamServiceDefinitionLoader with SamServiceDefinitionInterfacesLoader {

  var expression: SamServiceDefinitionBuilder = SamArchitectureBuilder.serviceBuilder

  def definedIn(definitionClass: ServiceContract): SamServiceDefinitionInterfacesLoader = {
    expression = expression.definedIn(definitionClass)
    this
  }

  def withKey(interfaceReference: Class[_]) {
    expression = expression.withKey(interfaceReference)
  }

  def withKey(interfaceReference: Class[_], annotation: Annotation) {
    expression = expression.withKey(interfaceReference, annotation)
  }

  def withKey(key: Key[_]) {
    expression = expression.withKey(key)
  }
}

case class SamServiceDefinitionBuilder(service: SamServiceObject) {

  def definedIn(contract: ServiceContract) = SamServiceDefinitionBuilder(service.copy(id = ServiceKey(contract)))

  def withKey(interfaceReference: Class[_]): SamServiceDefinitionBuilder = withKey(Key.get(interfaceReference))

  def withKey(interfaceReference: Class[_], annotation: Annotation): SamServiceDefinitionBuilder = withKey(Key.get(interfaceReference, annotation))

  def withKey(key: Key[_]): SamServiceDefinitionBuilder = SamServiceDefinitionBuilder(service.copy(contract = service.contract + key))

  def build: SamServiceObject = service
}


trait SEEConfigurationGrammar {
  def withArchitecture(toAdd: SamArchitectureDefinition): SEEConfigurationGrammar

  def withImplementation(toAdd: SamServiceImplementationPackageContract): SEEConfigurationGrammar

  def build: SEEConfiguration

}

case class SEEConfigurationBuilder(config: SEEConfiguration) extends SEEConfigurationGrammar {

  def withArchitecture(toAdd: SamArchitectureDefinition) = new SEEConfigurationBuilder(config.copy(architectures = config.architectures + toAdd))

  def withImplementation(toAdd: SamServiceImplementationPackageContract) = new SEEConfigurationBuilder(config.copy(implementations = config.implementations + toAdd))

  def build = config

}
