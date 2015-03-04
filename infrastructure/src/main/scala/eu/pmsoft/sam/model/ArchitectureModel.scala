/*
 * Copyright (c) 2015. Paweł Cesar Sanjuan Szklarz.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

