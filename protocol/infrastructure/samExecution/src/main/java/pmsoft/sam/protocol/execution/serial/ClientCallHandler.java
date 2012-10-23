package pmsoft.sam.protocol.execution.serial;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import pmsoft.sam.protocol.execution.CanonicalProtocolRequest;
import pmsoft.sam.protocol.execution.CanonicalProtocolRequestData;

public class ClientCallHandler extends ChannelInboundMessageHandlerAdapter<CanonicalProtocolRequestData> {

	private static final Logger logger = Logger.getLogger(ClientCallHandler.class.getName());

    final BlockingQueue<CanonicalProtocolRequestData> answer = new LinkedBlockingQueue<CanonicalProtocolRequestData>();

	private final CanonicalProtocolRequest request;

	public ClientCallHandler(CanonicalProtocolRequest request) {
		super();
		this.request = request;
	}

	public CanonicalProtocolRequestData getResponce() {
        boolean interrupted = false;
        for (;;) {
            try {
            	CanonicalProtocolRequestData responce = answer.take();
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
                return responce;
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
	}

	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.write(request);
		ctx.flush();
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, final CanonicalProtocolRequestData responce) throws Exception {
		// Offer the answer after closing the connection.
        ctx.channel().close().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                boolean offered = answer.offer(responce);
                assert offered;
            }
        });
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.log(Level.WARNING, "Unexpected exception in ClientCallHandler.", cause);
		ctx.close();
	}
}