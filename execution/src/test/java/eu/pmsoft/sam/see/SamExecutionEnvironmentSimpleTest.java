package eu.pmsoft.sam.see;

import com.google.inject.Inject;
import com.google.inject.Injector;
import eu.pmsoft.see.api.SamExecutionEnvironment;
import eu.pmsoft.see.api.setup.SamExecutionNode;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;


public class SamExecutionEnvironmentSimpleTest {

    @Inject
    private SamExecutionEnvironment samExecutionEnvironment;


    @BeforeMethod
    public void setUp() throws Exception {
        Injector injector = com.google.inject.Guice.createInjector(new SamExecutionModuleSimpleModuleTest());
        injector.injectMembers(this);

    }

    @Test
    public void testInfrastructureCreation() {
        assertNotNull(samExecutionEnvironment.getInfrastructureApi());
        assertNotNull(samExecutionEnvironment.createExecutionNode(anyPort()));
    }

    static AtomicInteger portCounter = new AtomicInteger(3333);

    private static int anyPort() {
        return portCounter.addAndGet(1);
    }

    @Test
    public void testNodeNotDuplicated() {
        int samePort = anyPort();
        SamExecutionNode callOne = samExecutionEnvironment.createExecutionNode(samePort);
        SamExecutionNode callTwo = samExecutionEnvironment.createExecutionNode(samePort);
        assertEquals(callOne, callTwo);
    }

//    @Test
//    public void testInfrastructureConfigurationEmpty() {
//        samExecutionEnvironment.getInfrastructureApi().setupInfrastructureConfiguration(SEEConfiguration.empty());
//    }

//    @Test
//    public void testInfrastructureConfigurationSetup() {
//        SamExecutionEnvironmentInfrastructure infrastructureApi = samExecutionEnvironment.getInfrastructureApi();
//
//        SamServiceDeprecated serviceFromArchitecture = infrastructureApi.getArchitectureManager().getService(new ServiceKey(TestServiceOne.class));
//        SamServiceImplementationDeprecated serviceImplementation = infrastructureApi.getServiceRegistry().getImplementation(SamServiceImplementationKey.definedBy(TestServiceZeroModule.class));
//
//        assertNull(serviceFromArchitecture);
//        assertNull(serviceImplementation);
//        SEEConfiguration configuration = TestServiceExecutionEnvironmentConfiguration.createArchitectureConfiguration();
//        infrastructureApi.setupInfrastructureConfiguration(configuration);
//
//        serviceFromArchitecture = infrastructureApi.getArchitectureManager().getService(new ServiceKey(TestServiceOne.class));
//        serviceImplementation = infrastructureApi.getServiceRegistry().getImplementation(SamServiceImplementationKey.definedBy(TestServiceZeroModule.class));
//        assertNotNull(serviceFromArchitecture);
//        assertNotNull(serviceImplementation);
//    }
//
//    @Test( dependsOnMethods = "testInfrastructureConfigurationSetup")
//    public void testNodeConfigurationSetup() {
//        testInfrastructureConfigurationSetup();
//        SEENodeConfiguration nodeConfiguration = getTestNodeConfigurationStoreService();
//        SamExecutionNode executionNode = samExecutionEnvironment.createExecutionNode(anyPort());
//        executionNode.setupConfiguration(nodeConfiguration);
//        SamExecutionEnvironmentInfrastructure infrastructureApi = samExecutionEnvironment.getInfrastructureApi();
//
//        Map<SIURL, ServiceKey> serviceRunningStatus = infrastructureApi.getServiceDiscovery().getServiceRunningStatus();
//        assertNotNull(serviceRunningStatus);
//        Assert.assertTrue(serviceRunningStatus.isEmpty());
//
//        Map<STID, ServiceKey> serviceTransactionSetup = executionNode.getServiceTransactionSetup();
//        assertNotNull(serviceTransactionSetup);
//        Assert.assertTrue(!serviceTransactionSetup.isEmpty());
//        assertEquals(serviceTransactionSetup.size(), 1);
//    }
//
//    @Test( dependsOnMethods = "testInfrastructureConfigurationSetup")
//    public void testSimpleCanonicalProtocolCall() throws ExecutionException {
//        testInfrastructureConfigurationSetup();
//        SamExecutionNode shoppingClientServer = samExecutionEnvironment.createExecutionNode(anyPort());
//        SamExecutionNode storeServer = samExecutionEnvironment.createExecutionNode(anyPort());
//        SamExecutionNode courierServer = samExecutionEnvironment.createExecutionNode(anyPort());
//
//        SIURL storeUrlProvided = createStoreServiceInstance(storeServer);
//        SIURL courierUrlProvided = createCourierServiceInstance(courierServer);
//
//        ServiceKey storeServiceKey = new ServiceKey(StoreService.class);
//        checkServiceDiscoveryStatus(storeUrlProvided, storeServiceKey);
//
//        ServiceKey courierServiceKey = new ServiceKey(CourierService.class);
//        checkServiceDiscoveryStatus(courierUrlProvided, courierServiceKey);
//
//        shoppingClientServer.setupConfiguration(getTestNodeConfigurationShoppingClient(storeUrlProvided, courierUrlProvided));
//
//        //TODO testowac udostepnienie transakcji z referencja do uslug zewnatrznych
////        SIURL shoppingUrl = extractUniqueServiceRunning(new ServiceKey(ShoppingService.class));
//
//        STID shoppingTransactionInstance = extractServiceTID(shoppingClientServer,new ServiceKey(ShoppingService.class));
//        Assert.assertNotNull(shoppingTransactionInstance);
//
//
////        SamExecutionApi shoppingExecutionApi = shoppingClientServer.getExecutionApi();
//
//        ServiceAction<Integer, ShoppingStoreWithCourierInteraction> interaction = new ServiceAction<Integer, ShoppingStoreWithCourierInteraction>(shoppingTransactionInstance,
//                Key.get(ShoppingStoreWithCourierInteraction.class)) {
//
//            @Override
//            public Integer executeInteraction(ShoppingStoreWithCourierInteraction service) {
//                return service.makeShoping();
//            }
//        };
//
////        Future<Integer> future = shoppingExecutionApi.executeServiceAction(interaction);
////        assertNotNull(future);
////        int shoppingCount = 0;
////        while (!future.isDone()) {
////            try {
////                shoppingCount = future.get();
////            } catch (InterruptedException e) {
////                // ignore
////            }
////        }
////        assertTrue(shoppingCount > 0);
//    }
//
//    private void checkServiceDiscoveryStatus(SIURL storeUrlProvided, ServiceKey storeServiceKey) {
//        SIURL storeUrl = extractUniqueServiceRunning(storeServiceKey);
//        Assert.assertNotNull(storeUrl);
//        assertEquals(storeUrlProvided,storeUrl);
//    }
//
//    private SIURL createCourierServiceInstance(SamExecutionNode courierServer) {
//        courierServer.setupConfiguration(getTestNodeConfigurationCourier());
//        STID courierTransactionInstance = extractServiceTID(courierServer,new ServiceKey(CourierService.class));
//        Assert.assertNotNull(courierTransactionInstance);
//        return courierServer.exposeInjectionConfiguration(courierTransactionInstance);
//    }
//
//    private SIURL createStoreServiceInstance(SamExecutionNode storeServer) {
//        storeServer.setupConfiguration(getTestNodeConfigurationStoreService());
//        STID storeTransactionInstance = extractServiceTID(storeServer,new ServiceKey(StoreService.class));
//        Assert.assertNotNull(storeTransactionInstance);
//        return storeServer.exposeInjectionConfiguration(storeTransactionInstance);
//    }
//
//    private STID extractServiceTID(SamExecutionNode executionNode, ServiceKey serviceKey) {
//        Map<STID, ServiceKey> serviceTransactionSetup = executionNode.getServiceTransactionSetup();
//        STID service = findService(serviceKey, serviceTransactionSetup);
//        assertNotNull("can't find service transaction for key " + serviceKey);
//        return service;
//    }
//
//    private SIURL extractUniqueServiceRunning( ServiceKey serviceKey) {
//        SamExecutionEnvironmentInfrastructure infrastructureApi = samExecutionEnvironment.getInfrastructureApi();
//        SamServiceDiscovery serviceDiscovery = infrastructureApi.getServiceDiscovery();
//        Map<SIURL, ServiceKey> services = serviceDiscovery.getServiceRunningStatus();
//        final SIURL url = findService(serviceKey, services);
//        assertNotNull("can't find service for key " + serviceKey, url);
//        return url;
//    }
//
//    private <T> T findService(ServiceKey serviceKey, Map<T, ServiceKey> services) {
//        for (Map.Entry<T, ServiceKey> entry : services.entrySet()) {
//            if (entry.getValue().equals(serviceKey)) {
//                return entry.getKey();
//            }
//        }
//        return null;
//    }
//
//    private SEENodeConfiguration getTestNodeConfigurationStoreService() {
//        return SEEConfigurationBuilder.nodeConfiguration()
//                .setupAction(new SEEServiceSetupAction() {
//                    @Override
//                    public void setup() {
//                        SIID storeInstanceId = createServiceInstance(TestStoreServiceModule.class);
//                        setupServiceTransaction(SamTransactionConfigurationUtil.createTransactionOn(StoreService.class).providedByServiceInstance(storeInstanceId));
//                    }
//                })
//                .build();
//    }
//
//    private SEENodeConfiguration getTestNodeConfigurationCourier() {
//        return SEEConfigurationBuilder.nodeConfiguration()
//                .setupAction(new SEEServiceSetupAction() {
//                    @Override
//                    public void setup() {
//                        SIID courierInstanceId = createServiceInstance(TestCourierServiceModule.class);
//                        setupServiceTransaction(SamTransactionConfigurationUtil.createTransactionOn(CourierService.class).providedByServiceInstance(
//                                courierInstanceId));
//                    }
//                })
//                .build();
//    }
//
//    private SEENodeConfiguration getTestNodeConfigurationShoppingClient( final SIURL storeURL, final SIURL courierURL) {
//        return SEEConfigurationBuilder.nodeConfiguration()
//                .setupAction(new SEEServiceSetupAction() {
//                    @Override
//                    public void setup() {
//                        SIID shoppingInstanceId = createServiceInstance(TestShoppingModule.class);
//
//                        setupServiceTransaction(SamTransactionConfigurationUtil.createTransactionOn(ShoppingService.class).urlBinding(StoreService.class, storeURL)
//                                .urlBinding(CourierService.class, courierURL).providedByServiceInstance(shoppingInstanceId));
//                    }
//                })
//                .build();
//    }
//
//    @DataProvider(name = "executionStrategies")
//    public Object[][] executionStrategies() {
//        return new Object[][]{{ExecutionStrategy.PROCEDURAL}, {ExecutionStrategy.SIMPLE_LAZY}};
//    }


}
