package eu.pmsoft.sam.model

import com.google.inject.Key
import java.util.concurrent.atomic.AtomicInteger


sealed abstract class ArchitectureModel

case class SamServiceKey(signature: ServiceContract)

case class SamServiceImplementationKey(module: GuiceModule, contract: SamServiceKey)


case class SamService(id: SamServiceKey, api: Set[Key[_]]) extends ArchitectureModel

case class SamCategory(id: String, services: Set[SamService]) extends ArchitectureModel

case class SamArchitecture(
                            signature: String,
                            categories: Set[SamCategory],
                            accessMap: Map[String, Set[String]]
                            ) extends ArchitectureModel


case class SamServiceImplementation(implKey: SamServiceImplementationKey,
                                    //                                    contract: SamServiceKey,
                                    bindServices: Seq[SamServiceKey]
                                     ) extends ArchitectureModel

object ServiceInstanceID {
  private val counter: AtomicInteger = new AtomicInteger(0)

  def apply() = new ServiceInstanceID(counter.addAndGet(1))
}

class ServiceInstanceID(val id: Int)

