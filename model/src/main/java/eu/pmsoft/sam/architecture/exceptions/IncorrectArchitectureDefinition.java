package eu.pmsoft.sam.architecture.exceptions;

public class IncorrectArchitectureDefinition extends RuntimeException {

    private static final long serialVersionUID = -2162301347523415386L;

    public IncorrectArchitectureDefinition(String error) {
        super(error);
    }

}
