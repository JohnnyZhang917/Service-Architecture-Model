package pmsoft.sam.protocol.record;

import java.util.Set;


import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * Provide instances using directly a service injector.
 * 
 * Service contract is verified on keys level.
 * @author pawel
 *
 */ 
class DirectInstanceProvider implements InstanceProvider {

	private final Injector directServiceInjector;
	private final ImmutableSet<Key<?>> serviceApiKeys;
	
	DirectInstanceProvider(Injector directServiceInjector, Set<Key<?>> serviceApiKeys) {
		this.directServiceInjector = directServiceInjector;
		this.serviceApiKeys = ImmutableSet.copyOf(serviceApiKeys);
	}

	@Override
	public <T> T getInstance(Key<T> key) {
		Preconditions.checkState(serviceApiKeys.contains(key),"This service don't provide a implementation of this key. Routing exceptions.");
		return directServiceInjector.getInstance(key);
	}

}
