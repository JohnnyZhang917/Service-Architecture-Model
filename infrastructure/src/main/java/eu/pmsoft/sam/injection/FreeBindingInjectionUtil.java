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

import com.google.inject.*;

public class FreeBindingInjectionUtil {

    public final static <T> void createIntermediateProvider(PrivateBinder binder, final Key<T> key, final int slotNr) {
        Provider<T> provider = new Provider<T>() {
            @Inject
            private Injector injector;
            private volatile int instanceNr = 0;

            public T get() {
                ExternalBindingSwitch<T> intermediate = new ExternalBindingSwitch<T>(key, instanceNr++, slotNr);
                injector.injectMembers(intermediate);
                return intermediate.getReferenceObject();
            }
        };
        binder.bind(key).toProvider(provider);
        binder.expose(key);
    }

    public final static <T> void createGlueBinding(Binder binder, Key<T> key, Injector realInjector) {
        binder.bind(key).toProvider(realInjector.getProvider(key));
    }


}
