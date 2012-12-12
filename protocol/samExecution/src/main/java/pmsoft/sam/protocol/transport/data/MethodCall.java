package pmsoft.sam.protocol.transport.data;

import java.io.Serializable;
import java.lang.reflect.Method;

public class MethodCall implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4747253868996529674L;

	private final int serviceSlotNr;

	private final int methodSignature;
	private final int instanceNr;
	private final int returnInstanceId;
	private int[] argumentReferences;
	
	// Just for protocol dumping
	private final String methodName;

	public MethodCall(Method method, int serviceInstanceNr, int returnId, int serviceSlotNr) {
		super();
		this.methodSignature = method.hashCode();
		this.instanceNr = serviceInstanceNr;
		this.returnInstanceId = returnId;
		this.serviceSlotNr = serviceSlotNr;
		this.methodName = method.getName();
	}

	public int[] getArgumentReferences() {
		return argumentReferences;
	}

	public void setArgumentReferences(int[] argumentReferences) {
		this.argumentReferences = argumentReferences;
	}

	public int getServiceSlotNr() {
		return serviceSlotNr;
	}

	public int getMethodSignature() {
		return methodSignature;
	}

	public int getInstanceNr() {
		return instanceNr;
	}

	public int getReturnInstanceId() {
		return returnInstanceId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if( argumentReferences != null) {
			for (int i = 0; i < argumentReferences.length; i++) {
				builder.append("#").append(argumentReferences[i]);
			}			
		}
		return "MethodCall["+serviceSlotNr+"][#"+instanceNr+"."+methodName+"("+builder.toString()+")#"+returnInstanceId+"]";
	}

}
