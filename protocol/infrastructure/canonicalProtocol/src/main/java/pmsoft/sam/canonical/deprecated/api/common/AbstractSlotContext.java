package pmsoft.sam.canonical.deprecated.api.common;

import java.lang.reflect.Method;

import pmsoft.sam.protocol.injection.MethodRecordContext;
import pmsoft.sam.protocol.injection.internal.MethodCall;
import pmsoft.sam.protocol.injection.internal.ir.CanonicalInstanceRecorder;
import pmsoft.sam.protocol.injection.internal.ir.ServiceSlotRecordingContext;

import com.google.inject.Key;

public abstract class AbstractSlotContext implements ServiceSlotRecordingContext {

	protected final MethodRecordContext executionContext;
	protected final int serviceSlotNrInTransaction;

	public AbstractSlotContext(MethodRecordContext executionContext, int serviceSlotNrInTransaction) {
		super();
		this.executionContext = executionContext;
		this.serviceSlotNrInTransaction = serviceSlotNrInTransaction;
	}

	public <T> void recordExternalMethodCall(Key<T> key, int serviceInstanceNr, Method method, Object[] args) {
		int[] arguments = null;
		if (args != null && args.length > 0) {
			arguments = parseArguments(args);
		}
		MethodCall call = new MethodCall(method, serviceInstanceNr, -1, serviceSlotNrInTransaction);
		if (args != null && args.length > 0) {
			call.setArgumentReferences(arguments);
		}
		executionContext.pushMethodCall(call);
	}

	protected abstract int[] parseArguments(Object[] args);

	protected abstract <S> CanonicalInstanceRecorder<S> createRecordInstance(Key<S> key);

	protected abstract <S> int createPendingDataBinding(Class<S> returnType);
	
	public <T, S> CanonicalInstanceRecorder<S> recordExternalMethodCallWithInterfaceReturnType(Key<T> key, int serviceInstanceNr,
			Method method, Object[] args, Class<S> returnType) {
		int[] arguments = null;
		if (args != null && args.length > 0) {
			arguments = parseArguments(args);
		}
		CanonicalInstanceRecorder<S> returnRecorder = createRecordInstance(Key.get(returnType));
		MethodCall call = new MethodCall(method, serviceInstanceNr, returnRecorder.getServiceInstanceNr(),
				serviceSlotNrInTransaction);
		if (args != null && args.length > 0) {
			call.setArgumentReferences(arguments);
		}
		executionContext.pushMethodCall(call);
		return returnRecorder;
	}

	@SuppressWarnings("unchecked")
	public <T, S> S recordExternalMethodCallWithReturnType(Key<T> key, int serviceInstanceNr, Method method, Object[] args,
			Class<S> returnType) {
		int[] arguments = null;
		if (args != null && args.length > 0) {
			arguments = parseArguments(args);
		}
		int returnDataNr = createPendingDataBinding(returnType);
		MethodCall call = new MethodCall(method, serviceInstanceNr, returnDataNr, serviceSlotNrInTransaction);
		if (args != null && args.length > 0) {
			call.setArgumentReferences(arguments);
		}
		return (S) executionContext.flushMethodCall(call);
	}
}
