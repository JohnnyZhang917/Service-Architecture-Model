package pmsoft.sam.canonical.deprecated.api.ir.model;

import pmsoft.sam.canonical.deprecated.api.ir.InstanceMergeVisitor;

public class DataObjectInstanceReference extends AbstractInstanceReference {

	public DataObjectInstanceReference(int instanceNr, Object objectReference) {
		super(instanceNr);
		this.objectReference = objectReference;
	}

	private final Object objectReference;

	public Object getObjectReference() {
		return objectReference;
	}

	@Override
	public void visitToMergeOnInstanceRegistry(InstanceMergeVisitor api) {
		api.visitDataObjectInstance(this);
	}

	@Override
	public String toString() {
		return "#" + instanceNr + "->DataObjectInstanceReference [ instanceNr=" + instanceNr + "]objectReference="
				+ objectReference;
	}

}
