package pmsoft.sam.protocol.transport.data;

import com.google.inject.Key;

public class ExternalSlotInstanceReference<T> extends BindingKeyInstanceReference<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7624452102317007090L;

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
