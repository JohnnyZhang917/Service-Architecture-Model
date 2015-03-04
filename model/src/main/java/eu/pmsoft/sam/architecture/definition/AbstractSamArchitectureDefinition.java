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

package eu.pmsoft.sam.architecture.definition;

import eu.pmsoft.sam.architecture.definition.SamArchitectureLoader.SamCategoryLoader;

/**
 * Definition of Architecture is contained in one subclass of AbstractSamArchitectureDefinition.
 *
 * Definitions for categories and access relation is provided. ServiceDefinition are defined externally and linked with Class object.
 *
 * In future for more complex architectures the definition mechanism may change.
 *
 * Architecture definition is not necessary to execute serviceInstance.
 *
 * @author pawel
 */
public abstract class AbstractSamArchitectureDefinition implements SamArchitectureDefinition {

    private final String architectureSignature;
    private SamArchitectureLoader loaderRef;

    protected AbstractSamArchitectureDefinition(String architectureSignature) {
        this.architectureSignature = architectureSignature;
    }

    protected AbstractSamArchitectureDefinition() {
        this.architectureSignature = this.getClass().getCanonicalName();
    }

    protected abstract void loadArchitectureDefinition();

    protected SamCategoryLoader createCategory(String categoryName) {
        return loaderRef.createCategory(categoryName);
    }

    public final void loadArchitectureDefinition(SamArchitectureLoader loader) {
        try {
            this.loaderRef = loader;
            this.loaderRef.architectureSignature(architectureSignature);
            loadArchitectureDefinition();
        } finally {
            this.loaderRef = null;
        }
    }

}
