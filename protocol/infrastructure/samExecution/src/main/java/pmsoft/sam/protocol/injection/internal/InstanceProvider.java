package pmsoft.sam.protocol.injection.internal;

import com.google.inject.Key;

interface InstanceProvider {

	public <T> T getInstance(Key<T> key);
}
