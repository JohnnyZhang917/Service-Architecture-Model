package pmsoft.sam.protocol.record;

import pmsoft.sam.protocol.record.InstanceRegistry;
import pmsoft.sam.see.api.model.SIURL;

interface ExternalInstanceRegistry extends InstanceRegistry {

	SIURL getExternalUrl();
}
