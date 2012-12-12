package pmsoft.execution;

import java.net.URL;

public interface InternalLogicContextFactory {

	public ExecutionContextInternalLogic open(URL targetURL);

}
