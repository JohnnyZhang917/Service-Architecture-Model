package eu.pmsoft.execution;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.List;
import java.util.UUID;

interface ThreadExecutionModelFactory {

    ThreadExecutionContext threadExecutionContext(ThreadExecutionContextInternalLogic internalLogic, ThreadMessagePipe headCommandPipe,
                                                  List<ThreadMessagePipe> endpoints, UUID transactionID);

    ThreadClientConnection openConnection(URL serverAddress);

    ThreadMessagePipe createPipe(Channel connection, String signature, UUID transactionID, URL address);

//    ThreadExecutionServer server(InetSocketAddress serverAddress);
}
