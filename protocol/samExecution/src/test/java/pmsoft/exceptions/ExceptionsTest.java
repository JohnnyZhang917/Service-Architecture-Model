package pmsoft.exceptions;

import com.google.inject.Inject;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

@Guice(modules = OperationReportingModule.class)
public class ExceptionsTest {

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
