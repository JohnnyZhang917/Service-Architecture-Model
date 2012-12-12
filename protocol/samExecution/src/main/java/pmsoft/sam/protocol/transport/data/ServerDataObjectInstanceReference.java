package pmsoft.sam.protocol.transport.data;


public class ServerDataObjectInstanceReference extends AbstractInstanceReference {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5920336088558722442L;

	public ServerDataObjectInstanceReference(int instanceNr, Object objectReference) {
		super(instanceNr);
		this.objectReference = objectReference;
	}

	private final Object objectReference;

	public Object getObjectReference() {
		return objectReference;
	}

	@Override
	public void visitToMergeOnInstanceRegistry(InstanceMergeVisitor api) {
		api.visitServerDataObjectInstance(this);
	}

	@Override
	public String toString() {
		return "#" + instanceNr + "->ServerDataObjectInstanceReference [ instanceNr=" + instanceNr + "]objectReference="
				+ objectReference;
	}

}
