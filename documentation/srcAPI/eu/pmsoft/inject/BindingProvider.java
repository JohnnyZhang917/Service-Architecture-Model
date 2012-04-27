package eu.pmsoft.inject;

import javax.inject.Provider;

import com.google.inject.Key;

public interface BindingProvider {

	<T> Provider<T> getProvider(Key<T> key);

}
