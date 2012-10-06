package pmsoft.sam.inject.wrapper.deprecated;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.google.inject.Provider;

class WrappingControllerProvider<T> implements Provider<T> {

	private class MonitoringExecutionReference implements InvocationHandler {

		private final T realInstance;

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			boolean firstCallEntry = WrappingControllerProvider.this.controller.bindExecutionContext();
			Object result = method.invoke(realInstance, args);
			if( firstCallEntry ) {
				WrappingControllerProvider.this.controller.unbindExecutionContext();
			}
			return result;
		}

		public MonitoringExecutionReference(T realInstance) {
			super();
			this.realInstance = realInstance;
		}
	}

	private final Provider<T> realProvider;
	private final Class<?> referenceClass;
	private final  WrappingInjectorController controller;

	public WrappingControllerProvider(Provider<T> realProvider,Class<?> referenceClass, WrappingInjectorController controller) {
		super();
		this.realProvider = realProvider;
		this.referenceClass =referenceClass;
		this.controller = controller;
	}

	@SuppressWarnings("unchecked")
	public T get() {
		T realReference = realProvider.get();
		MonitoringExecutionReference monitoringReference = new MonitoringExecutionReference(realReference);
		return (T) Proxy.newProxyInstance(referenceClass.getClassLoader(), new Class[] { referenceClass }, monitoringReference);
	}

}
