package pmsoft.sam.protocol.injection.internal;

import java.util.List;

import com.google.common.base.Joiner;

import pmsoft.sam.protocol.injection.internal.model.AbstractInstanceReference;

public class CanonicalProtocolRequestData {

	private List<AbstractInstanceReference> instanceReferences;
	private List<MethodCall> methodCalls;
	private int targetSlot;

	public CanonicalProtocolRequestData(List<AbstractInstanceReference> instanceReferences, List<MethodCall> methodCalls,
			int targetSlot) {
		super();
		this.instanceReferences = instanceReferences;
		this.methodCalls = methodCalls;
		this.targetSlot = targetSlot;
	}

	public List<AbstractInstanceReference> getInstanceReferences() {
		return instanceReferences;
	}

	public List<MethodCall> getMethodCalls() {
		return methodCalls;
	}

	public int getTargetSlot() {
		return targetSlot;
	}

	@Override
	public String toString() {
		return "CanonicalProtocolRequestData [\ninstanceReferences=[\n" + Joiner.on("\n").join(instanceReferences)
				+ "]\nmethodCalls=[\n" + Joiner.on("\n").join(methodCalls) + "]\ntargetSlot=" + targetSlot + "]\n";
	}

}
