package pmsoft.sam.protocol.injection.internal;

import java.lang.reflect.Method;


import com.google.inject.Key;

public interface ServiceSlotRecordingContext {

	<T> void recordExternalMethodCall(Key<T> key, int serviceInstanceNr, Method method, Object[] args);

	<T, S> CanonicalInstanceRecorder<S> recordExternalMethodCallWithInterfaceReturnType(Key<T> key, int serviceInstanceNr,
			Method method, Object[] args, Class<S> returnType);

	<T, S> S recordExternalMethodCallWithReturnType(Key<T> key, int serviceInstanceNr, Method method, Object[] args,
			Class<S> returnType);
	
}
