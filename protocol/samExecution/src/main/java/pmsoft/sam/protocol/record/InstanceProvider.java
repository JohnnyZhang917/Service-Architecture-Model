package pmsoft.sam.protocol.record;

import com.google.inject.Key;

interface InstanceProvider {

    public <T> T getInstance(Key<T> key);
}
