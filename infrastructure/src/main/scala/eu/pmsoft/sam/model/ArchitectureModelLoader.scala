package eu.pmsoft.sam.model

import eu.pmsoft.sam.architecture.definition.{SamArchitectureDefinition, SamArchitectureLoader}
import eu.pmsoft.sam.architecture.definition.SamArchitectureLoader.SamCategoryLoader
import eu.pmsoft.sam.definition.service.{SamServiceDefinitionLoader, SamServiceDefinition}
import com.google.inject.Key
import java.lang.annotation.Annotation
import eu.pmsoft.sam.architecture.exceptions.IncorrectArchitectureDefinition


object ArchitectureModelLoader {
  def loadArchitectureModel(definition: SamArchitectureDefinition): SamArchitectureObject = {
    val loader = new ArchitectureModelLoader()
    definition.loadArchitectureDefinition(loader)
    loader.createModel
  }
}

class ArchitectureModelLoader extends SamArchitectureLoader {

  private def createModel: SamArchitectureObject = {
    if (categories.isEmpty) throw new IncorrectArchitectureDefinition("empty architecture")
    val architectureCategories = categories.map(c => (c._1, SamCategoryObject(c._1, c._2.buildServices)))
    val accessMap = categories.map(c => (c._1, c._2.accessibleCategories))
    val selfAccess: Map[String, Set[String]] = accessMap.filter(a => a._2.contains(a._1))
    if (!selfAccess.isEmpty) throw new IncorrectArchitectureDefinition("Categories with self-access:%s".format(selfAccess.keys.mkString(":")))
    SamArchitectureObject(architectureCategories, accessMap)
  }

  var categories: Map[String, SamCategoryModelLoader] = Map.empty

  def createCategory(categoryId: String): SamCategoryLoader = {
    categories.get(categoryId) match {
      case Some(loader) => throw new IncorrectArchitectureDefinition("Duplicate creation of category %s".format(categoryId))
      case None => {
        val loader = new SamCategoryModelLoader(categoryId)
        categories = categories updated(categoryId, loader)
        loader
      }
    }
  }
}

class SamCategoryModelLoader(val categoryId: String) extends SamCategoryLoader {
  var accessibleCategories: Set[String] = Set.empty


  def buildServices: Set[SamServiceObject] = {
    null
  }

  def withService(serviceDefinition: SamServiceDefinition): SamCategoryLoader = {
//    serviceDefinition.loadServiceDefinition(serviceLoader)
//    serviceDefinitionLoaders += serviceLoader
    this
  }

  def accessToCategory(accesibleCategory: SamCategoryLoader): SamCategoryLoader = {
    accessibleCategories += accesibleCategory.getCategoryId
    this
  }

  def getCategoryId: String = categoryId
}





