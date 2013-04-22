package eu.pmsoft.sam.see.transport;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import eu.pmsoft.injectionUtils.logger.InjectLogger;
import eu.pmsoft.sam.infrastructure.SamInfrastructureLayerObject;
import eu.pmsoft.sam.see.configuration.SEEConfiguration;
import org.slf4j.Logger;
import scala.Option;
import scala.Some;
import scala.Some$;

import java.net.InetSocketAddress;

public class SamTransportLayerImplementation implements SamTransportLayer {

    private final InetSocketAddress serverAddress;

    @InjectLogger
    private Logger logger;

    @Inject
    SamTransportLayerImplementation(SEEConfiguration configuration) {
        super();
        this.serverAddress = configuration.address;
        ;
        Option<InetSocketAddress> address = new Some(serverAddress);
        SamInfrastructureLayerObject infra = new SamInfrastructureLayerObject(address);

//        ServerBuilder builder = new ServerBuilder();
//        builder.bindTo(serverAddress).codec()
    }

    @Override
    public String getServerEndpointBase() {
        Preconditions.checkState(serverAddress != null, "No server endpoint running locally. Environment should not try to create a transaction in this node");
        return serverAddress.getHostName() + ":" + serverAddress.getPort();
    }
}
