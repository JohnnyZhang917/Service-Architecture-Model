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

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ExternalBindingSwitch<T> implements InvocationHandler {

    private final Key<T> key;
    private final int instanceReferenceNr;
    private final int slotNr;

    @Inject
    private Provider<ExternalInstanceProvider> providerExternalInstanceProvider;

    @SuppressWarnings("unchecked")
    public ExternalBindingSwitch(Key<T> key, int instanceReferenceNr, int slotNr) {
        Class<? super T> mainClass = key.getTypeLiteral().getRawType();
        this.recordReference = (T) Proxy.newProxyInstance(mainClass.getClassLoader(), new Class<?>[]{mainClass}, this);
        this.instanceReferenceNr = instanceReferenceNr;
        this.slotNr = slotNr;
        this.key = key;
    }

    private T recordReference;

    public T getReferenceObject() {
        return recordReference;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        T internalReference = getInternalInstance();
        return method.invoke(internalReference, args);
    }

    public T getInternalInstance() {
        ExternalInstanceProvider externalProvider = providerExternalInstanceProvider.get();
        T internalReference = externalProvider.getReference(slotNr, key, instanceReferenceNr);
        return internalReference;
    }
}
