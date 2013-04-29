package eu.pmsoft.sam.see.transport;

public interface SamTransportLayer {

    public SamTransportCommunicationContext createExecutionEndpoint(int port);

}
