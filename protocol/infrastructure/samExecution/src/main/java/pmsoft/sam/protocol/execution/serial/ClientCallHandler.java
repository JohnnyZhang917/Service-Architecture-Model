package pmsoft.sam.protocol.execution.serial;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;

import java.util.logging.Level;
import java.util.logging.Logger;

import pmsoft.sam.protocol.execution.CanonicalProtocolRequest;
import pmsoft.sam.protocol.execution.CanonicalProtocolRequestData;

public class ClientCallHandler extends ChannelInboundMessageHandlerAdapter<CanonicalProtocolRequestData> {

	private static final Logger logger = Logger.getLogger(ClientCallHandler.class.getName());

	private final CanonicalProtocolRequest request;
	private CanonicalProtocolRequestData responce;

	public ClientCallHandler(CanonicalProtocolRequest request) {
		super();
		this.request = request;
	}

	public CanonicalProtocolRequestData getResponce() {
		return responce;
	}

	public void setResponce(CanonicalProtocolRequestData responce) {
		this.responce = responce;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.write(request);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, CanonicalProtocolRequestData responce) throws Exception {
		this.responce = responce;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.log(Level.WARNING, "Unexpected exception from downstream.", cause);
		ctx.close();
	}
}