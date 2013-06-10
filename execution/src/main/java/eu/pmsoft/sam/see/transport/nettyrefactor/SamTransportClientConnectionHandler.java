package eu.pmsoft.sam.see.transport.nettyrefactor;

import eu.pmsoft.execution.ThreadMessage;
import eu.pmsoft.injectionUtils.logger.InjectLogger;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import org.slf4j.Logger;

import javax.inject.Inject;

class SamTransportClientConnectionHandler extends ChannelInboundMessageHandlerAdapter<ThreadMessage> {

    @InjectLogger
    private Logger logger;

    @Inject
    SamTransportClientConnectionHandler() {
        System.out.println("!!!!!!!!!!!init client handler");
    }


    @Override
    public void messageReceived(ChannelHandlerContext ctx, ThreadMessage msg) throws Exception {
        System.out.println("client handler get data");
//        ctx.write(msg);
//        throw new RuntimeException("TODO");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.channel().close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleState) {
            IdleState e = (IdleState) evt;
            if (e == IdleState.READER_IDLE) {
                ctx.channel().close();
            } else if (e == IdleState.WRITER_IDLE) {
                ctx.channel().close();
            }
        }
    }
}
