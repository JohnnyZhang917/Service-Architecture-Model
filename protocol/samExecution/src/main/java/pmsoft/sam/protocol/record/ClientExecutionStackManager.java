package pmsoft.sam.protocol.record;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.slf4j.LoggerFactory;
import pmsoft.sam.see.api.model.ExecutionStrategy;

class ClientExecutionStackManager extends AbstractExecutionStackManager {

    @Inject
    public ClientExecutionStackManager(@Assisted ImmutableList<InstanceRegistry> instanceRegistries, @Assisted ExecutionStrategy executionStrategy) {
        super(instanceRegistries, LoggerFactory.getLogger(ClientExecutionStackManager.class), executionStrategy);
    }

    @Override
    public void executeCanonicalProtocol() {
        // TODO better class hierarchii to aovid this implementations
        throw new RuntimeException("not possible");
    }

}
