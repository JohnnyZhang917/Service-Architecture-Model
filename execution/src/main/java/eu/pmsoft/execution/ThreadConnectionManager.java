package eu.pmsoft.execution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.netty.channel.Channel;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

class ThreadConnectionManager {

//    private final ConcurrentMap<String, ThreadClientConnection> clientConnections;
//    private final ThreadExecutionModelFactory model;
//
//    private final Map<String, ThreadMessagePipe> pipeRoutingMap = Maps.newConcurrentMap();

    @Inject
    ThreadConnectionManager(ThreadExecutionModelFactory model) {
//        this.model = model;
//        clientConnections = Maps.newConcurrentMap();
    }

//    ThreadClientConnection openConnection(URL address) {
//        String key = address.getHost() + ":" + address.getPort();
//        if (!clientConnections.containsKey(key)) {
//            ThreadClientConnection serverConnection = model.openConnection(address);
//            clientConnections.putIfAbsent(key, serverConnection);
//        }
//        return clientConnections.get(key);
//    }

//    List<ThreadMessagePipe> openPipes(UUID transactionID, List<URL> endpointAdressList) {
//        ImmutableList.Builder<ThreadMessagePipe> builder = ImmutableList.builder();
//        for (URL address : endpointAdressList) {
//            ThreadClientConnection connection = openConnection(address);
//            String signature = newUniquePipeTransactionSignature(transactionID, address);
//            ThreadMessagePipe threadMessagePipe = connection.bindPipe(signature, transactionID, address);
//            builder.add(threadMessagePipe);
//        }
//        return builder.build();
//    }

//    ThreadMessagePipe createHeadPipe(String signature, Channel channel) {
//        ThreadMessagePipe headPipe = model.createPipe(channel, signature, null, null);
//        pipeRoutingMap.put(headPipe.getSignature(), headPipe);
//        return headPipe;
//    }

//    AtomicInteger signatureCounter = new AtomicInteger();

//    private String newUniquePipeTransactionSignature(UUID transactionID, URL endpointAddress) {
//        // TODO make a more effective unique signature pipe
//        int counter = signatureCounter.getAndAdd(1);
//        return counter + ":" + transactionID.toString() + "-->" + endpointAddress.toString();
//    }

}
