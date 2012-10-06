package pmsoft.sam.canonical.deprecated;

import pmsoft.sam.canonical.deprecated.api.ir.InstanceRegistry;
import pmsoft.sam.protocol.injection.InstanceProvider;

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
