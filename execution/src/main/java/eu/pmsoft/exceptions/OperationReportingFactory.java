package eu.pmsoft.exceptions;

import com.google.common.base.Preconditions;

public class OperationReportingFactory {

    ThreadLocal<OperationContext> contextPerThread = new ThreadLocal<OperationContext>();

    public OperationContext ensureEmptyContext() {
        OperationContext current = contextPerThread.get();
        Preconditions.checkState(current == null, "context is not empty, unexpected state on SamOperationContextFactory");
        OperationContext context = new OperationContext(null);
        contextPerThread.set(context);
        return context;
    }

    public OperationContext openExistingContext() {
        OperationContext current = contextPerThread.get();
        Preconditions.checkNotNull(current, "operation context is empty, unexpected state on SamOperationContextFactory");
        return current;
    }

    public OperationContext openNestedContext() {
        OperationContext current = contextPerThread.get();
        OperationContext context = new OperationContext(current);
        contextPerThread.set(context);
        return context;
    }

    public void closeContext(OperationContext operationContext) {
        contextPerThread.set(operationContext.getParent());
    }
}
