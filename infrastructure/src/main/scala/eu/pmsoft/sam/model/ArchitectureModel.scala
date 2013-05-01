package eu.pmsoft.sam.model

import com.google.inject.Key
import eu.pmsoft.sam.architecture.definition.SamArchitectureDefinition
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract

sealed abstract class ArchitectureModel

case class ServiceKey(signature: ServiceContract) extends ArchitectureModel

case class SamServiceObject(id: ServiceKey, contract: Set[Key[_]]) extends ArchitectureModel

case class SamCategoryObject(id: String, services: Set[SamServiceObject]) extends ArchitectureModel

case class SamArchitectureObject(
                                  categories: Set[SamCategoryObject],
                                  accessMap: Map[String, Set[String]]
                                  ) extends ArchitectureModel


case class ServiceImplementationKey(module: GuiceModule)

case class ServiceImplementationObject(
                                        key: ServiceImplementationKey,
                                        contract: ServiceKey,
                                        bindedServices: Set[ServiceKey]
                                        ) extends ArchitectureModel

case class SEEConfiguration(
                            architectures : Set[SamArchitectureDefinition],
                            implementations : Set[SamServiceImplementationPackageContract])

case class SEENodeConfiguration()