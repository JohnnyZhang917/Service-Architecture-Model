package pmsoft.sam.protocol.transport.data;

import com.google.common.base.Joiner;

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

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("CanonicalProtocolRequestData [\n");
        if (instanceReferences != null) {
            buf.append("instanceReferences=[\n");
            Joiner.on("\n").appendTo(buf, instanceReferences);
            buf.append("\n]");

        }
        if (methodCalls != null) {
            buf.append("methodCalls=[\n");
            Joiner.on("\n").appendTo(buf, methodCalls);
            buf.append("\n]");
        }
        return buf.toString();
    }
}
