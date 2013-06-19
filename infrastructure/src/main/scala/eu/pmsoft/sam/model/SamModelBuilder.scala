package eu.pmsoft.sam.model

import eu.pmsoft.sam.architecture.definition.{SamArchitectureLoader, SamArchitectureDefinition}
import eu.pmsoft.sam.definition.implementation.{SamServicePackageLoader, SamServiceImplementationDefinitionLoader, SamServiceImplementationPackageContract}
import java.lang.annotation.Annotation
import com.google.inject.Key
import eu.pmsoft.sam.definition.service.{SamServiceDefinition, SamServiceDefinitionLoader}
import eu.pmsoft.sam.definition.service.SamServiceDefinitionLoader.SamServiceDefinitionInterfacesLoader
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationDefinitionLoader.ContractAndModule
import eu.pmsoft.sam.architecture.definition.SamArchitectureLoader.SamCategoryLoader



case class SEEConfiguration(
                             architectures: Set[SamArchitectureDefinition],
                             implementations: Set[SamServiceImplementationPackageContract],
                             port: Int)

case class SEEConfigurationBuilder(config: SEEConfiguration) {

  def withArchitecture(toAdd: SamArchitectureDefinition) = new SEEConfigurationBuilder(config.copy(architectures = config.architectures + toAdd))

  def withImplementation(toAdd: SamServiceImplementationPackageContract) = new SEEConfigurationBuilder(config.copy(implementations = config.implementations + toAdd))

  def build = config

}





object SamModelBuilder {



  def implementationKey(module: GuiceModule) = SamServiceImplementationKey(module)

  def loadArchitectureDefinition(architectureDefinition: SamArchitectureDefinition): SamArchitecture = {
    val loader = new SamArchitectureDefinitionLoader
    architectureDefinition.loadArchitectureDefinition(loader)
    loader.build
  }

  def loadServiceDefinition(serviceDefinition: ServiceDefinition): SamService = {
    val loader = new SamServiceLoader
    serviceDefinition.loadServiceDefinition(loader)
    loader.expression.build
  }

  def loadImplementationPackage(implementationPackage: SamServiceImplementationPackageContract): Set[SamServiceImplementation] = {
    val reader = new SamServicePackageDefinitionLoader
    implementationPackage.loadContractPackage(reader)
    reader.implementations.map(loadImplementationDefinition _)
  }

  def loadImplementationDefinition(implementationDefinition: ServiceImplementationDefinition): SamServiceImplementation = {
    val loader = new SamServiceImplementationLoader
    implementationDefinition.loadServiceImplementationDefinition(loader)
    loader.expression.build
  }

}

class SamServicePackageDefinitionLoader extends SamServicePackageLoader {

  var implementations: Set[ServiceImplementationDefinition] = Set.empty

  def registerImplementation(serviceImplementationContract: ServiceImplementationDefinition) {
    implementations = implementations + serviceImplementationContract
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

  def build = SamArchitecture(categoryMap.values.map(_.category).toSet, accessMap)

}

class SamCategoryBuilder(val id: String, val architectureLoader: SamArchitectureDefinitionLoader) extends SamCategoryLoader {

  var category: SamCategory = SamCategory(id, Set.empty)

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
  var expression: ServiceImplementationBuilder = ServiceImplementationBuilder(SamServiceImplementation(null, null, Seq.empty))


  def withBindingsTo(userService: ServiceContract): ContractAndModule = {
    expression = expression.withBindingsTo(userService)
    this
  }

  def signature(contract: ServiceContract, serviceImplementationModule: GuiceModule): ContractAndModule = {
    expression = expression.signature(contract, serviceImplementationModule)
    this
  }
}

case class ServiceImplementationBuilder(implementation: SamServiceImplementation) {
  def withBindingsTo(userService: ServiceContract) = {
    ServiceImplementationBuilder(
      implementation.copy(
        bindServices =  SamServiceKey(userService) +: implementation.bindServices
      ))
  }

  def signature(contract: ServiceContract, serviceImplementationModule: GuiceModule) = {
    ServiceImplementationBuilder(
      implementation.copy(
        implKey = SamServiceImplementationKey(serviceImplementationModule),
        contract = SamServiceKey(contract)
      ))
  }

  def build = implementation

}


class SamServiceLoader extends SamServiceDefinitionLoader with SamServiceDefinitionInterfacesLoader {

  var expression: SamServiceDefinitionBuilder = SamServiceDefinitionBuilder(SamService(SamServiceKey(null), Set.empty))

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

case class SamServiceDefinitionBuilder(service: SamService) {

  def definedIn(contract: ServiceContract) = SamServiceDefinitionBuilder(service.copy(id = SamServiceKey(contract)))

  def withKey(interfaceReference: Class[_]): SamServiceDefinitionBuilder = withKey(Key.get(interfaceReference))

  def withKey(interfaceReference: Class[_], annotation: Annotation): SamServiceDefinitionBuilder = withKey(Key.get(interfaceReference, annotation))

  def withKey(key: Key[_]): SamServiceDefinitionBuilder = SamServiceDefinitionBuilder(service.copy(api = service.api + key))

  def build: SamService = service
}


