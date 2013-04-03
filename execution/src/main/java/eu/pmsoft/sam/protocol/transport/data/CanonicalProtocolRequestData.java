package eu.pmsoft.sam.protocol.transport.data;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.google.common.base.Joiner;
import eu.pmsoft.sam.protocol.transport.CanonicalInstanceReference;
import eu.pmsoft.sam.protocol.transport.CanonicalMethodCall;
import eu.pmsoft.sam.protocol.transport.CanonicalRequest;

import java.util.List;

public class CanonicalProtocolRequestData {

    private final CanonicalRequest data;

    public CanonicalProtocolRequestData(byte[] payload) {
        CanonicalRequest d = new CanonicalRequest();
        ProtostuffIOUtil.mergeFrom(payload, d, CanonicalRequest.getSchema());
        this.data = d;
    }

    public CanonicalProtocolRequestData(CanonicalRequest data) {
        this.data = data;
    }

    public byte[] getPayload() {
        LinkedBuffer buffer = LinkedBuffer.allocate(512);
        try {
            return ProtostuffIOUtil.toByteArray(data, CanonicalRequest.getSchema(), buffer);
        } finally {
            buffer.clear();
        }
    }

    public boolean isCloseThread() {
        return data.getCloseThread();
    }

    public List<CanonicalInstanceReference> getInstanceReferences() {
        return data.getInstancesList();
    }

    public List<CanonicalMethodCall> getMethodCalls() {
        return data.getCallsList();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("CanonicalProtocolRequestData [\n");
        if (data.getInstancesList() != null) {
            buf.append("instanceReferences=[\n");
            Joiner.on("\n").appendTo(buf, data.getInstancesList());
            buf.append("\n]");

        }
        if (data.getCallsList() != null) {
            buf.append("methodCalls=[\n");
            Joiner.on("\n").appendTo(buf, data.getCallsList());
            buf.append("\n]");
        }
        return buf.toString();
    }
}
