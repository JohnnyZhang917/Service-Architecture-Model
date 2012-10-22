package pmsoft.sam.protocol.execution.model;


public abstract class AbstractInstanceReference {

	protected final int instanceNr;

	public AbstractInstanceReference(int instanceNr) {
		super();
		this.instanceNr = instanceNr;
	}

	public abstract void visitToMergeOnInstanceRegistry(InstanceMergeVisitor api);

	public int getInstanceNr() {
		return instanceNr;
	}
	
}
