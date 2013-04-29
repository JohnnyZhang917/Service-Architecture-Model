package eu.pmsoft.sam.see.transport.netty;

import eu.pmsoft.injectionUtils.logger.InjectLogger;
import eu.pmsoft.sam.see.transport.SamTransportCommunicationContext;
import eu.pmsoft.sam.see.transport.SamTransportLayer;
import eu.pmsoft.sam.see.transport.nettyrefactor.SamTransportServerNetty;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Provider;

public class SamTransportLayerNetty implements SamTransportLayer {


    @InjectLogger
    private Logger logger;

    private final Provider<SamTransportServerNetty> transportServerProvider;

    @Inject
    public SamTransportLayerNetty(Provider<SamTransportServerNetty> transportServerProvider) {
        this.transportServerProvider = transportServerProvider;
    }


    @Override
    public SamTransportCommunicationContext createExecutionEndpoint(int port) {
        SamTransportServerNetty samTransportServerNetty = transportServerProvider.get();
        samTransportServerNetty.setupServer(port);
        return samTransportServerNetty;
    }

}
