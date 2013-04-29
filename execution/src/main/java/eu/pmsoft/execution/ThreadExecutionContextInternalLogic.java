package eu.pmsoft.execution;

import com.google.inject.Key;

import java.net.URL;
import java.util.List;
import java.util.UUID;

public interface ThreadExecutionContextInternalLogic {

    public <T> T getInstance(Key<T> key);

//    public void enterExecution(ThreadMessagePipe headCommandPipe, List<ThreadMessagePipe> endpoints);
    public void enterExecution();

    public void exitExecution();

    public List<URL> getEndpointAddressList();

    void executeCanonicalProtocol();

    void initTransaction();

    void closeTransaction();
}
