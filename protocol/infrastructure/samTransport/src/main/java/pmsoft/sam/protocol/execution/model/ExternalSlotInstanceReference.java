package pmsoft.sam.protocol.execution.model;

import com.google.inject.Key;

public class ExternalSlotInstanceReference<T> extends BindingKeyInstanceReference<T> {

	public ExternalSlotInstanceReference(int instanceNr, Key<T> key) {
		super(instanceNr, key);
	}


	@Override
	public void visitToMergeOnInstanceRegistry(InstanceMergeVisitor api) {
		api.visitExternalSlotInstance(this);
	}

	@Override
	public String toString() {
		return "#"+instanceNr+"->ExternalSlotInstanceReference [key=" + key + ", instanceNr=" + instanceNr + "]";
	}

}