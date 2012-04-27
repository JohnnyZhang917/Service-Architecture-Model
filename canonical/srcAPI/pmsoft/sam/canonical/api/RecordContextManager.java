package pmsoft.sam.canonical.api;

import java.net.URL;

import pmsoft.sam.inject.wrapper.InstanceProvider;

public interface RecordContextManager {

	InstanceProvider registerExternalInstanceProvider(URL externalReference);

}
