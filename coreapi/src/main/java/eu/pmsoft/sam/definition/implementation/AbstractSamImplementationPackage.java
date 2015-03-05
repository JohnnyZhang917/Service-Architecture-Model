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

public abstract class AbstractSamImplementationPackage implements
        SamServiceImplementationPackageContract {

    private SamServicePackageLoader reader;

    @Override
    public void loadContractPackage(SamServicePackageLoader reader) {
        try {
            this.reader = reader;
            packageDefinition();
        } finally {
            this.reader = null;
        }
    }

    public abstract void packageDefinition();

    protected final void registerImplementation(
            AbstractSamServiceImplementationDefinition<? extends SamServiceDefinition> serviceImplementationContract) {
        reader.registerImplementation(serviceImplementationContract);
    }

    protected final <T extends SamServiceDefinition> void registerImplementation(Class<T> serviceContract, Class<? extends Module> implementationModule){
        registerImplementation(new AbstractSamServiceImplementationDefinition<T>(serviceContract,implementationModule){});
    }

}
