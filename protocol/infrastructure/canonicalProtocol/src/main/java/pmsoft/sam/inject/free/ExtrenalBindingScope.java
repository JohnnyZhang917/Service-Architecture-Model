package pmsoft.sam.inject.free;

import com.google.common.base.Preconditions;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

public class ExtrenalBindingScope implements Scope, ExternalBindingController {

	private final ThreadLocal<ExternalInstanceProvider> perThreadServiceExecutionRecorder;

	private static final Provider<Object> SEEDED_KEY_PROVIDER = new Provider<Object>() {
		public Object get() {
			throw new IllegalStateException("If you got here then it means that the environment implementation don't bind correctly the execution transaction.");
		}
	};

	public ExtrenalBindingScope() {
		super();
		perThreadServiceExecutionRecorder = new ThreadLocal<ExternalInstanceProvider>();
	}

	/**
	 * This scope is for internal use only. We know that only
	 * ServiceExecutionRecorder type will be scoped
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

	/**
	 * Returns a provider that always throws exception complaining that the
	 * object in question must be seeded before it can be injected.
	 * 
	 * @return typed provider
	 */
	@SuppressWarnings({ "unchecked" })
	public <T> Provider<T> seededKeyProvider() {
		return (Provider<T>) SEEDED_KEY_PROVIDER;
	}

}
