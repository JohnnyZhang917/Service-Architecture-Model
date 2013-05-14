package eu.pmsoft.sam.protocol.record;

import eu.pmsoft.see.api.model.SIURL;

interface ExternalInstanceRegistry extends InstanceRegistry {

    SIURL getExternalUrl();
}
