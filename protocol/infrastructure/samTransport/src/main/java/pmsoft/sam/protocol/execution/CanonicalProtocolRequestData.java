package pmsoft.sam.protocol.execution;

import java.io.Serializable;
import java.util.List;

import pmsoft.sam.protocol.execution.model.AbstractInstanceReference;
import pmsoft.sam.protocol.execution.model.MethodCall;

import com.google.common.base.Joiner;

public class CanonicalProtocolRequestData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6013461254667743062L;
	private List<AbstractInstanceReference> instanceReferences;
	private List<MethodCall> methodCalls;

	public CanonicalProtocolRequestData(List<AbstractInstanceReference> instanceReferences, List<MethodCall> methodCalls) {
		super();
		this.instanceReferences = instanceReferences;
		this.methodCalls = methodCalls;
	}

	public List<AbstractInstanceReference> getInstanceReferences() {
		return instanceReferences;
	}

	public List<MethodCall> getMethodCalls() {
		return methodCalls;
	}

	@Override
	public String toString() {
		return "CanonicalProtocolRequestData [\ninstanceReferences=[\n" + Joiner.on("\n").join(instanceReferences) + "]\nmethodCalls=[\n"
				+ Joiner.on("\n").join(methodCalls) + "]\n";
	}

}
