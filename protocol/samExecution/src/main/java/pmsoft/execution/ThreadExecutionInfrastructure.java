package pmsoft.execution;

import com.google.inject.Inject;
import org.slf4j.Logger;
import pmsoft.injectionUtils.logger.InjectLogger;

import java.net.InetSocketAddress;

public final class ThreadExecutionInfrastructure {

    private final ModelFactory modelFactory;

    @InjectLogger
    private Logger logger;

    @Inject
    public ThreadExecutionInfrastructure(ModelFactory modelFactory) {
        super();
        this.modelFactory = modelFactory;
    }

    public ThreadExecutionServer createServer(InetSocketAddress serverAddress) {
        logger.debug("New server instance, adress {}", serverAddress);
        return modelFactory.server(serverAddress);
    }
}
