package pmsoft.sam.inject.wrapper;

import com.google.inject.Key;

public interface InstanceProvider {

	public <T> T getInstance(Key<T> key);
}
