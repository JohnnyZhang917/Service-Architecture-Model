package eu.pmsoft.exceptions;

import com.google.inject.Inject;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;

@Guice(modules = OperationReportingModule.class)
public class ExceptionsTest {

//    public static class TestRootException extends Exception {
//
//    }
//
//    public static class SpecificOneExce extends TestRootException{}
//    public static class SpecificTwoExce extends TestRootException{}
//
//    public static class TestRootRuntimeException extends RuntimeException {
//
//    }
//
//    public static class TestExceptionUseRoot {
//
//        public void businessMethod() {
//
//            try {
//
//            } catch (TestRootRuntimeException e) {
//
//            }
//        }
//    }

    @Inject
    OperationReportingFactory operationReportingFactory;

    @Test
    public void testEmptyHasNoErrors() {
        for (int i = 0; i < 2; i++) {
            OperationContext operationContext = operationReportingFactory.ensureEmptyContext();
            assertFalse(operationContext.getErrors().hasErrors());
            assertNull(operationContext.getParent());
            operationReportingFactory.closeContext(operationContext);
        }
    }
}
