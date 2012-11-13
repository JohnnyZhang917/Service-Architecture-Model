package pmsoft.sam.protocol.execution.model;


public class ClientDataObjectInstanceReference extends AbstractInstanceReference {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5920336088558722442L;

	public ClientDataObjectInstanceReference(int instanceNr, Object objectReference) {
		super(instanceNr);
		this.objectReference = objectReference;
	}

	private final Object objectReference;

	public Object getObjectReference() {
		return objectReference;
	}

	@Override
	public void visitToMergeOnInstanceRegistry(InstanceMergeVisitor api) {
		api.visitClientDataObjectInstance(this);
	}

	@Override
	public String toString() {
		return "#" + instanceNr + "->ClientDataObjectInstanceReference [ instanceNr=" + instanceNr + "]objectReference="
				+ objectReference;
	}

}
