package pmsoft.sam.protocol.transport.data;

import com.google.common.base.Objects;

import java.io.Serializable;
import java.util.List;

public class CanonicalProtocolRequestData implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 393248247046868614L;
    private final List<AbstractInstanceReference> instanceReferences;
    private final List<MethodCall> methodCalls;
    private final boolean closeThread;

    public CanonicalProtocolRequestData(List<AbstractInstanceReference> instanceReferences, List<MethodCall> methodCalls, boolean closeThread) {
        super();
        this.instanceReferences = instanceReferences;
        this.methodCalls = methodCalls;
        this.closeThread = closeThread;
    }

    public boolean isCloseThread() {
        return closeThread;
    }

    public List<AbstractInstanceReference> getInstanceReferences() {
        return instanceReferences;
    }

    public List<MethodCall> getMethodCalls() {
        return methodCalls;
    }

//    @Override
//	public String toString() {
//		return "CanonicalProtocolRequestData [\ninstanceReferences=[\n" + Joiner.on("\n").join(instanceReferences) + "]\nmethodCalls=[\n"
//				+ Joiner.on("\n").join(methodCalls) + "]\n";
//	}


    @Override
    public String toString() {
        return Objects.toStringHelper(this).omitNullValues()
                .add("instanceReferences", instanceReferences)
                .add("methodCalls", methodCalls)
                .add("closeThread", closeThread)
                .toString();
    }
}
