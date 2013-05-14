package eu.pmsoft.sam.protocol.record;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.slf4j.LoggerFactory;

class ClientExecutionStackManager extends AbstractExecutionStackManager {

    @Inject
    public ClientExecutionStackManager(@Assisted ImmutableList<InstanceRegistry> instanceRegistries) {
        super(instanceRegistries, LoggerFactory.getLogger(ClientExecutionStackManager.class));
    }

    @Override
    public void executeCanonicalProtocol() {
        // TODO better class hierarchii to aovid this implementations
        throw new RuntimeException("not possible");
    }

}
