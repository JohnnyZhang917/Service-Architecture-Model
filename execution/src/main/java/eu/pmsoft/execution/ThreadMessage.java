package eu.pmsoft.execution;

import com.google.common.base.Objects;

import java.io.Serializable;
import java.net.URL;
import java.util.UUID;

public class ThreadMessage implements Serializable {

    private static final long serialVersionUID = 4155532844270180398L;

    public enum ThreadProtocolMessageType {
        INITIALIZE_TRANSACTION, CLOSE_TRANSACTION,
        CANONICAL_PROTOCOL_EXECUTION,
        EXCEPTION_MESSAGE
    }

    private UUID transactionId;
    private URL sourceUrl;
    private URL targetUrl;
    private byte[] payload;
    private String signature;
    private ThreadProtocolMessageType messageType;

    public ThreadMessage() {
    }

    public ThreadProtocolMessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(ThreadProtocolMessageType messageType) {
        this.messageType = messageType;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public URL getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(URL targetUrl) {
        this.targetUrl = targetUrl;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).omitNullValues()
                .add("transactionId", transactionId)
                .add("targetUrl", targetUrl)
                .add("payload", payload)
                .add("signature", signature)
                .add("messageType", messageType)
                .toString();
    }

    public String getSignature() {
        return signature;
    }

    public URL getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(URL sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
