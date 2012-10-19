package pmsoft.sam.canonical.deprecated.api.execution;

import static com.google.common.base.Preconditions.checkState;
import pmsoft.sam.canonical.deprecated.api.common.AbstractSlotContext;
import pmsoft.sam.protocol.injection.MethodRecordContext;
import pmsoft.sam.protocol.injection.internal.ir.CanonicalInstanceRecorder;
import pmsoft.sam.protocol.injection.internal.ir.InstanceRegistry;

import com.google.common.base.Preconditions;
import com.google.inject.Key;

public class SlotExecutionContext extends AbstractSlotContext {

	private InstanceRegistry executionInstanceRegistry;
	private InstanceRegistry recordingInstanceRegistry;

	public SlotExecutionContext(MethodRecordContext executionContext) {
		super(executionContext, 0);
	}
	

	// private final RecordInstanceRegistry secondaryInstanceRegistry;

	// public SlotExecutionContext(MethodRecordContext executionContext, int
	// serviceSlotNrInTransaction,
	// ExecutionInstanceRegistry mainInstanceRegistry) {
	// super(executionContext, serviceSlotNrInTransaction);
	// this.mainInstanceRegistry = mainInstanceRegistry;
	// // this.secondaryInstanceRegistry = secondaryInstanceRegistry;
	// }

	// public RecordInstanceRegistry getDelegateRecordRegistry() {
	// return secondaryInstanceRegistry;
	// }

	@Override
	protected int[] parseArguments(Object[] args) {
		Preconditions.checkNotNull(executionInstanceRegistry);
		// TODO pobierac instance dla objektow
		checkState(false, "TODO");
		return null;
	}

	// private int[] parseArguments(Object[] args) {
	// int[] argumentNrs = new int[args.length];
	// for (int i = 0; i < args.length; i++) {
	// Object arg = args[i];
	// if (arg == null) {
	// argumentNrs[i] = 0;
	// }
	// // Extract the real object from switching proxy's
	// if (arg instanceof Proxy) {
	// Proxy parg = (Proxy) arg;
	// InvocationHandler handler = Proxy.getInvocationHandler(parg);
	// if (handler instanceof ExternalBindingSwitch) {
	// ExternalBindingSwitch<?> switchProxy = (ExternalBindingSwitch<?>)
	// handler;
	// arg = switchProxy.getInternalInstance();
	// }
	// }
	//
	// if (arg instanceof Proxy) {
	// Proxy parg = (Proxy) arg;
	// InvocationHandler handler = Proxy.getInvocationHandler(parg);
	// if (handler instanceof CanonicalInstanceRecorder) {
	// CanonicalInstanceRecorder<?> recorder = (CanonicalInstanceRecorder<?>)
	// handler;
	// if (recorder.getServiceSlotNr() == serviceSlotNrInTransaction) {
	// argumentNrs[i] = recorder.getServiceInstanceNr();
	// } else {
	// // FIXME repeating external references
	// argumentNrs[i] =
	// createExternalInstanceBinding(recorder.getServiceSlotNr(),
	// recorder.getServiceInstanceNr(),recorder.getKey());
	// }
	// } else {
	// throw new RuntimeException("Unknow Proxy type");
	// }
	// } else if (arg instanceof Serializable) {
	// // FIXME repeating object references
	// argumentNrs[i] = createDataBinding(arg);
	// } else {
	// throw new
	// RuntimeException("argument not serializable be canonical protocol");
	// }
	// }
	// return argumentNrs;
	// }

	@Override
	protected <S> CanonicalInstanceRecorder<S> createRecordInstance(Key<S> key) {
		return executionInstanceRegistry.createKeyBinding(key);
	}

	@Override
	protected <S> int createPendingDataBinding(Class<S> returnType) {
		return executionInstanceRegistry.createPendingDataBinding(returnType);
	}


	public InstanceRegistry getExecutionInstanceRegistry() {
		return executionInstanceRegistry;
	}


	public void setExecutionInstanceRegistry(InstanceRegistry executionInstanceRegistry) {
		this.executionInstanceRegistry = executionInstanceRegistry;
	}


	public InstanceRegistry getRecordingInstanceRegistry() {
		return recordingInstanceRegistry;
	}


	public void setRecordingInstanceRegistry(InstanceRegistry recordingInstanceRegistry) {
		this.recordingInstanceRegistry = recordingInstanceRegistry;
	}

}
