## Service Architecture Model

A formal model for definition and execution of software architectures.

Architecture specification is written in code (currently java). It is possible to compile and analyze provided definitions. On future some soft formal specification paradigm are planned.

## Current limitations

Lazy evaluation of external methods calls can change the execution semantic of the program. It is possible to preserve program semantic on base of the architecture definitions, but it is not implemented in the current versions.

## TODO

- Business logic exceptions transport
- Architecture compilation
- Custom binary format for method calls serialization
- Execution model related to architecture semantic constrains

## Why SAM is different???

Service Implementations are injection modules providing implementations of Service Specification interfaces. The real magic is in the definition and use of a canonical execution protocol to communicate between service instance. Execution of method `simpleMethod` below

```java
public class CallToExternalService {

    @Inject
	private ExternalService coreService;

	public boolean simpleMethod(int counter) {
		coreService.resetProcess();
		for (int i = 0; i < internalCounter; i++) {
			coreService.putData(i);
		}
		return coreService.isProcessStatusOk();
	}

}
```
is serialized with the canonical protocol to a request containing information about executed methods and created instance. Such request information may be written as:

<pre>
i_0.resetProcess()
i_0.putData(i_1)
...
i_0.putData(i_n)
i_0.isProcessStatusOk()r_0
PAYLOAD
i_0=KEY<CoreServiceExample X 0>
i_1="IntegerSerialization"
...
i_n="IntegerSerialization"
r_0=Type<boolean>
</pre>

As You may note, this is the necessary information to create a instance on a injector provider and execute all method calls.

## It is more that just lazy execution of method calls.

It is possible to pass external reference between different external services. This provide new interaction possibilities between service instance without any additional overhead on service implementations.

To see the pass of reference between services run the test `pmsoft.sam.see.api.TestSEEexecution.testCourierSetup`. The test presents interaction between a Client, a Store service and a Courier Service. Store and Courier service are no directly interconnected. The Client object have a injected reference to each of this services:

```java
public class TestShoppingStoreWithCourierInteraction implements ShoppingStoreWithCourierInteraction {

    private final CourierServiceContract courier;
    private final StoreServiceContract store;

    @Inject
    public TestShoppingStoreWithCourierInteraction(CourierServiceContract courier, StoreServiceContract store) {
        super();
        this.courier = courier;
        this.store = store;
    }

    @Override
    public Integer makeShoping() {
        StoreOrder order = store.createNewOrder();
        order.addProduct("a", 1);
        order.addProduct("b", 1);

        CourierServiceOrder contract = courier.openOrder("myContractId");
        CourierAddressSetupInfo address = contract.setupAddress();
        boolean addressSetupOk = order.receiveExternalCourierService(address);
        if (!addressSetupOk) {
            throw new RuntimeException("address setup interaction failed");
        }
        Integer price = contract.getServicePrice();
        System.err.println("order price is " + price);
        String orderDone = order.realizeOrder();
        System.err.println("My order contains:" + orderDone);
        return price;
    }
}
```
In this test:
1. The StoreOrder reference is extracted from the Store service.
2. Some products are selected from the store
3. A CourierServiceOrder is open in the Courier service.
4. A CourierAddressSetupInfo reference is extracted from the Courier service and passed to the StoreOrder reference.
5. The CourierServiceOrder object provide a calculated price according to the information setup during Courier--Store interaction.
6. the Store order is realized.

The Courier--Store service interaction is performed passing methods calls information across the Client service Instance.

The execution logs showing the use of the Canonical Protocol is showed below:
```
2705 [main] DEBUG pmsoft.execution.ThreadExecutionInfrastructure - New server instance, adress 0.0.0.0/0.0.0.0:4989
2712 [main] DEBUG pmsoft.execution.ThreadExecutionServer - starting local server on address 0.0.0.0/0.0.0.0:4989
2924 [main] DEBUG pmsoft.execution.ThreadExecutionInfrastructure - New server instance, adress 0.0.0.0/0.0.0.0:4988
2934 [main] DEBUG pmsoft.execution.ThreadExecutionServer - starting local server on address 0.0.0.0/0.0.0.0:4988
3091 [main] DEBUG pmsoft.execution.ThreadExecutionInfrastructure - New server instance, adress 0.0.0.0/0.0.0.0:4987
3093 [main] DEBUG pmsoft.execution.ThreadExecutionServer - starting local server on address 0.0.0.0/0.0.0.0:4987
3254 [pool-5-thread-1] DEBUG pmsoft.sam.protocol.record.ClientExecutionStackManager - recording call MethodCall[1][#0.createNewOrder()#1]
3254 [pool-5-thread-1] DEBUG pmsoft.sam.protocol.record.ClientExecutionStackManager - recording call MethodCall[1][#1.addProduct(#2#3)#-1]
3254 [pool-5-thread-1] DEBUG pmsoft.sam.protocol.record.ClientExecutionStackManager - recording call MethodCall[1][#1.addProduct(#4#5)#-1]
3255 [pool-5-thread-1] DEBUG pmsoft.sam.protocol.record.ClientExecutionStackManager - recording call MethodCall[0][#0.openOrder(#1)#2]
3255 [pool-5-thread-1] DEBUG pmsoft.sam.protocol.record.ClientExecutionStackManager - slotChange
3256 [pool-5-thread-1] DEBUG pmsoft.execution.ThreadMessagePipe - sending message: 1:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4988/service0
ThreadMessage{payload=CanonicalProtocolRequestData [
instanceReferences=[
#0->BindingKeyInstanceReference [key=SerializableKey [type=interface pmsoft.sam.see.api.data.architecture.contract.store.StoreServiceContract, annotationType=null, annotationInstance=null], instanceNr=0]
#1->BindingKeyInstanceReference [key=SerializableKey [type=interface pmsoft.sam.see.api.data.architecture.contract.store.StoreOrder, annotationType=null, annotationInstance=null], instanceNr=1]
#2->ClientDataObjectInstanceReference [ instanceNr=2]objectReference=a
#3->ClientDataObjectInstanceReference [ instanceNr=3]objectReference=1
#4->ClientDataObjectInstanceReference [ instanceNr=4]objectReference=b
#5->ClientDataObjectInstanceReference [ instanceNr=5]objectReference=1
]methodCalls=[
MethodCall[1][#0.createNewOrder()#1]
MethodCall[1][#1.addProduct(#2#3)#-1]
MethodCall[1][#1.addProduct(#4#5)#-1]
]}
3258 [pool-5-thread-1] DEBUG pmsoft.sam.protocol.record.ClientExecutionStackManager - recording call MethodCall[0][#2.setupAddress()#3]
3258 [pool-5-thread-1] DEBUG pmsoft.sam.protocol.record.ClientExecutionStackManager - recording call MethodCall[1][#1.receiveExternalCourierService(#6)#7]
3258 [pool-5-thread-1] DEBUG pmsoft.sam.protocol.record.ClientExecutionStackManager - slotChange
3268 [nioEventLoopGroup-12-2] DEBUG pmsoft.execution.ThreadMessagePipe - receive message: 1:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4988/service0
3269 [pool-6-thread-1] DEBUG pmsoft.sam.protocol.record.ProviderExecutionStackManager - execution method call MethodCall[1][#0.createNewOrder()#1]
3269 [pool-6-thread-1] DEBUG pmsoft.sam.protocol.record.ProviderExecutionStackManager - execution method call MethodCall[1][#1.addProduct(#2#3)#-1]
3269 [pool-6-thread-1] DEBUG pmsoft.sam.protocol.record.ProviderExecutionStackManager - execution method call MethodCall[1][#1.addProduct(#4#5)#-1]
3270 [pool-6-thread-1] DEBUG pmsoft.execution.ThreadMessagePipe - sending message: 1:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4988/service0
ThreadMessage{}
3272 [nioEventLoopGroup-16-2] DEBUG pmsoft.execution.ThreadMessagePipe - receive message: 1:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4988/service0
3273 [pool-5-thread-1] DEBUG pmsoft.execution.ThreadMessagePipe - sending message: 0:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4987/service0
ThreadMessage{payload=CanonicalProtocolRequestData [
instanceReferences=[
#0->BindingKeyInstanceReference [key=SerializableKey [type=interface pmsoft.sam.see.api.data.architecture.contract.courier.CourierServiceContract, annotationType=null, annotationInstance=null], instanceNr=0]
#1->ClientDataObjectInstanceReference [ instanceNr=1]objectReference=myContractId
#2->BindingKeyInstanceReference [key=SerializableKey [type=interface pmsoft.sam.see.api.data.architecture.contract.courier.CourierServiceOrder, annotationType=null, annotationInstance=null], instanceNr=2]
#3->BindingKeyInstanceReference [key=SerializableKey [type=interface pmsoft.sam.see.api.data.architecture.contract.courier.CourierAddressSetupInfo, annotationType=null, annotationInstance=null], instanceNr=3]
]methodCalls=[
MethodCall[0][#0.openOrder(#1)#2]
MethodCall[0][#2.setupAddress()#3]
]}
3281 [nioEventLoopGroup-14-2] DEBUG pmsoft.execution.ThreadMessagePipe - receive message: 0:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4987/service0
3282 [pool-7-thread-1] DEBUG pmsoft.sam.protocol.record.ProviderExecutionStackManager - execution method call MethodCall[0][#0.openOrder(#1)#2]
3282 [pool-7-thread-1] DEBUG pmsoft.sam.protocol.record.ProviderExecutionStackManager - execution method call MethodCall[0][#2.setupAddress()#3]
3283 [pool-7-thread-1] DEBUG pmsoft.execution.ThreadMessagePipe - sending message: 0:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4987/service0
ThreadMessage{}
3285 [nioEventLoopGroup-15-2] DEBUG pmsoft.execution.ThreadMessagePipe - receive message: 0:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4987/service0
3286 [pool-5-thread-1] DEBUG pmsoft.execution.ThreadMessagePipe - sending message: 1:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4988/service0
ThreadMessage{payload=CanonicalProtocolRequestData [
instanceReferences=[
#6->ExternalSlotInstanceReference [key=SerializableKey [type=interface pmsoft.sam.see.api.data.architecture.contract.courier.CourierAddressSetupInfo, annotationType=null, annotationInstance=null], instanceNr=6]
#7->PendingDataInstanceReference [dataType=boolean, instanceNr=7]
]methodCalls=[
MethodCall[1][#1.receiveExternalCourierService(#6)#7]
]}
3296 [nioEventLoopGroup-12-2] DEBUG pmsoft.execution.ThreadMessagePipe - receive message: 1:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4988/service0
3296 [pool-6-thread-1] DEBUG pmsoft.sam.protocol.record.ProviderExecutionStackManager - execution method call MethodCall[1][#1.receiveExternalCourierService(#6)#7]
3296 [pool-6-thread-1] DEBUG pmsoft.sam.protocol.record.ProviderExecutionStackManager - recording call MethodCall[0][#6.setCity(#8)#-1]
3296 [pool-6-thread-1] DEBUG pmsoft.sam.protocol.record.ProviderExecutionStackManager - recording call MethodCall[0][#6.setStreet(#9)#-1]
3297 [pool-6-thread-1] DEBUG pmsoft.sam.protocol.record.ProviderExecutionStackManager - recording call MethodCall[0][#6.setPackageSize(#10)#-1]
3297 [pool-6-thread-1] DEBUG pmsoft.sam.protocol.record.ProviderExecutionStackManager - recording call MethodCall[0][#6.setupDone()#11]
3297 [pool-6-thread-1] DEBUG pmsoft.execution.ThreadMessagePipe - sending message: 1:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4988/service0
ThreadMessage{payload=CanonicalProtocolRequestData [
instanceReferences=[
#8->ServerDataObjectInstanceReference [ instanceNr=8]objectReference=Warsaw
#9->ServerDataObjectInstanceReference [ instanceNr=9]objectReference=Gorczewska
#10->ServerDataObjectInstanceReference [ instanceNr=10]objectReference=3
#11->ServerPendingDataInstanceReference [instanceNr=11]
]methodCalls=[
MethodCall[0][#6.setCity(#8)#-1]
MethodCall[0][#6.setStreet(#9)#-1]
MethodCall[0][#6.setPackageSize(#10)#-1]
MethodCall[0][#6.setupDone()#11]
]}
3318 [nioEventLoopGroup-16-2] DEBUG pmsoft.execution.ThreadMessagePipe - receive message: 1:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4988/service0
3319 [pool-5-thread-1] DEBUG pmsoft.sam.protocol.record.ClientExecutionStackManager - execution method call MethodCall[0][#6.setCity(#8)#-1]
3319 [pool-5-thread-1] DEBUG pmsoft.sam.protocol.record.ClientExecutionStackManager - recording call MethodCall[0][#3.setCity(#4)#-1]
3319 [pool-5-thread-1] DEBUG pmsoft.sam.protocol.record.ClientExecutionStackManager - execution method call MethodCall[0][#6.setStreet(#9)#-1]
3319 [pool-5-thread-1] DEBUG pmsoft.sam.protocol.record.ClientExecutionStackManager - recording call MethodCall[0][#3.setStreet(#5)#-1]
3319 [pool-5-thread-1] DEBUG pmsoft.sam.protocol.record.ClientExecutionStackManager - execution method call MethodCall[0][#6.setPackageSize(#10)#-1]
3320 [pool-5-thread-1] DEBUG pmsoft.sam.protocol.record.ClientExecutionStackManager - recording call MethodCall[0][#3.setPackageSize(#6)#-1]
3320 [pool-5-thread-1] DEBUG pmsoft.sam.protocol.record.ClientExecutionStackManager - execution method call MethodCall[0][#6.setupDone()#11]
3320 [pool-5-thread-1] DEBUG pmsoft.sam.protocol.record.ClientExecutionStackManager - recording call MethodCall[0][#3.setupDone()#7]
3320 [pool-5-thread-1] DEBUG pmsoft.execution.ThreadMessagePipe - sending message: 0:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4987/service0
ThreadMessage{payload=CanonicalProtocolRequestData [
instanceReferences=[
#4->ClientDataObjectInstanceReference [ instanceNr=4]objectReference=Warsaw
#5->ClientDataObjectInstanceReference [ instanceNr=5]objectReference=Gorczewska
#6->ClientDataObjectInstanceReference [ instanceNr=6]objectReference=3
#7->PendingDataInstanceReference [dataType=boolean, instanceNr=7]
]methodCalls=[
MethodCall[0][#3.setCity(#4)#-1]
MethodCall[0][#3.setStreet(#5)#-1]
MethodCall[0][#3.setPackageSize(#6)#-1]
MethodCall[0][#3.setupDone()#7]
]}
3332 [nioEventLoopGroup-14-2] DEBUG pmsoft.execution.ThreadMessagePipe - receive message: 0:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4987/service0
3333 [pool-7-thread-1] DEBUG pmsoft.sam.protocol.record.ProviderExecutionStackManager - execution method call MethodCall[0][#3.setCity(#4)#-1]
3333 [pool-7-thread-1] DEBUG pmsoft.sam.protocol.record.ProviderExecutionStackManager - execution method call MethodCall[0][#3.setStreet(#5)#-1]
3333 [pool-7-thread-1] DEBUG pmsoft.sam.protocol.record.ProviderExecutionStackManager - execution method call MethodCall[0][#3.setPackageSize(#6)#-1]
3333 [pool-7-thread-1] DEBUG pmsoft.sam.protocol.record.ProviderExecutionStackManager - execution method call MethodCall[0][#3.setupDone()#7]
3334 [pool-7-thread-1] DEBUG pmsoft.execution.ThreadMessagePipe - sending message: 0:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4987/service0
ThreadMessage{payload=CanonicalProtocolRequestData [
instanceReferences=[
#7->FilledDataInstanceReference [instanceNr=7, getObjectReference()=true]
]}
3339 [nioEventLoopGroup-15-2] DEBUG pmsoft.execution.ThreadMessagePipe - receive message: 0:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4987/service0
3340 [pool-5-thread-1] DEBUG pmsoft.sam.protocol.record.MethodRecordContext - find return object for MethodCall[0][#3.setupDone()#7]
3340 [pool-5-thread-1] DEBUG pmsoft.execution.ThreadMessagePipe - sending message: 1:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4988/service0
ThreadMessage{payload=CanonicalProtocolRequestData [
instanceReferences=[
#11->FilledDataInstanceReference [instanceNr=11, getObjectReference()=true]
]}
3344 [nioEventLoopGroup-12-2] DEBUG pmsoft.execution.ThreadMessagePipe - receive message: 1:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4988/service0
3345 [pool-6-thread-1] DEBUG pmsoft.sam.protocol.record.MethodRecordContext - find return object for MethodCall[0][#6.setupDone()#11]
3345 [pool-6-thread-1] DEBUG pmsoft.execution.ThreadMessagePipe - sending message: 1:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4988/service0
ThreadMessage{payload=CanonicalProtocolRequestData [
instanceReferences=[
#7->FilledDataInstanceReference [instanceNr=7, getObjectReference()=true]
]}
3350 [nioEventLoopGroup-16-2] DEBUG pmsoft.execution.ThreadMessagePipe - receive message: 1:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4988/service0
3350 [pool-5-thread-1] DEBUG pmsoft.sam.protocol.record.MethodRecordContext - find return object for MethodCall[1][#1.receiveExternalCourierService(#6)#7]
3350 [pool-5-thread-1] DEBUG pmsoft.sam.protocol.record.ClientExecutionStackManager - recording call MethodCall[0][#2.getServicePrice()#8]
3351 [pool-5-thread-1] DEBUG pmsoft.execution.ThreadMessagePipe - sending message: 0:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4987/service0
ThreadMessage{payload=CanonicalProtocolRequestData [
instanceReferences=[
#8->PendingDataInstanceReference [dataType=class java.lang.Integer, instanceNr=8]
]methodCalls=[
MethodCall[0][#2.getServicePrice()#8]
]}
3358 [nioEventLoopGroup-14-2] DEBUG pmsoft.execution.ThreadMessagePipe - receive message: 0:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4987/service0
3360 [pool-7-thread-1] DEBUG pmsoft.sam.protocol.record.ProviderExecutionStackManager - execution method call MethodCall[0][#2.getServicePrice()#8]
3361 [pool-7-thread-1] DEBUG pmsoft.execution.ThreadMessagePipe - sending message: 0:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4987/service0
ThreadMessage{payload=CanonicalProtocolRequestData [
instanceReferences=[
#8->FilledDataInstanceReference [instanceNr=8, getObjectReference()=8800]
]}
3366 [nioEventLoopGroup-15-2] DEBUG pmsoft.execution.ThreadMessagePipe - receive message: 0:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4987/service0
3367 [pool-5-thread-1] DEBUG pmsoft.sam.protocol.record.MethodRecordContext - find return object for MethodCall[0][#2.getServicePrice()#8]
order price is 8800
3367 [pool-5-thread-1] DEBUG pmsoft.sam.protocol.record.ClientExecutionStackManager - recording call MethodCall[1][#1.realizeOrder()#12]
3367 [pool-5-thread-1] DEBUG pmsoft.execution.ThreadMessagePipe - sending message: 1:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4988/service0
ThreadMessage{payload=CanonicalProtocolRequestData [
instanceReferences=[
#12->PendingDataInstanceReference [dataType=class java.lang.String, instanceNr=12]
]methodCalls=[
MethodCall[1][#1.realizeOrder()#12]
]}
3372 [nioEventLoopGroup-12-2] DEBUG pmsoft.execution.ThreadMessagePipe - receive message: 1:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4988/service0
3372 [pool-6-thread-1] DEBUG pmsoft.sam.protocol.record.ProviderExecutionStackManager - execution method call MethodCall[1][#1.realizeOrder()#12]
3373 [pool-6-thread-1] DEBUG pmsoft.execution.ThreadMessagePipe - sending message: 1:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4988/service0
ThreadMessage{payload=CanonicalProtocolRequestData [
instanceReferences=[
#12->FilledDataInstanceReference [instanceNr=12, getObjectReference()=product:b-1
product:a-1
]
]}
3377 [nioEventLoopGroup-16-2] DEBUG pmsoft.execution.ThreadMessagePipe - receive message: 1:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4988/service0
3377 [pool-5-thread-1] DEBUG pmsoft.sam.protocol.record.MethodRecordContext - find return object for MethodCall[1][#1.realizeOrder()#12]
My order contains:product:b-1
product:a-1

3378 [pool-5-thread-1] DEBUG pmsoft.execution.ThreadMessagePipe - notify for dead connection : 0:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4987/service0
3378 [pool-5-thread-1] DEBUG pmsoft.execution.ThreadMessagePipe - notify for dead connection : 1:1b76e67b-34e2-4cd8-99d6-0f3d8cffa8f7-->http://0.0.0.0:4988/service0
3378 [main] DEBUG pmsoft.execution.ThreadExecutionServer - shutdown local server on address 0.0.0.0/0.0.0.0:4989
3388 [main] DEBUG pmsoft.execution.ThreadExecutionServer - shutdown local server on address 0.0.0.0/0.0.0.0:4988
3394 [main] DEBUG pmsoft.execution.ThreadExecutionServer - shutdown local server on address 0.0.0.0/0.0.0.0:4987
```