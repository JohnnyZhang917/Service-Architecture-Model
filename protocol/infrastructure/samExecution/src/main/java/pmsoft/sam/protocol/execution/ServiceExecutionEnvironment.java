package pmsoft.sam.protocol.execution;

import pmsoft.sam.see.api.model.SIURL;

public interface ServiceExecutionEnvironment {
	
	public static String SERVICE_PORT_NAMED_BINDING = "SERVICE_PORT_NAMED";

	public SIURL createUniqueURL();
	
	public void startUpServices();
	
	public void shutdownServices();
	
}
