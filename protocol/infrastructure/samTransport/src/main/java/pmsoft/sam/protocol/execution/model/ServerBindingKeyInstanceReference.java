package pmsoft.sam.protocol.execution.model;

import com.google.inject.Key;

public class ServerBindingKeyInstanceReference<T> extends BindingKeyInstanceReference<T> {

	public ServerBindingKeyInstanceReference(int instanceNr, Key<T> key) {
		super(instanceNr, key);
	}

	@Override
	public void visitToMergeOnInstanceRegistry(InstanceMergeVisitor api) {
		api.visitServerBindingKeyInstanceReference(this);
	}

	@Override
	public String toString() {
		return "#"+instanceNr+"->ServerBindingKeyInstanceReference [key=" + key + ", instanceNr=" + instanceNr + "]";
	}
	
	
	
}
