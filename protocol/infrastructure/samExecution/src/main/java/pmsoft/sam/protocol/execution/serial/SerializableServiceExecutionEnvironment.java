package pmsoft.sam.protocol.execution.serial;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

import pmsoft.sam.protocol.execution.ServiceExecutionEnvironment;
import pmsoft.sam.see.api.model.SIURL;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class SerializableServiceExecutionEnvironment implements ServiceExecutionEnvironment {

	private final String host;
	private final Integer port;
	private final SerializableServer server;
	private final AtomicInteger counter = new AtomicInteger();

	@Inject
	public SerializableServiceExecutionEnvironment(@Named(ServiceExecutionEnvironment.SERVICE_PORT_NAMED_BINDING) Integer port, SerializableServer server) {
		super();
		try {
			this.host = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO exception handling policy
			throw new RuntimeException(e);
		}
		this.port = port;
		this.server = server;
	}

	@Override
	public SIURL createUniqueURL() {
		try {
			return new SIURL(new URL("http", host, port, "/service" + counter.addAndGet(1)));
		} catch (MalformedURLException e) {
			// TODO exception handling policy
			throw new RuntimeException(e);
		}
	}

}
