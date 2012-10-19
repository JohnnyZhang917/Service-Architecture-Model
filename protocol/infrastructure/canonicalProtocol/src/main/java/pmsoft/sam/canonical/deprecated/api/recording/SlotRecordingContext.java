package pmsoft.sam.canonical.deprecated.api.recording;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import pmsoft.sam.canonical.deprecated.api.common.AbstractSlotContext;
import pmsoft.sam.protocol.injection.MethodRecordContext;

import com.google.inject.Key;

public class SlotRecordingContext extends AbstractSlotContext {

	private InstanceRegistry instanceRegistry;

	public SlotRecordingContext(MethodRecordContext executionContext, int serviceSlotNrInTransaction) {
		super(executionContext, serviceSlotNrInTransaction);
	}
	
	@Override
	protected int[] parseArguments(Object[] args) {
		int[] argumentNrs = new int[args.length];
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (arg == null) {
				argumentNrs[i] = 0;
			}
			// Extract the real object from switching proxy's
			if (arg instanceof Proxy) {
				Proxy parg = (Proxy) arg;
				InvocationHandler handler = Proxy.getInvocationHandler(parg);
				if (handler instanceof ExternalBindingSwitch) {
					ExternalBindingSwitch<?> switchProxy = (ExternalBindingSwitch<?>) handler;
					arg = switchProxy.getInternalInstance();
				}
			}

			if (arg instanceof Proxy) {
				Proxy parg = (Proxy) arg;
				InvocationHandler handler = Proxy.getInvocationHandler(parg);
				if (handler instanceof CanonicalInstanceRecorder) {
					CanonicalInstanceRecorder<?> recorder = (CanonicalInstanceRecorder<?>) handler;
					if (recorder.getServiceSlotNr() == serviceSlotNrInTransaction) {
						argumentNrs[i] = recorder.getServiceInstanceNr();
					} else {
						argumentNrs[i] = executionContext.createExternalInstanceBinding(serviceSlotNrInTransaction, recorder);
					}
				} else {
					throw new RuntimeException("Unknow Proxy type");
				}
			} else if (arg instanceof Serializable) {
				argumentNrs[i] = instanceRegistry.createDataBinding(arg);
			} else {
				throw new RuntimeException("argument not serializable be canonical protocol");
			}
		}
		return argumentNrs;
	}

	@Override
	protected <S> CanonicalInstanceRecorder<S> createRecordInstance(Key<S> key) {
		return instanceRegistry.createKeyBinding(key);
	}

	@Override
	protected <S> int createPendingDataBinding(Class<S> returnType) {
		return instanceRegistry.createPendingDataBinding(returnType);
	}

	public InstanceRegistry getInstanceRegistry() {
		return instanceRegistry;
	}

	public void setInstanceRegistry(InstanceRegistry instanceRegistry) {
		this.instanceRegistry = instanceRegistry;
	}


}
