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

package eu.pmsoft.sam.injection;


import com.google.inject.PrivateModule;
import com.google.inject.Provider;

public class ExtrenalBindingInfrastructureModule extends PrivateModule {

    /**
     * ExternalInstanceProvider is injected to the injection context using the ExternalBindingController interface.
     * The scope ExternalBindingScope implements ExternalBindingController.
     * <p/>
     * Guice requires a provider to be passed, so using this empty one.
     */
    private static final Provider<ExternalInstanceProvider> NOT_USED_PROVIDER = new Provider<ExternalInstanceProvider>() {
        @Override
        public ExternalInstanceProvider get() {
            throw new RuntimeException("use of this provider is not possible");
        }
    };

    @Override
    protected void configure() {
        ExternalBindingScope infrastructureScope = new ExternalBindingScope();
        bind(ExternalBindingController.class).toInstance(infrastructureScope);
        bind(ExternalInstanceProvider.class).toProvider(NOT_USED_PROVIDER).in(infrastructureScope);
        expose(ExternalBindingController.class);
        expose(ExternalInstanceProvider.class);
        binder().requireExplicitBindings();
    }

}
