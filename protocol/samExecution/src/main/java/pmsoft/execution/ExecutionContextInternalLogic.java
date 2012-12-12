package pmsoft.execution;

import java.net.URL;
import java.util.List;

import com.google.inject.Key;

public interface ExecutionContextInternalLogic {
	public <T> T getInstance(Key<T> key);

	public void enterExecution(ThreadMessagePipe headCommandPipe, List<ThreadMessagePipe> endpoints);

	public void exitExecution();

	public List<URL> getEndpointAdressList();

    void executeCanonicalProtocol();

    void initTransaction();

    void closeTransaction();
}
