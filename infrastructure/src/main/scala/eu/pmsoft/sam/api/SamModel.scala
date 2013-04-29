package eu.pmsoft.sam.api

import com.google.inject.{Injector, Module}

object SamModel {
  def serviceKey(module: Class[_ <: Module]) = new SamServiceImplementationKey(module.getName)
}
class SamModel {

}

trait SamServiceInstance {
  val injector : Injector

  val key : SIID

}

case class SIID()

case class SamServiceImplementationKey(key: String)


