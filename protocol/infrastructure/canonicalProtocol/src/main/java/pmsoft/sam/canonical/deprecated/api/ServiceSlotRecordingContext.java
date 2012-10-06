package pmsoft.sam.canonical.deprecated.api;

import java.lang.reflect.Method;

import pmsoft.sam.canonical.deprecated.CanonicalInstanceRecorder;

import com.google.inject.Key;

public interface ServiceSlotRecordingContext {

	<T> void recordExternalMethodCall(Key<T> key, int serviceInstanceNr, Method method, Object[] args);

	<T, S> CanonicalInstanceRecorder<S> recordExternalMethodCallWithInterfaceReturnType(Key<T> key, int serviceInstanceNr,
			Method method, Object[] args, Class<S> returnType);

	<T, S> S recordExternalMethodCallWithReturnType(Key<T> key, int serviceInstanceNr, Method method, Object[] args,
			Class<S> returnType);
	
}
