package eu.pmsoft.sam.model

import com.google.inject.Key
import eu.pmsoft.sam.architecture.definition.SamArchitectureDefinition
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract

sealed abstract class ArchitectureEntry


case class ServiceKey(signature: ServiceContract) extends ArchitectureEntry

case class SamServiceObject(id: ServiceKey, contract: Set[Key[_]]) extends ArchitectureEntry

case class SamCategoryObject(id: String, services: Set[SamServiceObject]) extends ArchitectureEntry

case class SamArchitectureObject(
                                  categories: Map[String, SamCategoryObject],
                                  accessMap: Map[String, Set[String]]
                                  ) extends ArchitectureEntry


case class ServiceImplementationKey(module: GuiceModule)

case class ServiceImplementationObject(
                                        key: ServiceImplementationKey,
                                        contract: ServiceKey,
                                        bindedServices: Set[ServiceKey]
                                        ) extends ArchitectureEntry

case class SEEConfiguration(
                            architectures : Set[SamArchitectureDefinition],
                            implementations : Set[SamServiceImplementationPackageContract])


case class SEENodeConfiguration()