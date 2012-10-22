package pmsoft.sam.protocol.execution.serial;


@Deprecated
public class SerializableServerInfrastructure {

//	private final AtomicInteger counter = new AtomicInteger(0);
//
////	private final ServiceExecutionEnvironment see;
//
////	private final SerializableServer server;
//	private boolean running = false;
//
////	@Inject
////	public SerializableServerInfrastructure(ServiceExecutionEnvironment see, SerializableServer server) {
////		this.see = see;
////		this.server = server;
////	}
//
//	public void startServer(int port) {
//		Preconditions.checkState(!running, "server already running");
//		running = true;
//		server.setPort(port);
////		see.setupEnvironment();
//		Thread serverThread = new Thread(this.server, "SEE");
//		serverThread.start();
//	}
//
//	public void stopServer() {
//		server.shutdown();
//	}
//
//	public URL createUniqueServiceURL() {
//		String service = "service" + counter.addAndGet(1);
//		try {
//			return new URL("http", "localhost", server.getPort(), "service" + service);
//		} catch (MalformedURLException e) {
//			throw new RuntimeException(e);
//		}
//	}

}
