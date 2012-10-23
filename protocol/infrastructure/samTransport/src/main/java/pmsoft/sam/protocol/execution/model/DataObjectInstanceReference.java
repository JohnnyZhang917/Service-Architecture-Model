package pmsoft.sam.protocol.execution.model;


public class DataObjectInstanceReference extends AbstractInstanceReference {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5920336088558722442L;

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
