package eu.pmsoft.execution;

import com.google.common.base.Objects;

import java.io.Serializable;
import java.net.URL;
import java.util.UUID;

public class ThreadMessage implements Serializable {

    public enum ThreadProtocolMessageType {
        INITIALIZE_TRANSACTION, CLOSE_TRANSACTION,
        CANONICAL_PROTOCOL_EXECUTION,
        EXCEPTION_MESSAGE
    }

    private UUID uuid;
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

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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
                .add("uuid", uuid)
                .add("targetUrl", targetUrl)
                .add("payload", payload)
                .add("signature", signature)
                .add("messageType", messageType)
                .toString();
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
