package eu.pmsoft.exceptions.data;

import com.google.inject.Inject;
import eu.pmsoft.exceptions.*;

public class TestExceptionBusinessLayerRoot {

    private final OperationExceptionContextApi<TestRootRuntimeException> exceptionContextApi;

    @Inject
    public TestExceptionBusinessLayerRoot(OperationExceptionContextBuilderApi exceptionContextApi) {
        this.exceptionContextApi = exceptionContextApi.exceptionContext(TestRootRuntimeException.class);
    }

    /**
     * This operation has always a result. This means that errors are passed is information on result.
     * @param input
     * @return
     */
    public TestResultExample businessOperationPublicApi(TestOperationInput input) {
        OEContext<TestRootRuntimeException> operationContext = exceptionContextApi.openNestedOperationContext();
        try {
            try {
                return nestedCalls(input);
            } finally {
                // ????
                operationContext.close();
            }
        } catch (TestRootRuntimeException operationException) {
            operationContext.catchException(operationException);
            ////////////
            //translation to error on operation response
            TestResultExample errorResult = new TestResultExample();
            errorResult.markOperationAsFailed();
            return errorResult;
            ////////////
        } finally {
            // ????
            operationContext.close();
        }
    }

    // Here operation finish on a exception. Operation context close normally and no result is returned.
    public TestResultExample businessOperationPublicApiWithChecked(TestOperationInput input) throws TestCheckedException {
        OEContext<TestRootRuntimeException> operationContext = exceptionContextApi.openNestedOperationContext();

        try {
            try {
                // send exception during method execution
                if( input.isCausefoCheckedExceptionResult())
                    throw new TestCheckedException();

                return nestedCalls(input);
            } finally {
                // ????
                operationContext.close();
            }
        } catch (TestRootRuntimeException operationException) {
            operationContext.catchException(operationException);
            //////////////////////
            // translate to exception
            throw new TestCheckedException();
            //////////////////////
        } finally {
            //// ????
            operationContext.close();
        }

    }

    private TestResultExample nestedCalls(TestOperationInput input) {
        OEContext<TestRootRuntimeException> operationContext = exceptionContextApi.openNestedOperationContext();
        operationContext.mayThrow(SpecificOneException.class);
        operationContext.mayThrow(SpecificTwoException.class);
        try {
            stepOne(input);
            stepTwo(input);
            stepThree(input);
        } catch (SpecificOneException e) {
            // Try to handle this situation.
            // Checked exception means: Handle this predefined case
            // ... handling code, or delegate to operation nested context
            if(! input.isAbleToHandleExceptionOne()) {
                operationContext.chainException(e);
            }
        } catch (SpecificTwoException e) {
            if(! input.isAbleToHandleExceptionTwo()) {
                operationContext.chainException(e);
            }
        } finally {
            operationContext.close();
        }
        return null;
    }

    private void stepThree(TestOperationInput input) {
        // Injected context with thread local scope
        OEContextApi operationContext = exceptionContextApi.getCurrentOperationContext();
        if( input.isCauseForNestedOperationExceptions()) {
            operationContext.addMessage("Error Message").throwAsException();
        }
    }

    private void stepTwo(TestOperationInput input) throws SpecificTwoException {
        if( input.isCauseOfExceptionTwo()) throw  new SpecificTwoException();
    }

    private void stepOne(TestOperationInput input)  throws SpecificOneException {
        if( input.isCauseOfExceptionOne()) throw  new SpecificOneException();
    }
}
