package pmsoft.sam.protocol.execution.serial;

import java.net.URL;

import pmsoft.sam.protocol.execution.CanonicalProtocolExecutionServiceClientApi;
import pmsoft.sam.protocol.execution.CanonicalProtocolRequest;
import pmsoft.sam.protocol.execution.CanonicalProtocolRequestData;
import pmsoft.sam.protocol.execution.CanonicalProtocolServiceEndpointLocation;

public class SerializableClientApi implements CanonicalProtocolExecutionServiceClientApi {

	@Override
	public CanonicalProtocolRequestData executeExternalCanonicalRequest(CanonicalProtocolRequest request) {
		CanonicalProtocolServiceEndpointLocation target = request.getTargetLocation();
		URL location = target.getEndpointLocation();
		ClientCall call = new ClientCall(location.getHost(), location.getPort(), request);
		try {
			return call.run();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
