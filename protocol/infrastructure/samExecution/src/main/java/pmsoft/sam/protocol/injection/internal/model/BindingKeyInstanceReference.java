package pmsoft.sam.protocol.injection.internal.model;

import com.google.inject.Key;

public class BindingKeyInstanceReference<T> extends AbstractInstanceReference {

	protected final Key<T> key;

	public BindingKeyInstanceReference(int instanceNr, Key<T> key) {
		super(instanceNr);
		this.key = key;
	}

	@Override
	public void visitToMergeOnInstanceRegistry(InstanceMergeVisitor api) {
		api.visitBindingKey(this);
	}

	public Key<T> getKey() {
		return key;
	}

	@Override
	public String toString() {
		return "#"+instanceNr+"->BindingKeyInstanceReference [key=" + key + ", instanceNr=" + instanceNr + "]";
	}
	
}
