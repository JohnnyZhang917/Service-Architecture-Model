package eu.pmsoft.execution;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import eu.pmsoft.injectionUtils.logger.InjectLogger;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import org.slf4j.Logger;

import java.util.Map;

class ProviderConnectionHandler extends ChannelInboundMessageHandlerAdapter<ThreadMessage> {

    @InjectLogger
    private Logger logger;
    private final ExecutionContextManager contextManager;
    private final ThreadExecutionManager executionManager;
    // TODO make something more efficient to multiplex the connection
    private Map<String, ThreadExecutionContext> transactionBinding = Maps.newHashMap();

    @Inject
    ProviderConnectionHandler(ExecutionContextManager contextManager, ThreadExecutionManager executionManager) {
        this.contextManager = contextManager;
        this.executionManager = executionManager;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, ThreadMessage msg) throws Exception {
        try {
            ThreadMessage.ThreadProtocolMessageType messageType = msg.getMessageType();
            switch (messageType) {
                case INITIALIZE_TRANSACTION:
                    initializeTransaction(ctx, msg);
                    break;
                case CLOSE_TRANSACTION:
                    closeTransaction(ctx, msg);
                    break;
                case CANONICAL_PROTOCOL_EXECUTION:
                    execute(msg);
                    break;
            }
        } catch (Exception anyException) {
            ThreadMessage exception = new ThreadMessage();
            exception.setMessageType(ThreadMessage.ThreadProtocolMessageType.EXCEPTION_MESSAGE);
            // TODO pass exceptions on the protocol for every level of execution.
            exception.setPayload(anyException.toString().getBytes());
            ctx.write(exception);
            throw anyException;
        }
    }

    private void execute(ThreadMessage msg) {
        Preconditions.checkState(transactionBinding.containsKey(msg.getSignature()));
        ThreadExecutionContext context = transactionBinding.get(msg.getSignature());
        ThreadMessagePipe head = context.getHeadCommandPipe();
        head.receiveMessage(msg);
        if (!context.isExecutionRunning()) {
            executionManager.runExecutionContext(context);
        }
    }

    private void closeTransaction(ChannelHandlerContext ctx, ThreadMessage msg) {
        // FIXME close of transaction have some errors
        Preconditions.checkState(transactionBinding.containsKey(msg.getSignature()));
        ThreadExecutionContext context = transactionBinding.remove(msg.getSignature());
//        context.exitGrobalTransactionContext();
        // this is a normal stop for the transaction, so expect it not running any more
        Preconditions.checkState(!context.isExecutionRunning());
    }

    private void initializeTransaction(ChannelHandlerContext ctx, ThreadMessage msg) {
        ThreadExecutionContext context;
        Preconditions.checkState(!transactionBinding.containsKey(msg.getSignature()));
        logger.trace("initializeTransaction. Create context on base of message {}", msg);
        // the pipe signature is used as key, because a transaction can have multiple service instance running in one server node
        context = contextManager.openExecutionContextForProtocolExecution(msg.getSignature(), msg.getUuid(), msg.getTargetUrl(), ctx.channel());
        context.initGrobalTransactionContext();
        transactionBinding.put(msg.getSignature(), context);
    }

}
