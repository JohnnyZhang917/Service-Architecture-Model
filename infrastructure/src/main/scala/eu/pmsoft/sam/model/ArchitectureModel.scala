package eu.pmsoft.sam.model

import eu.pmsoft.sam.architecture.definition.{SamArchitectureDefinition, SamArchitectureLoader}
import eu.pmsoft.sam.architecture.definition.SamArchitectureLoader.SamCategoryLoader
import eu.pmsoft.sam.definition.service.{SamServiceLoaderBase, SamServiceDefinition}
import com.google.inject.Key
import java.lang.annotation.Annotation
import eu.pmsoft.sam.architecture.exceptions.IncorrectArchitectureDefinition
import eu.pmsoft.sam.architecture.model.{SamCategory, SamArchitecture}
import eu.pmsoft.sam.architecture.model
import java.util


sealed abstract class SamArchitectureEntry
case class ServiceKey(signature:Class[_ <: SamServiceDefinition]) extends SamArchitectureEntry

case class SamServiceObject(key:ServiceKey, contract:Iterable[Key[_]]) extends SamArchitectureEntry
case class SamCategoryObject(id:String , services:Set[SamServiceObject]) extends SamArchitectureEntry
case class SamArchitectureObject(categories: Map[String,SamCategoryObject], accessMap : Map[String,Set[String]]) extends SamArchitectureEntry


object ArchitectureModelLoader {
  def loadArchitectureModel(definition : SamArchitectureDefinition) : SamArchitectureObject = {
    val loader = new ArchitectureModelLoader()
    definition.loadArchitectureDefinition(loader)
    loader.createModel
  }
}

class ArchitectureModelLoader extends SamArchitectureLoader {

  private def createModel : SamArchitectureObject = {
    if(categories.isEmpty) throw new IncorrectArchitectureDefinition("empty architecture")
    val architectureCategories = categories.map( c =>  (c._1,SamCategoryObject(c._1,c._2.buildServices)) )
    val accessMap = categories.map( c => (c._1,c._2.accessibleCategories))
    val selfAccess: Map[String, Set[String]] = accessMap.filter(a => a._2.contains(a._1))
    if( ! selfAccess.isEmpty ) throw new IncorrectArchitectureDefinition("Categories with self-access:%s".format(selfAccess.keys.mkString(":")))
    SamArchitectureObject(architectureCategories,accessMap)
  }

  var categories : Map[String,SamCategoryModelLoader] = Map.empty

  def createCategory(categoryId: String): SamCategoryLoader = {
    categories.get(categoryId) match {
      case Some(loader) => throw new IncorrectArchitectureDefinition("Duplicate creation of category %s".format(categoryId))
      case None => {
        val loader = new SamCategoryModelLoader(categoryId)
        categories = categories updated (categoryId,loader)
        loader
      }
    }
  }
}

class SamCategoryModelLoader(val categoryId : String) extends SamCategoryLoader {
  var accessibleCategories : Set[String] = Set.empty
  var serviceDefinitionLoaders : Set[SamServiceLoaderBaseImpl] = Set.empty

  def buildServices : Set[SamServiceObject] = {
    if( serviceDefinitionLoaders.isEmpty ) throw new IncorrectArchitectureDefinition("empty category %s".format(categoryId))
    serviceDefinitionLoaders map { _.buildService }
  }

  def withService(serviceDefinition: SamServiceDefinition): SamCategoryLoader = {
    val serviceLoader =  new SamServiceLoaderBaseImpl()
    serviceDefinition.loadServiceDefinition(serviceLoader)
    serviceDefinitionLoaders += serviceLoader
    this
  }

  def accessToCategory(accesibleCategory: SamCategoryLoader): SamCategoryLoader = {
    accessibleCategories += accesibleCategory.getCategoryId
    this
  }

  def getCategoryId: String = categoryId
}

class SamServiceLoaderBaseImpl extends SamServiceLoaderBase {
  var realLoader : Option[SamServiceLoaderImpl] = None

  def setupLoadContext(definition: Class[_ <: SamServiceDefinition]) = realLoader.getOrElse( {
    realLoader = Some(new SamServiceLoaderImpl(definition))
    realLoader.get
  })

  def buildService  = {
    val loader = realLoader.get
    SamServiceObject(ServiceKey(loader.definitionClass),loader.serviceKeys)
  }

}

class SamServiceLoaderImpl(val definitionClass : Class[_ <: SamServiceDefinition]) extends SamServiceLoaderBase.SamServiceLoader {
  var serviceKeys : Set[Key[_]] = Set.empty
  def addInterface(keytype: Class[_]) = {
    serviceKeys += Key.get(keytype)
  }
  def addInterface(keytype: Class[_], annotation: Annotation) = {
    serviceKeys += Key.get(keytype,annotation)
  }
}





