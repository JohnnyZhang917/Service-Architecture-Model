package eu.pmsoft.sam.see.api;

import com.google.inject.AbstractModule;

import java.net.InetSocketAddress;

public class TestSamEnvironment extends AbstractModule {
    @Override
    protected void configure() {
        bind(TestServiceExecutionCreationByStep.class);
        InetSocketAddress serverAddressBind = new InetSocketAddress(5111);
        bind(InetSocketAddress.class).toInstance(serverAddressBind);
    }
}
