package eu.pmsoft.sam.see.transport.netty;

import com.google.inject.AbstractModule;
import eu.pmsoft.sam.see.transport.SamTransportLayer;
import eu.pmsoft.sam.see.transport.nettyrefactor.SamTransportServerNetty;

public class SamTransportNettyModule extends AbstractModule {
    @Override
    protected void configure() {
        binder().requireExplicitBindings();
        bind(SamTransportLayer.class).to(SamTransportLayerNetty.class).asEagerSingleton();
        bind(SamTransportServerNetty.class);
//        bind(SamTransportServerConnectionHandler.class);
//        bind(SamTransportClientConnectionHandler.class);
//        bind(SamTransportServerConnectionHandler.class);

    }
}
