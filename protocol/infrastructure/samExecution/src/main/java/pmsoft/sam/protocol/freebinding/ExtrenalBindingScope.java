package pmsoft.sam.protocol.freebinding;


import com.google.common.base.Preconditions;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

public class ExtrenalBindingScope implements Scope, ExternalBindingController {

	private final ThreadLocal<ExternalInstanceProvider> perThreadServiceExecutionRecorder;


	public ExtrenalBindingScope() {
		perThreadServiceExecutionRecorder = new ThreadLocal<ExternalInstanceProvider>();
	}

	/**
	 * This scope is for internal use only. We know that only
	 * ServiceExecutionRecorder type will be scoped.
	 * If this is not the case, then somebody mess around and have big problems
	 */
	public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
		return new Provider<T>() {
			@SuppressWarnings("unchecked")
			public T get() {
				ExternalInstanceProvider value = perThreadServiceExecutionRecorder.get();
				Preconditions.checkNotNull(value);
				return (T) value;
			}
		};
	}

	public void bindRecordContext(ExternalInstanceProvider recordContext) {
		perThreadServiceExecutionRecorder.set(recordContext);
	}

	public void unBindRecordContext() {
		perThreadServiceExecutionRecorder.remove();
	}

}
