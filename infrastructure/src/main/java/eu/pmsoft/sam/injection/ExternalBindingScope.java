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


import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

class ExternalBindingScope implements Scope, ExternalBindingController {

    private final ThreadLocal<ExternalInstanceProvider> perThreadServiceExecutionRecorder;

    ExternalBindingScope() {
        perThreadServiceExecutionRecorder = new ThreadLocal<ExternalInstanceProvider>();
    }

    public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
        return new Provider<T>() {
            @SuppressWarnings("unchecked")
            public T get() {
                ExternalInstanceProvider value = perThreadServiceExecutionRecorder.get();
                assert value != null;
                return (T) value;
            }
        };
    }

    @Override
    public void bindRecordContext(DependenciesBindingContext context) {
        perThreadServiceExecutionRecorder.set(context.getExternalInstanceProvider());
    }

    @Override
    public void unBindRecordContext() {
        perThreadServiceExecutionRecorder.remove();
    }

}
