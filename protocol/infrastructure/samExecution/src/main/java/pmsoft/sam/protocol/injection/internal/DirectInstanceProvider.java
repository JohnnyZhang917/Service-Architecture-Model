package pmsoft.sam.protocol.injection.internal;

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
public class DirectInstanceProvider implements InstanceProvider {

	private final Injector directServiceInjector;
	private final ImmutableSet<Key<?>> serviceApiKeys;
	
	public DirectInstanceProvider(Injector directServiceInjector, Set<Key<?>> serviceApiKeys) {
		this.directServiceInjector = directServiceInjector;
		this.serviceApiKeys = ImmutableSet.copyOf(serviceApiKeys);
	}

	@Override
	public <T> T getInstance(Key<T> key) {
		Preconditions.checkState(serviceApiKeys.contains(key),"This service don't provide a implementation of this key. Routing error.");
		return directServiceInjector.getInstance(key);
	}

}
