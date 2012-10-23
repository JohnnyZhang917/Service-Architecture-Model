package pmsoft.sam.protocol.execution.model;

import java.io.Serializable;


public abstract class AbstractInstanceReference implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6208754835772242239L;
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
