package pmsoft.sam.canonical.deprecated.api;

import java.net.URL;

import pmsoft.sam.protocol.injection.InstanceProvider;

public interface RecordContextManager {

	InstanceProvider registerExternalInstanceProvider(URL externalReference);

}
