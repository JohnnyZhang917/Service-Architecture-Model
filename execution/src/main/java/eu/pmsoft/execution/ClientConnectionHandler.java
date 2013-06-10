package eu.pmsoft.execution;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;

import java.util.Map;

class ClientConnectionHandler extends ChannelInboundMessageHandlerAdapter<ThreadMessage> {

    private final Map<String, ThreadMessagePipeDeprecated> pipeRoutingMap = Maps.newConcurrentMap();

    void bindPipe(String signature, ThreadMessagePipeDeprecated pipe) {
        ThreadMessagePipeDeprecated previous = pipeRoutingMap.put(signature, pipe);
        assert previous == null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, ThreadMessage msg) throws Exception {
        ThreadMessage.ThreadProtocolMessageType messageType = msg.getMessageType();
        switch (messageType) {
            case INITIALIZE_TRANSACTION:
            case CLOSE_TRANSACTION:
                throw new IllegalStateException();
            case CANONICAL_PROTOCOL_EXECUTION:
                routeMessage(msg);
                break;
            case EXCEPTION_MESSAGE:
                throw new RuntimeException(new String(msg.getPayload()));
        }
    }

    private void routeMessage(ThreadMessage msg) {
        // TODO multiplex response on calls
        ThreadMessagePipeDeprecated clientPipe = pipeRoutingMap.get(msg.getSignature());
        Preconditions.checkNotNull(clientPipe);
        clientPipe.receiveMessage(msg);
    }
}
