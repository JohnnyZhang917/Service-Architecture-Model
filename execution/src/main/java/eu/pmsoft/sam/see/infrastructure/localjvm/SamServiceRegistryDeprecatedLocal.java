package eu.pmsoft.sam.see.infrastructure.localjvm;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.google.common.collect.Sets.SetView;
import com.google.inject.Module;
import eu.pmsoft.sam.architecture.model.ServiceKeyDeprecated;
import eu.pmsoft.sam.definition.implementation.AbstractSamServiceImplementationDefinition;
import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract;
import eu.pmsoft.sam.definition.implementation.SamServicePackageLoader;
import eu.pmsoft.sam.definition.service.SamServiceDefinition;
import eu.pmsoft.see.api.infrastructure.SamServiceRegistryDeprecated;
import eu.pmsoft.see.api.model.SamServiceImplementationDeprecated;
import eu.pmsoft.see.api.model.SamServiceImplementationKey;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Deprecated
class SamServiceRegistryDeprecatedLocal implements SamServiceRegistryDeprecated {

    private final Map<SamServiceImplementationKey, ServiceImplementationDeprecatedObject> register = Maps.newHashMap();


    @Override
    public void registerServiceImplementationPackage(SamServiceImplementationPackageContract definition) {
//        OperationContext operationContext = operationReportingFactory.openNestedContext();
//        try {
            Map<SamServiceImplementationKey, ServiceImplementationDeprecatedObject> registerNew = null;
            SamServicePackageLoaderImpl reader = new SamServicePackageLoaderImpl();
            definition.loadContractPackage(reader);
            Set<ServiceImplementationDeprecatedObject> implementations = reader.buildImplementations();
            registerNew = Maps.newHashMapWithExpectedSize(implementations.size());
            for (ServiceImplementationDeprecatedObject serviceImplementation : implementations) {
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
    public SamServiceImplementationDeprecated getImplementation(SamServiceImplementationKey key) {
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

        public Set<ServiceImplementationDeprecatedObject> buildImplementations() {
            ImmutableSet.Builder<ServiceImplementationDeprecatedObject> builder = ImmutableSet.builder();
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

            public ServiceImplementationDeprecatedObject build() {
                Preconditions.checkState(module != null);
                ServiceKeyDeprecated contract = new ServiceKeyDeprecated(serviceDefinition);
                ImmutableList.Builder<ServiceKeyDeprecated> binds = ImmutableList.builder();
                for (Class<? extends SamServiceDefinition> serviceBind : bindings) {
                    binds.add(new ServiceKeyDeprecated(serviceBind));
                }
                SamServiceImplementationKey key = new SamServiceImplementationKey(module.getName());
                return new ServiceImplementationDeprecatedObject(module, key, contract, binds.build());
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

    private static class ServiceImplementationDeprecatedObject implements SamServiceImplementationDeprecated {

        private final Class<? extends Module> module;
        private final SamServiceImplementationKey key;
        private final ServiceKeyDeprecated contract;
        private final ImmutableList<ServiceKeyDeprecated> binds;

        ServiceImplementationDeprecatedObject(Class<? extends Module> module, SamServiceImplementationKey key, ServiceKeyDeprecated contract,
                                              ImmutableList<ServiceKeyDeprecated> binds) {
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
        public ServiceKeyDeprecated getSpecificationKey() {
            return contract;
        }

        @Override
        public List<ServiceKeyDeprecated> getBindedServices() {
            return binds;
        }

    }
}
