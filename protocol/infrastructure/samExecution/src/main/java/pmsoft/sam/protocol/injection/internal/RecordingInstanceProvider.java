package pmsoft.sam.protocol.injection.internal;

import com.google.inject.Key;

public class RecordingInstanceProvider implements InstanceProvider {

	private final InstanceRegistry instanceRegistry;

	public RecordingInstanceProvider(InstanceRegistry instanceRegistry) {
		super();
		this.instanceRegistry = instanceRegistry;
	}

	public <T> T getInstance(Key<T> key) {
		return instanceRegistry.createKeyBinding(key).getInstance();
	}

}
