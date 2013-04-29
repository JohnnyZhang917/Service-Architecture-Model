package eu.pmsoft.sam.see.transport.nettyrefactor;

import eu.pmsoft.execution.ThreadMessage;
import eu.pmsoft.injectionUtils.logger.InjectLogger;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import org.slf4j.Logger;

import javax.inject.Inject;

class SamTransportServerConnectionHandler extends ChannelInboundMessageHandlerAdapter<ThreadMessage> {

    @InjectLogger
    private Logger logger;

    @Inject
    public SamTransportServerConnectionHandler(){
        System.out.println("!!!!!!!!!!!init server handler");
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, ThreadMessage msg) throws Exception {
        System.out.println("server handler get data");
//        ctx.write(msg).addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture future) throws Exception {
//                if( ! future.isSuccess() ) {
//
//                }
//            }
//        });
//        throw new RuntimeException("TODO");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.channel().close();
        throw new RuntimeException(cause);
    }

}
