package eu.pmsoft.exceptions.data;

public class TestOperationInput {


    private boolean causeOfExceptionTwo = false;
    private boolean causeOfExceptionOne = false;
    private boolean causeForNestedOperationExceptions;
    private boolean ableToHandleExceptionOne;
    private boolean ableToHandleExceptionTwo;
    private boolean causefoCheckedExceptionResult;

    public boolean isCauseOfExceptionTwo() {
        return causeOfExceptionTwo;
    }

    public void setCauseOfExceptionTwo(boolean causeOfExceptionTwo) {
        this.causeOfExceptionTwo = causeOfExceptionTwo;
    }

    public boolean isCauseOfExceptionOne() {
        return causeOfExceptionOne;
    }

    public void setCauseOfExceptionOne(boolean causeOfExceptionOne) {
        this.causeOfExceptionOne = causeOfExceptionOne;
    }

    public boolean isCauseForNestedOperationExceptions() {
        return causeForNestedOperationExceptions;
    }

    public void setCauseForNestedOperationExceptions(boolean causeForNestedOperationExceptions) {
        this.causeForNestedOperationExceptions = causeForNestedOperationExceptions;
    }

    public boolean isAbleToHandleExceptionOne() {
        return ableToHandleExceptionOne;
    }

    public void setAbleToHandleExceptionOne(boolean ableToHandleExceptionOne) {
        this.ableToHandleExceptionOne = ableToHandleExceptionOne;
    }

    public boolean isAbleToHandleExceptionTwo() {
        return ableToHandleExceptionTwo;
    }

    public void setAbleToHandleExceptionTwo(boolean ableToHandleExceptionTwo) {
        this.ableToHandleExceptionTwo = ableToHandleExceptionTwo;
    }

    public boolean isCausefoCheckedExceptionResult() {
        return causefoCheckedExceptionResult;
    }

    public void setCausefoCheckedExceptionResult(boolean causefoCheckedExceptionResult) {
        this.causefoCheckedExceptionResult = causefoCheckedExceptionResult;
    }
}
