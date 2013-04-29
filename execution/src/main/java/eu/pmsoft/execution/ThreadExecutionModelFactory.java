package eu.pmsoft.execution;

import java.util.UUID;

interface ThreadExecutionModelFactory {

    ThreadExecutionContext threadExecutionContext(ThreadExecutionContextInternalLogic internalLogic,
//                                                  ThreadMessagePipe headCommandPipe,
//                                                  List<ThreadMessagePipe> endpoints,
                                                  UUID transactionID);

//    ThreadClientConnection openConnection(URL serverAddress);

//    ThreadMessagePipe createPipe(Channel connection, String signature, UUID transactionID, URL address);

//    ThreadExecutionServer server(InetSocketAddress serverAddress);
}
