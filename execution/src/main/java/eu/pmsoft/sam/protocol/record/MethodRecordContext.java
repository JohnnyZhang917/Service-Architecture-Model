package eu.pmsoft.sam.protocol.record;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.assistedinject.Assisted;
import eu.pmsoft.sam.protocol.freebinding.ExternalBindingSwitch;
import eu.pmsoft.sam.protocol.transport.CanonicalInstanceReference;
import eu.pmsoft.sam.protocol.transport.CanonicalMethodCall;
import eu.pmsoft.sam.protocol.transport.data.CanonicalProtocolSerialFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;

public class MethodRecordContext {

    private final ImmutableList<InstanceRegistry> instanceRegistries;
    private final AbstractExecutionStackManager executionManager;
    private final Logger logger;

    @Inject
    MethodRecordContext(@Assisted ImmutableList<InstanceRegistry> instanceRegistries, @Assisted AbstractExecutionStackManager executionManager) {
        this.instanceRegistries = instanceRegistries;
        this.executionManager = executionManager;
        for (Iterator<InstanceRegistry> iterator = instanceRegistries.iterator(); iterator.hasNext(); ) {
            InstanceRegistry registry = iterator.next();
            registry.setMethodRecordContext(this);
        }
        this.logger = LoggerFactory.getLogger(MethodRecordContext.class);
    }

    public AbstractExecutionStackManager getExecutionManager() {
        return executionManager;
    }

    private InstanceRegistry getInstanceRegistry(int slotNumber) {
        return instanceRegistries.get(slotNumber);
    }

    private Object getMethodCallReturnObject(CanonicalMethodCall call) {
        logger.debug("find return object for {}", call);
        InstanceRegistry instanceRegistry = getInstanceRegistry(call.getSlotNr());
        CanonicalInstanceReference reference = instanceRegistry.getInstanceReference(call.getReturnInstanceId());

        if (reference.getInstanceType() == CanonicalInstanceReference.InstanceType.CLIENT_DATA_OBJECT ||
                reference.getInstanceType() == CanonicalInstanceReference.InstanceType.FILLED_DATA_INSTANCE) {
            logger.trace("execution complete for call {}. reference is {}", call, reference);
            return instanceRegistry.getInstanceObject(call.getReturnInstanceId());
        }
        logger.error("execution incomplete for call {}. reference has type {}", call, reference.getInstanceType());
        throw new RuntimeException("reference is not filled and thread escaped");
    }

    <T> void recordExternalMethodCall(int serviceSlotNr, int serviceInstanceNr, Method method, Object[] args) {
        int[] arguments = null;
        if (args != null && args.length > 0) {
            arguments = parseArguments(serviceSlotNr, args);
        }
        CanonicalMethodCall call = CanonicalProtocolSerialFactory.createMethodCall(serviceSlotNr, serviceInstanceNr, -1, method, arguments);
        executionManager.pushMethodCall(call);

    }


    <T, S> CanonicalInstanceRecorder<S> recordExternalMethodCallWithInterfaceReturnType(int serviceSlotNr, int serviceInstanceNr,
                                                                                        Method method, Object[] args, Class<S> returnType) {
        int[] arguments = null;
        if (args != null && args.length > 0) {
            arguments = parseArguments(serviceSlotNr, args);
        }
        CanonicalInstanceRecorder<S> returnRecorder = getInstanceRegistry(serviceSlotNr).createKeyBinding(Key.get(returnType));
        CanonicalMethodCall call = CanonicalProtocolSerialFactory.createMethodCall(serviceSlotNr, serviceInstanceNr, returnRecorder.getServiceInstanceNr(), method, arguments);
        executionManager.pushMethodCall(call);
        return returnRecorder;
    }

    @SuppressWarnings("unchecked")
    <T, S> S recordExternalMethodCallWithReturnType(int serviceSlotNr, int serviceInstanceNr, Method method, Object[] args,
                                                    Class<S> returnType) {
        int[] arguments = null;
        if (args != null && args.length > 0) {
            arguments = parseArguments(serviceSlotNr, args);
        }
        int returnDataNr = getInstanceRegistry(serviceSlotNr).createPendingDataBinding(returnType);
        CanonicalMethodCall call = CanonicalProtocolSerialFactory.createMethodCall(serviceSlotNr, serviceInstanceNr, returnDataNr, method, arguments);
        executionManager.forceResolveCall(call);
        return (S) getMethodCallReturnObject(call);

    }


    protected int[] parseArguments(int serviceSlotNrContext, Object[] args) {
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
                    if (recorder.getServiceSlotNr() == serviceSlotNrContext) {
                        argumentNrs[i] = recorder.getServiceInstanceNr();
                    } else {
                        argumentNrs[i] = createExternalInstanceBinding(serviceSlotNrContext, recorder);
                    }
                } else {
                    throw new RuntimeException("Unknown Proxy type");
                }
            } else if (arg instanceof Serializable) {
                argumentNrs[i] = getInstanceRegistry(serviceSlotNrContext).createDataBinding(arg);
            } else {
                throw new RuntimeException("argument not serializable be canonical protocol");
            }
        }
        return argumentNrs;
    }


    /**
     * A method execution on slot executionSlotNr have a argument that is a reference to a external binding.
     * <p/>
     * Calls realized in the external service must go on top of the execution stack.
     */
    public int createExternalInstanceBinding(int executionSlotNr, CanonicalInstanceRecorder<?> externalSlotRecordReference) {
        InstanceRegistry targetInstanceRegistry = getInstanceRegistry(executionSlotNr);
        int externalInstanceNr = targetInstanceRegistry.createExternalInstanceBinding(externalSlotRecordReference.getKey(), externalSlotRecordReference.getInstance());
        // Is it necessary to save more information about the mapping?? the slot
        // context have a reference to the record proxy to make any call so for
        // execution it should be enough
        return externalInstanceNr;
    }

}
