package eu.pmsoft.exceptions.data;

public class TestResultExample {

    private boolean error = false;
    private String operationResult;

    public boolean isError() {
        return error;
    }

    public void markOperationAsFailed() {
        this.error = true;
    }

    public void setOperationResult(String operationResult) {
        this.operationResult = operationResult;
    }

    public String getOperationResult() {
        return operationResult;
    }
}
