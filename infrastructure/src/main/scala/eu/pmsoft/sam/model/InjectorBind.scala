package eu.pmsoft.sam.model

sealed abstract class InjectorBind

case class SIID()

case class SIURL()

//abstract case class BindPoint(contract : ServiceKey) extends BindTransactionModel

case class InjectorBindPointSIID(instance : SIID, contract : ServiceKey) extends InjectorBind
case class InjectorBindPointExternal(endpoint : SIURL, contract : ServiceKey) extends InjectorBind

case class InjectionBindService(exposedInstance : SIID, contract: ServiceKey, bindings: Set[InjectorBind]) extends InjectorBind


object InjectorBind {
  def getExternalService(bind : InjectorBind) : Set[InjectorBindPointExternal] = {
    bind match {
      case InjectorBindPointSIID(_,_) => Set.empty
      case external : InjectorBindPointExternal => Set(external)
      case InjectionBindService(_,_,bindings) => bindings.flatMap( getExternalService(_))
    }
  }
}