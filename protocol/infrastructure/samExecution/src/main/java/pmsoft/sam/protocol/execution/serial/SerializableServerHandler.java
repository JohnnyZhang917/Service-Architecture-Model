package pmsoft.sam.protocol.execution.serial;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import pmsoft.sam.protocol.execution.CanonicalProtocolExecutionServiceProviderApi;
import pmsoft.sam.protocol.execution.CanonicalProtocolRequest;

import com.google.inject.Inject;

public class SerializableServerHandler extends ChannelInboundMessageHandlerAdapter<CanonicalProtocolRequest> {

	private final CanonicalProtocolExecutionServiceProviderApi handler;
	
	@Inject
	public SerializableServerHandler(CanonicalProtocolExecutionServiceProviderApi handler) {
		this.handler = handler;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, CanonicalProtocolRequest msg) throws Exception {
		System.out.println(msg);
		ctx.write(handler.handleCanonicalRequest(msg));
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("Unexpected exception in SerializableServerHandler.");
		ctx.close();
	}
}
