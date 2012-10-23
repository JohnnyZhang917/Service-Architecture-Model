package pmsoft.sam.protocol.execution.local;

import pmsoft.sam.protocol.execution.ServiceExecutionEnvironment;
import pmsoft.sam.see.api.model.SIURL;

public class LocalServiceExecutionEnvironment implements ServiceExecutionEnvironment {

	private int counter = 0;
	
	@Override
	public SIURL createUniqueURL() {
		return new SIURL("http://localhost/service"+counter++);
	}

	@Override
	public void startUpServices() {
		
	}

	@Override
	public void shutdownServices() {
		
	}

}
