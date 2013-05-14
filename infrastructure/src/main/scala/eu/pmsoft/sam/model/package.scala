package eu.pmsoft.sam

import eu.pmsoft.sam.definition.service.SamServiceDefinition
import eu.pmsoft.sam.definition.implementation.AbstractSamServiceImplementationDefinition
import com.google.inject.Module

package object model {

  type ServiceImplementationDefinition = AbstractSamServiceImplementationDefinition[_ <: SamServiceDefinition]

  type ServiceDefinition = SamServiceDefinition

  type ServiceContract = Class[_ <: SamServiceDefinition]

  type GuiceModule = Class[_ <: Module]
}
