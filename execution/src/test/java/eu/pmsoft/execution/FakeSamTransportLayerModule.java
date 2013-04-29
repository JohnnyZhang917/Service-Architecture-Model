package eu.pmsoft.execution;

@Deprecated
public class FakeSamTransportLayerModule {
//
//    @Override
//    protected void configure() {
//        bind(SamTransportLayer.class).to(LocalMapTransportLayer.class).asEagerSingleton();
//    }
//
//    private static class LocalMapTransportLayer implements SamTransportLayer {
//
//        private final ExecutionContextManager executionContextManager;
//        private final ThreadExecutionManager threadExecutionManager;
//
//        @Inject
//        private LocalMapTransportLayer(ExecutionContextManager executionContextManager, ThreadExecutionManager threadExecutionManager) {
//            this.executionContextManager = executionContextManager;
//            this.threadExecutionManager = threadExecutionManager;
//        }
//
//        @Override
//        public String getServerEndpointBase() {
//            return null;
//        }
//
//        Map<URL, LocalServiceEndpointBinding> openEndpoints = Maps.newHashMap();
//
//        @Override
//        public ServiceEndpointBinding bindServiceInstance(URL serviceEndpointAddress) {
//            if (!openEndpoints.containsKey(serviceEndpointAddress)) {
//                openEndpoints.put(serviceEndpointAddress, new LocalServiceEndpointBinding(serviceEndpointAddress));
//            }
//            return openEndpoints.get(serviceEndpointAddress);
//        }
//
//        @Override
//        public TransactionCommunicationContext openTransactionCommunicationContext(UUID transactionUniqueID) {
//            return new LocalTransactionCommunicationContext(transactionUniqueID);
//        }
//
//        @Override
//        public SamTransportServer createExecutionEndpoint(int port) {
//            return null;
//        }
//
//        private TransportChannel createClientConnection(URL targetService, UUID transactionID) {
//            return new ClientConnection(targetService, transactionID);
//        }
//
//        private class LocalServiceEndpointBinding implements ServiceEndpointBinding {
//            private final URL endpoint;
//
//            public LocalServiceEndpointBinding(URL serviceEndpointAddress) {
//                this.endpoint = serviceEndpointAddress;
//            }
//
//            public void simulateCommunication(ClientConnection clientConnection) {
//                ThreadMessage message = clientConnection.clientToServerQueue.poll();
//                //TODO co z message???
//
//            }
//        }
//
//        private class LocalTransactionCommunicationContext implements TransactionCommunicationContext {
//
//            private final UUID transactionId;
//
//            private LocalTransactionCommunicationContext(UUID transactionId) {
//                this.transactionId = transactionId;
//            }
//
//            @Override
//            public TransportChannel openChannel(URL targetService) {
//                return createClientConnection(targetService, transactionId);
//            }
//
//            @Override
//            public void closeContext() {
//
//            }
//        }
//
//        private class ClientConnection extends TransportChannel {
//
//            private final URL target;
//            private final UUID transactionID;
//            private Queue<ThreadMessage> clientToServerQueue = new ArrayDeque<ThreadMessage>();
//            private Queue<ThreadMessage> serverToClientQueue = new ArrayDeque<ThreadMessage>();
//
//            public ClientConnection(URL targetService, UUID transactionID) {
//                this.target = targetService;
//                this.transactionID = transactionID;
//                initializeCommunication();
//            }
//
//            private void initializeCommunication() {
////                ThreadExecutionContext threadExecutionContext = executionContextManager.openExecutionContextForProtocolExecution(transactionID, target);
//                ThreadExecutionContext threadExecutionContext = executionContextManager.openExecutionContextForProtocolExecution(transactionID,null);
//                if (!threadExecutionContext.isExecutionRunning()) {
//                    threadExecutionManager.runExecutionContext(threadExecutionContext);
//                }
//            }
//
//            @Override
//            public void sendMessage(ThreadMessage message) {
//                clientToServerQueue.add(message);
//                openEndpoints.get(target).simulateCommunication(this);
//            }
//
//            @Override
//            public ThreadMessage waitResponse() {
//                ThreadMessage response = null;
//                while (response == null) {
//                    response = serverToClientQueue.poll();
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                    }
//                }
//                return response;
//            }
//
//            @Override
//            public ThreadMessage pollMessage() {
//                return serverToClientQueue.poll();
//            }
//        }
//
//        //
////        @Override
////        public TransactionCommunicationContext openTransactionCommunicationContext(URL serviceInstanceExecutionContext, UUID transactionUniqueID) {
////            return new WrappingTransactionCommunicationContext(serviceInstanceExecutionContext, transactionUniqueID);
////        }
////
////        private class WrappingTransactionCommunicationContext implements TransactionCommunicationContext {
////            private final URL serviceContext;
////            private final UUID transactionID;
////
////            private WrappingTransactionCommunicationContext(URL serviceContext, UUID transactionID) {
////                this.serviceContext = serviceContext;
////                this.transactionID = transactionID;
////            }
////
////            @Override
////            public TransportChannel openServiceEndpointChannel() {
////                return LocalMapTransportLayer.this.createServerEndpoint(transactionID,serviceContext);
////            }
////
////            @Override
////            public TransportChannel openChannel(URL targetService) {
////                return LocalMapTransportLayer.this.createClientEndpoint(serviceContext,transactionID, targetService);
////            }
////
////            @Override
////            public void closeContext() {
////
////            }
////        }
////
////        private TransportChannel createClientEndpoint(URL serviceContext, UUID transactionID, URL targetService) {
////            return null;
////        }
////
////        private TransportChannel createServerEndpoint(UUID transactionID, URL serviceEndpointURL) {
////            return null;
////        }
////
////
////
////        Map<String, Queue<ThreadMessage>> channelQueuesMap = Maps.newHashMap();
////
////        private TransportChannel getChannelQueue(URL serviceContext, UUID transactionID, URL targetService) {
////            Queue<ThreadMessage> sendingQueue = openQueue(serviceContext, transactionID, targetService);
////            Queue<ThreadMessage> receivingQueue = openQueue(serviceContext, transactionID, targetService);
////            return new TransportChannelQueue(serviceContext, transactionID, targetService, sendingQueue, receivingQueue);
////        }
////
////        private Queue<ThreadMessage> openQueue(URL from, UUID id, URL to) {
////            String key = from.toExternalForm() + id.toString() + to.toString();
////            if (!channelQueuesMap.containsKey(key)) {
////                channelQueuesMap.put(key, new ArrayDeque<ThreadMessage>(1));
////            }
////            return channelQueuesMap.get(key);
////        }
////
////        private static class TransportChannelQueue extends TransportChannel {
////            private final URL target;
////            private final URL source;
////            private final UUID transactionId;
////            private final Queue<ThreadMessage> sendingQueue;
////            private final Queue<ThreadMessage> receivingQueue;
////
////            public TransportChannelQueue(URL serviceContext, UUID transactionID, URL targetService, Queue<ThreadMessage> sendingQueue, Queue<ThreadMessage> receivingQueue) {
////                this.source = serviceContext;
////                this.transactionId = transactionID;
////                this.target = targetService;
////                this.sendingQueue = sendingQueue;
////                this.receivingQueue = receivingQueue;
////            }
////
////            @Override
////            public void sendMessage(ThreadMessage message) {
////                assert message.getTransactionId().equals(transactionId);
////                assert message.getSourceUrl().equals(source);
////                assert message.getTargetUrl().equals(target);
////                sendingQueue.add(message);
////            }
////
////            @Override
////            public ThreadMessage waitResponse() {
////                ThreadMessage response = null;
////                while (response == null) {
////                    response = receivingQueue.poll();
////                    try {
////                        Thread.sleep(100);
////                    } catch (InterruptedException e) {
////                    }
////                }
////                return response;
////            }
////
////            @Override
////            public ThreadMessage pollMessage() {
////                return receivingQueue.poll();
////            }
////        }
////
////
////        private void routingMessage(URL context, ThreadMessage message, UUID transactionID) {
//////            getCommunicationLine(context,message.getTargetUrl()).sendMessage(message);
////            ThreadExecutionContext threadExecutionContext = executionContextManager.openExecutionContextForProtocolExecution(transactionID, context);
////            if (!threadExecutionContext.isExecutionRunning()) {
////                threadExecutionContext.initGrobalTransactionContext(transactionID);
////                threadExecutionManager.runExecutionContext(threadExecutionContext);
////            }
////        }


}
