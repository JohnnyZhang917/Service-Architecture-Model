package eu.pmsoft.inject;

import com.google.inject.Key;

import javax.inject.Provider;

public interface BindingProvider {

    <T> Provider<T> getProvider(Key<T> key);

}
