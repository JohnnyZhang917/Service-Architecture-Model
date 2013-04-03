package eu.pmsoft.sam.protocol.record;

import eu.pmsoft.sam.see.api.model.SIURL;

interface ExternalInstanceRegistry extends InstanceRegistry {

    SIURL getExternalUrl();
}
