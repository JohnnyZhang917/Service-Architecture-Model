package pmsoft.execution;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import pmsoft.injectionUtils.logger.InjectLogger;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class ThreadMessagePipe {

    private final ConcurrentLinkedQueue<ThreadMessage> messageInputQueue = new ConcurrentLinkedQueue<ThreadMessage>();
    private final Channel connection;
    // TODO create multiple wait strategies
    private final Object waitMonitor = new Object();
    private final String signature;
    private final UUID transactionID;
    private final URL address;
    @InjectLogger
    private Logger logger;

    @Inject
    ThreadMessagePipe(@Assisted Channel connection, @Assisted String signature, @Nullable @Assisted UUID transactionID, @Nullable @Assisted URL address) {
        super();
        this.connection = connection;
        this.signature = signature;
        this.transactionID = transactionID;
        this.address = address;
    }

    public void receiveMessage(ThreadMessage message) {
        logger.debug("receive message: {}", signature);
        messageInputQueue.add(message);
        synchronized (waitMonitor) {
            logger.trace("notify on : {}", signature);
            waitMonitor.notify();
        }
    }

    public ThreadMessage pollMessage() {
        return messageInputQueue.poll();
    }

    public void sendMessage(ThreadMessage message) {
        logger.debug("sending message: {}\n{}", signature, message);
        message.setSignature(signature);
        message.setMessageType(ThreadMessage.ThreadProtocolMessageType.CANONICAL_PROTOCOL_EXECUTION);
        connection.write(message);
    }

    public ThreadMessage waitResponse() {
        logger.trace("start wait for response: {}", signature);
        synchronized (waitMonitor) {
            while (messageInputQueue.isEmpty()) {
                try {
                    waitMonitor.wait();
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
        return messageInputQueue.poll();
    }

    public String getSignature() {
        return signature;
    }

    public void initializeTransactionConnection() {
        ThreadMessage initConnectionMessage = new ThreadMessage();
        initConnectionMessage.setUuid(transactionID);
        initConnectionMessage.setSignature(signature);
        initConnectionMessage.setTargetUrl(address);
        initConnectionMessage.setMessageType(ThreadMessage.ThreadProtocolMessageType.INITIALIZE_TRANSACTION);
        logger.trace("Sending initialization pipe binding message {}", initConnectionMessage);
        connection.write(initConnectionMessage);
    }

    public void closeTransactionConnection() {
        ThreadMessage closeTransactionMessage = new ThreadMessage();
        closeTransactionMessage.setSignature(signature);
        closeTransactionMessage.setMessageType(ThreadMessage.ThreadProtocolMessageType.CLOSE_TRANSACTION);
        synchronized (waitMonitor) {
            connection.write(closeTransactionMessage);
            logger.debug("notify for dead connection : {}", signature);
            waitMonitor.notify();
        }
    }
}
