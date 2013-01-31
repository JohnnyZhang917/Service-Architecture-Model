package pmsoft.sam.exceptions;

import com.google.common.base.Preconditions;

public class SamOperationContextFactory {

    ThreadLocal<SamOperationContext> contextPerThread = new ThreadLocal<SamOperationContext>();

    public SamOperationContext ensureEmptyContext(){
        SamOperationContext current = contextPerThread.get();
        Preconditions.checkState(current == null,"context is not empty, unexpected state on SamOperationContextFactory");
        SamOperationContext context = new SamOperationContext();
        contextPerThread.set(context);
        return context;
    }

    public SamOperationContext openNewContext() {
        return new SamOperationContext();
    }

    public SamOperationContext openExistingContext() {
        SamOperationContext current = contextPerThread.get();
        Preconditions.checkNotNull(current,"operation context is empty, unexpected state on SamOperationContextFactory");
        return current;
    }
}
