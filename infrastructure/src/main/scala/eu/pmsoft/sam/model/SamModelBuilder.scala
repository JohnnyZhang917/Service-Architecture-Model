package eu.pmsoft.sam.model

import eu.pmsoft.sam.architecture.definition.{SamArchitectureLoader, SamArchitectureDefinition}
import eu.pmsoft.sam.definition.implementation.{SamServiceImplementationDefinitionLoader, SamServiceImplementationPackageContract}
import java.lang.annotation.Annotation
import com.google.inject.Key
import eu.pmsoft.sam.definition.service.{SamServiceDefinition, SamServiceDefinitionLoader}
import eu.pmsoft.sam.definition.service.SamServiceDefinitionLoader.SamServiceDefinitionInterfacesLoader
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationDefinitionLoader.ContractAndModule
import eu.pmsoft.sam.architecture.definition.SamArchitectureLoader.SamCategoryLoader


object SamModelBuilder {

  def loadArchitectureDefinition(architectureDefinition: SamArchitectureDefinition) = {
    val loader = new SamArchitectureDefinitionLoader
    architectureDefinition.loadArchitectureDefinition(loader)
    loader.build
  }

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

class SamArchitectureDefinitionLoader extends SamArchitectureLoader {
  var categoryMap: Map[String, SamCategoryBuilder] = Map.empty
  var accessMap: Map[String, Set[String]] = Map.empty

  def accessMark(from: String, to: String) = {
    accessMap.get(from) match {
      case None => accessMap = accessMap updated(from, Set(to))
      case Some(set) => accessMap = accessMap updated(from, set + to)
    }
  }

  private def createCategoryBuilder(id: String) = {
    val builder = new SamCategoryBuilder(id, this)
    categoryMap += id -> builder
    builder
  }

  def createCategory(categoryName: String): SamCategoryLoader = {
    categoryMap.getOrElse(categoryName, createCategoryBuilder(categoryName))
  }

  def build = SamArchitectureObject(categoryMap.values.map(_.category).toSet, accessMap)

}

class SamCategoryBuilder(val id: String, val architectureLoader: SamArchitectureDefinitionLoader) extends SamCategoryLoader {

  var category: SamCategoryObject = SamCategoryObject(id, Set.empty)

  def getCategoryId: String = id

  def accessToCategory(accessibleCategory: SamCategoryLoader): SamCategoryLoader = {
    architectureLoader.accessMark(id, accessibleCategory.getCategoryId)
    this
  }

  def withService(serviceDefinition: SamServiceDefinition): SamCategoryLoader = {
    val service = SamModelBuilder.loadServiceDefinition(serviceDefinition)
    category = category.copy(services = category.services + service)
    this
  }
}


class SamServiceImplementationLoader extends SamServiceImplementationDefinitionLoader with SamServiceImplementationDefinitionLoader.ContractAndModule {
  var expression: ServiceImplementationBuilder = ServiceImplementationBuilder(ServiceImplementationObject(ServiceImplementationKey(null),ServiceKey(null),Set.empty))


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

  var expression: SamServiceDefinitionBuilder = SamServiceDefinitionBuilder(SamServiceObject(ServiceKey(null), Set.empty))

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


case class SEEConfigurationBuilder(config: SEEConfiguration) {

  def withArchitecture(toAdd: SamArchitectureDefinition) = new SEEConfigurationBuilder(config.copy(architectures = config.architectures + toAdd))

  def withImplementation(toAdd: SamServiceImplementationPackageContract) = new SEEConfigurationBuilder(config.copy(implementations = config.implementations + toAdd))

  def build = config

}
