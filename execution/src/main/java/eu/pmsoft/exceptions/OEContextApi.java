package eu.pmsoft.exceptions;

public interface OEContextApi {

    OEContextApi addMessage(String message);

    void throwAsException();
}
