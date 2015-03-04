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

package eu.pmsoft.sam.definition.implementation;

import com.google.inject.Module;
import eu.pmsoft.sam.definition.service.SamServiceDefinition;

public abstract class AbstractSamServiceImplementationDefinition<T extends SamServiceDefinition> implements SamServiceImplementationDefinition<T> {

    private final Class<T> serviceContract;
    private final Class<? extends Module> implementationModule;
    private SamServiceImplementationDefinitionLoader.ContractAndModule definitionLoader;

    protected AbstractSamServiceImplementationDefinition(Class<T> serviceContract, Class<? extends Module> implementationModule) {
        this.serviceContract = serviceContract;
        this.implementationModule = implementationModule;
    }

    @Override
    public final void loadServiceImplementationDefinition(SamServiceImplementationDefinitionLoader loader) {
        try {
            this.definitionLoader = loader.signature(serviceContract, implementationModule);
            implementationDefinition();
        } finally {
            this.definitionLoader = null;
        }
    }

    protected void implementationDefinition() {

    }

    protected final void withBindingsTo(Class<? extends SamServiceDefinition> userService) {
        definitionLoader.withBindingsTo(userService);
    }

}
