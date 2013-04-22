package eu.pmsoft.execution;

import java.net.URL;

/**
 * interface to be implemented by the provider of internal logic of the thread execution machine
 */
public interface ThreadExecutionLogicProvider {

    public ThreadExecutionContextInternalLogic open(URL targetURL);

}