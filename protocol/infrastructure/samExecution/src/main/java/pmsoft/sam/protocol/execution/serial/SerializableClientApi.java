package pmsoft.sam.protocol.execution.serial;

import java.net.URL;

import pmsoft.sam.protocol.execution.CanonicalProtocolExecutionServiceClientApi;
import pmsoft.sam.protocol.execution.CanonicalProtocolRequest;
import pmsoft.sam.protocol.execution.CanonicalProtocolRequestData;
import pmsoft.sam.protocol.execution.CanonicalProtocolServiceEndpointLocation;

import com.google.inject.Inject;

public class SerializableClientApi implements CanonicalProtocolExecutionServiceClientApi {


	@Inject
	public SerializableClientApi() {
		super();
	}

	@Override
	public CanonicalProtocolRequestData executeExternalCanonicalRequest(CanonicalProtocolRequest request) {
		CanonicalProtocolServiceEndpointLocation target = request.getTargetLocation();
		URL location = target.getEndpointLocation();
		ClientCall call = new ClientCall(location.getHost(), location.getPort(), request);
		try {
			CanonicalProtocolRequestData responce = call.run();
			return responce;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
