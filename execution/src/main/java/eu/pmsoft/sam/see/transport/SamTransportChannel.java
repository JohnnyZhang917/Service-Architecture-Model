package eu.pmsoft.sam.see.transport;

import eu.pmsoft.execution.ThreadMessage;

public interface SamTransportChannel {

    public void bindConnection();
    public void unbindConnection();

    public void sendMessage(ThreadMessage message);

    public ThreadMessage waitResponse();

    public ThreadMessage pollMessage();
}
