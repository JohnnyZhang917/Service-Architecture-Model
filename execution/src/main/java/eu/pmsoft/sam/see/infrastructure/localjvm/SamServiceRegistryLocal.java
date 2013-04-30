package eu.pmsoft.sam.see.infrastructure.localjvm;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.google.common.collect.Sets.SetView;
import com.google.inject.Module;
import eu.pmsoft.sam.architecture.model.ServiceKey;
import eu.pmsoft.sam.definition.implementation.AbstractSamServiceImplementationDefinition;
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationDefinitionLoader;
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;
import eu.pmsoft.sam.definition.implementation.SamServicePackageLoader;
import eu.pmsoft.sam.definition.service.SamServiceDefinition;
import eu.pmsoft.sam.see.api.infrastructure.SamServiceRegistry;
import eu.pmsoft.sam.see.api.model.SamServiceImplementation;
import eu.pmsoft.sam.see.api.model.SamServiceImplementationKey;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Deprecated
class SamServiceRegistryLocal implements SamServiceRegistry {

    private final Map<SamServiceImplementationKey, ServiceImplementationObject> register = Maps.newHashMap();


    @Override
    public void registerServiceImplementationPackage(SamServiceImplementationPackageContract definition) {
//        OperationContext operationContext = operationReportingFactory.openNestedContext();
//        try {
            Map<SamServiceImplementationKey, ServiceImplementationObject> registerNew = null;
            SamServicePackageLoaderImpl reader = new SamServicePackageLoaderImpl();
            definition.loadContractPackage(reader);
            Set<ServiceImplementationObject> implementations = reader.buildImplementations();
            registerNew = Maps.newHashMapWithExpectedSize(implementations.size());
            for (ServiceImplementationObject serviceImplementation : implementations) {
                registerNew.put(serviceImplementation.getKey(), serviceImplementation);
            }
            SetView<SamServiceImplementationKey> alreadyExistingKeys = Sets.intersection(registerNew.keySet(), register.keySet());
            Preconditions.checkState(alreadyExistingKeys.size() == 0, "Duplicate registration of services with kets [%s]", alreadyExistingKeys);
            register.putAll(registerNew);
//        } catch (OperationRuntimeException operationError) {
//            operationContext.getErrors().addError(operationError);
//        } finally {
//            operationReportingFactory.closeContext(operationContext);
//            if (operationContext.getErrors().hasErrors()) {
//                throw operationContext.getRuntimeError();
//            }
//        }
    }

    @Override
    public SamServiceImplementation getImplementation(SamServiceImplementationKey key) {
        return register.get(key);
    }

    @Deprecated
    private class SamServicePackageLoaderImpl implements SamServicePackageLoader {
//        private List<SamServiceImplementationLoaderImplementationDefinition> implementations = Lists.newArrayList();
//        private final OperationContext operationContext;

//        public SamServicePackageLoaderImpl(OperationContext operationContext) {
//            this.operationContext = operationContext;
//        }

        @Override
        public void registerImplementation(AbstractSamServiceImplementationDefinition<? extends SamServiceDefinition> serviceImplementationContract) {
//            SamServiceImplementationLoaderImplementationDefinition loader = new SamServiceImplementationLoaderImplementationDefinition();
//            serviceImplementationContract.loadServiceImplementationDefinition(loader);
//            implementations.add(loader);

        }

        public Set<ServiceImplementationObject> buildImplementations() {
            ImmutableSet.Builder<ServiceImplementationObject> builder = ImmutableSet.builder();
//            for (SamServiceImplementationLoaderImplementationDefinition definition : implementations) {
//                builder.add(definition.build());
//            }
            return builder.build();
        }

        @Deprecated
        private class SamServiceImplementationLoaderImplementationDefinition
//                implements SamServiceImplementationDefinitionLoader
        {
//            InternalServiceImplementationDefinitionImplementation internal = new InternalServiceImplementationDefinitionImplementation();
            private Class<? extends SamServiceDefinition> serviceDefinition = null;
            private final Set<Class<? extends SamServiceDefinition>> bindings = Sets.newHashSet();
            private Class<? extends Module> module = null;

//            @Override
//            public Contract provideContract(Class<? extends SamServiceDefinition> contract) {
//                if (this.serviceDefinition != null) {
////                    operationContext.getErrors().addError("duplicated call to provideContract on service implementation definition for %s", serviceDefinition);
//                    // TODO
//                }
//                this.serviceDefinition = contract;
//                return internal;
//            }

            public ServiceImplementationObject build() {
                Preconditions.checkState(module != null);
                ServiceKey contract = new ServiceKey(serviceDefinition);
                ImmutableList.Builder<ServiceKey> binds = ImmutableList.builder();
                for (Class<? extends SamServiceDefinition> serviceBind : bindings) {
                    binds.add(new ServiceKey(serviceBind));
                }
                SamServiceImplementationKey key = new SamServiceImplementationKey(module.getName());
                return new ServiceImplementationObject(module, key, contract, binds.build());
            }

//            private class InternalServiceImplementationDefinitionImplementation implements ContractAndModule,Contract {
//
//                @Override
//                public ContractAndModule withBindingsTo(Class<? extends SamServiceDefinition> userService) {
//                    if (!bindings.add(userService)) {
////                        operationContext.getErrors().addError("service binding to contract declared two times for %s", userService);
//                        // TODO
//                    }
//                    return this;
//                }
//
//                @Override
//                public ContractAndModule implementedInModule(Class<? extends Module> serviceImplementationModule) {
//                    if (module != null) {
////                        operationContext.getErrors().addError("duplicated declaration of service implementation module for %s", serviceImplementationModule);
//                        // TODO
//                    }
//                    module = serviceImplementationModule;
//                    return this;
//                }
//            }
        }

    }

    private static class ServiceImplementationObject implements SamServiceImplementation {

        private final Class<? extends Module> module;
        private final SamServiceImplementationKey key;
        private final ServiceKey contract;
        private final ImmutableList<ServiceKey> binds;

        ServiceImplementationObject(Class<? extends Module> module, SamServiceImplementationKey key, ServiceKey contract,
                                    ImmutableList<ServiceKey> binds) {
            this.module = module;
            this.key = key;
            this.contract = contract;
            this.binds = binds;
        }

        @Override
        public Class<? extends Module> getModule() {
            return module;
        }

        @Override
        public SamServiceImplementationKey getKey() {
            return key;
        }

        @Override
        public ServiceKey getSpecificationKey() {
            return contract;
        }

        @Override
        public List<ServiceKey> getBindedServices() {
            return binds;
        }

    }
}
