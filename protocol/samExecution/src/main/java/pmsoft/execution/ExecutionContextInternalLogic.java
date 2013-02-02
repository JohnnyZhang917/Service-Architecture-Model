package pmsoft.execution;

import com.google.inject.Key;

import java.net.URL;
import java.util.List;

public interface ExecutionContextInternalLogic {
	public <T> T getInstance(Key<T> key);

	public void enterExecution(ThreadMessagePipe headCommandPipe, List<ThreadMessagePipe> endpoints);

	public void exitExecution();

	public List<URL> getEndpointAdressList();

    void executeCanonicalProtocol();

    void initTransaction();

    void closeTransaction();
}
