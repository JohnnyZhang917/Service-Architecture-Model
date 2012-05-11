## Service Architecture Model

A formal model for definition of software architectures. 

Architecture specification is written in code (currently only java). It is possible to compile and analyze provided definitions. On future some soft formal specification paradigm are planned.

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

``i_0.resetProcess()
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
``

As You may note, this is the neccesary information to create a instance on a injector provider and execute all method calls.

## It is more that just lazy execution of method calls.

It is possible to pass external reference between different external services. This provide new interaction posibilities between service instance without any additional overhead on service implementations. To show this run the test `pmsoft.sam.module.definition.test.OAuthPrintingPhotoTest` (output given below), it shows the implementation of the OAuth guice example given on http://hueniverse.com/oauth/guide/workflow/

The big difference is that there is not explicit interaction between services faji and beppa. The resource reference passing is made directly at the canonical protocol level, without any additional code on client and external services. The information about service orchestration is given externally by a injection configuration, at the Service Execution Enviroment level. For the current prototype the injection configuration is provided on the test code:
```java
		TransactionConfigurator transaction = see.createTransactionConfiguration(janeGuiInterface);
		InjectionConfiguration configuration = transaction.getInjectionConfiguration();
		configuration.bindExternalInstance(fajiImplementationDefinition.getServiceSpecificationKey(), fajiURL);
		configuration.bindExternalInstance(beppaImplementationDefinition.getServiceSpecificationKey(), beppaURL);

```

## Current limitations

The current prototype implementation is a proof of concept running in a single thread in one JVM. A real distributed implementation is in progress.

## OAuth test output

Output of the OAuth test. Note the external reference exchange between services on the call of method `getPhotoData`.

<pre>
SEE: Jane start the OAuth test
SEE: Setup transactions to run the service interaction
SEE: Execute Jane interaction
JANE: I am back from her Scotland vacation, lets share photos
JANE: registering in PhotoSharingService
CP: Canonical Protocol Execution. Request
CP: ServiceExecutionRequest [forwardCall=true, canonicalTransactionIdentificator=a2a508c5-5722-43eb-90e1-b86d90122e00, targetURL=http://localhost/transaction1, sourceURL=http://localhost/transaction2, data=CanonicalProtocolRequestData [
instanceReferences=[
#0->BindingKeyInstanceReference [key=Key[type=pmsoft.sam.module.definition.test.oauth.service.PhotoSharingAPI, annotation=[none]], instanceNr=0]
#1->DataObjectInstanceReference [ instanceNr=1]objectReference=JANE
#2->PendingDataInstanceReference [dataType=class java.lang.String, instanceNr=2]]
methodCalls=[
MethodCall[#0.register(#1)#2]]
targetSlot=1]
]
CP: Canonical Protocol Execution. Response
CP: CanonicalProtocolRequestData [
instanceReferences=[
#2->FilledDataInstanceReference [instanceNr=2, getObjectReference()=user0]]
methodCalls=[
]
targetSlot=0]

JANE: upload photos
JANE: Lets send the photos to grandmother
JANE: create print photo order
CP: Canonical Protocol Execution. Request
CP: ServiceExecutionRequest [forwardCall=true, canonicalTransactionIdentificator=a2a508c5-5722-43eb-90e1-b86d90122e00, targetURL=http://localhost/transaction1, sourceURL=http://localhost/transaction2, data=CanonicalProtocolRequestData [
instanceReferences=[
#3->DataObjectInstanceReference [ instanceNr=3]objectReference=user0
#4->BindingKeyInstanceReference [key=Key[type=pmsoft.sam.module.definition.test.oauth.service.PhotoSharingAlbum, annotation=[none]], instanceNr=4]
#5->DataObjectInstanceReference [ instanceNr=5]objectReference=landscape photo
#6->DataObjectInstanceReference [ instanceNr=6]objectReference=[B@7f4d1d41
#7->DataObjectInstanceReference [ instanceNr=7]objectReference=water photo
#8->DataObjectInstanceReference [ instanceNr=8]objectReference=[B@1fbbd7b2
#9->PendingDataInstanceReference [dataType=class [Lpmsoft.sam.module.definition.test.oauth.service.PhotoInfo;, instanceNr=9]]
methodCalls=[
MethodCall[#0.getAccessToAlbum(#3)#4]
MethodCall[#4.uploadPhoto(#5#6)#-1]
MethodCall[#4.uploadPhoto(#7#8)#-1]]
targetSlot=1]
]
CP: Canonical Protocol Execution. Response
CP: CanonicalProtocolRequestData [
instanceReferences=[
]
methodCalls=[
]
targetSlot=0]

CP: Canonical Protocol Execution. Request
CP: ServiceExecutionRequest [forwardCall=true, canonicalTransactionIdentificator=a2a508c5-5722-43eb-90e1-b86d90122e00, targetURL=http://localhost/transaction0, sourceURL=http://localhost/transaction2, data=CanonicalProtocolRequestData [
instanceReferences=[
#0->BindingKeyInstanceReference [key=Key[type=pmsoft.sam.module.definition.test.oauth.service.PrintingPhotoAPI, annotation=[none]], instanceNr=0]
#1->BindingKeyInstanceReference [key=Key[type=pmsoft.sam.module.definition.test.oauth.service.PrintingPhotoOrder, annotation=[none]], instanceNr=1]
#2->DataObjectInstanceReference [ instanceNr=2]objectReference=grandmother direction]
methodCalls=[
MethodCall[#0.createPrintingOrder()#1]
MethodCall[#1.addAdressInformation(#2)#-1]]
targetSlot=0]
]
CP: Canonical Protocol Execution. Response
CP: CanonicalProtocolRequestData [
instanceReferences=[
]
methodCalls=[
]
targetSlot=0]

CP: Canonical Protocol Execution. Request
CP: ServiceExecutionRequest [forwardCall=true, canonicalTransactionIdentificator=a2a508c5-5722-43eb-90e1-b86d90122e00, targetURL=http://localhost/transaction1, sourceURL=http://localhost/transaction2, data=CanonicalProtocolRequestData [
instanceReferences=[
]
methodCalls=[
MethodCall[#4.getPhotoList()#9]]
targetSlot=1]
]
CP: Canonical Protocol Execution. Response
CP: CanonicalProtocolRequestData [
instanceReferences=[
#9->FilledDataInstanceReference [instanceNr=9, getObjectReference()=[Lpmsoft.sam.module.definition.test.oauth.service.PhotoInfo;@6655bb93]]
methodCalls=[
]
targetSlot=0]

JANE: send photo resource references
JANE: send photo resource references
JANE: now submit the printing order
CP: Canonical Protocol Execution. Request
CP: ServiceExecutionRequest [forwardCall=true, canonicalTransactionIdentificator=a2a508c5-5722-43eb-90e1-b86d90122e00, targetURL=http://localhost/transaction1, sourceURL=http://localhost/transaction2, data=CanonicalProtocolRequestData [
instanceReferences=[
#10->DataObjectInstanceReference [ instanceNr=10]objectReference=0
#11->BindingKeyInstanceReference [key=Key[type=pmsoft.sam.module.definition.test.oauth.service.PhotoResource, annotation=[none]], instanceNr=11]
#12->DataObjectInstanceReference [ instanceNr=12]objectReference=1
#13->BindingKeyInstanceReference [key=Key[type=pmsoft.sam.module.definition.test.oauth.service.PhotoResource, annotation=[none]], instanceNr=13]]
methodCalls=[
MethodCall[#4.getPhotoSharingResource(#10)#11]
MethodCall[#4.getPhotoSharingResource(#12)#13]]
targetSlot=1]
]
CP: Canonical Protocol Execution. Response
CP: CanonicalProtocolRequestData [
instanceReferences=[
]
methodCalls=[
]
targetSlot=0]

CP: Canonical Protocol Execution. Request
CP: ServiceExecutionRequest [forwardCall=true, canonicalTransactionIdentificator=a2a508c5-5722-43eb-90e1-b86d90122e00, targetURL=http://localhost/transaction0, sourceURL=http://localhost/transaction2, data=CanonicalProtocolRequestData [
instanceReferences=[
#3->ExternalSlotInstanceReference [key=Key[type=pmsoft.sam.module.definition.test.oauth.service.PhotoResource, annotation=[none]], instanceNr=3]
#4->ExternalSlotInstanceReference [key=Key[type=pmsoft.sam.module.definition.test.oauth.service.PhotoResource, annotation=[none]], instanceNr=4]
#5->PendingDataInstanceReference [dataType=boolean, instanceNr=5]]
methodCalls=[
MethodCall[#1.addPhotoResourceReference(#3)#-1]
MethodCall[#1.addPhotoResourceReference(#4)#-1]
MethodCall[#1.submitOrder()#5]]
targetSlot=0]
]
CP: Canonical Protocol Execution. Request
CP: ServiceExecutionRequest [forwardCall=false, canonicalTransactionIdentificator=a2a508c5-5722-43eb-90e1-b86d90122e00, targetURL=http://localhost/transaction2, sourceURL=http://localhost/transaction0, data=CanonicalProtocolRequestData [
instanceReferences=[
#6->ServerBindingKeyInstanceReference [key=Key[type=pmsoft.sam.module.definition.test.oauth.service.PhotoResourceExtended, annotation=[none]], instanceNr=6]
#7->ServerPendingDataInstanceReference [instanceNr=7]]
methodCalls=[
MethodCall[#3.getExtendedApi()#6]
MethodCall[#6.checkNestedInterserviceInteraction()#7]]
targetSlot=0]
]
CP: Canonical Protocol Execution. Request
CP: ServiceExecutionRequest [forwardCall=true, canonicalTransactionIdentificator=a2a508c5-5722-43eb-90e1-b86d90122e00, targetURL=http://localhost/transaction1, sourceURL=http://localhost/transaction2, data=CanonicalProtocolRequestData [
instanceReferences=[
#14->BindingKeyInstanceReference [key=Key[type=pmsoft.sam.module.definition.test.oauth.service.PhotoResourceExtended, annotation=[none]], instanceNr=14]
#15->PendingDataInstanceReference [dataType=boolean, instanceNr=15]]
methodCalls=[
MethodCall[#11.getExtendedApi()#14]
MethodCall[#14.checkNestedInterserviceInteraction()#15]]
targetSlot=1]
]
CP: Canonical Protocol Execution. Response
CP: CanonicalProtocolRequestData [
instanceReferences=[
#15->FilledDataInstanceReference [instanceNr=15, getObjectReference()=true]]
methodCalls=[
]
targetSlot=0]

CP: Canonical Protocol Execution. Response
CP: CanonicalProtocolRequestData [
instanceReferences=[
#7->FilledDataInstanceReference [instanceNr=7, getObjectReference()=true]]
methodCalls=[
]
targetSlot=0]

CP: Canonical Protocol Execution. Request
CP: ServiceExecutionRequest [forwardCall=false, canonicalTransactionIdentificator=a2a508c5-5722-43eb-90e1-b86d90122e00, targetURL=http://localhost/transaction2, sourceURL=http://localhost/transaction0, data=CanonicalProtocolRequestData [
instanceReferences=[
#8->ServerPendingDataInstanceReference [instanceNr=8]]
methodCalls=[
MethodCall[#3.getPhotoData()#8]]
targetSlot=0]
]
CP: Canonical Protocol Execution. Request
CP: ServiceExecutionRequest [forwardCall=true, canonicalTransactionIdentificator=a2a508c5-5722-43eb-90e1-b86d90122e00, targetURL=http://localhost/transaction1, sourceURL=http://localhost/transaction2, data=CanonicalProtocolRequestData [
instanceReferences=[
#16->PendingDataInstanceReference [dataType=class [B, instanceNr=16]]
methodCalls=[
MethodCall[#11.getPhotoData()#16]]
targetSlot=1]
]
CP: Canonical Protocol Execution. Response
CP: CanonicalProtocolRequestData [
instanceReferences=[
#16->FilledDataInstanceReference [instanceNr=16, getObjectReference()=[B@7f4d1d41]]
methodCalls=[
]
targetSlot=0]

CP: Canonical Protocol Execution. Response
CP: CanonicalProtocolRequestData [
instanceReferences=[
#8->FilledDataInstanceReference [instanceNr=8, getObjectReference()=[B@7f4d1d41]]
methodCalls=[
]
targetSlot=0]

Printing photo
LANDSCAPE PHOTO
CP: Canonical Protocol Execution. Request
CP: ServiceExecutionRequest [forwardCall=false, canonicalTransactionIdentificator=a2a508c5-5722-43eb-90e1-b86d90122e00, targetURL=http://localhost/transaction2, sourceURL=http://localhost/transaction0, data=CanonicalProtocolRequestData [
instanceReferences=[
#9->ServerBindingKeyInstanceReference [key=Key[type=pmsoft.sam.module.definition.test.oauth.service.PhotoResourceExtended, annotation=[none]], instanceNr=9]
#10->ServerPendingDataInstanceReference [instanceNr=10]]
methodCalls=[
MethodCall[#4.getExtendedApi()#9]
MethodCall[#9.checkNestedInterserviceInteraction()#10]]
targetSlot=0]
]
CP: Canonical Protocol Execution. Request
CP: ServiceExecutionRequest [forwardCall=true, canonicalTransactionIdentificator=a2a508c5-5722-43eb-90e1-b86d90122e00, targetURL=http://localhost/transaction1, sourceURL=http://localhost/transaction2, data=CanonicalProtocolRequestData [
instanceReferences=[
#17->BindingKeyInstanceReference [key=Key[type=pmsoft.sam.module.definition.test.oauth.service.PhotoResourceExtended, annotation=[none]], instanceNr=17]
#18->PendingDataInstanceReference [dataType=boolean, instanceNr=18]]
methodCalls=[
MethodCall[#13.getExtendedApi()#17]
MethodCall[#17.checkNestedInterserviceInteraction()#18]]
targetSlot=1]
]
CP: Canonical Protocol Execution. Response
CP: CanonicalProtocolRequestData [
instanceReferences=[
#18->FilledDataInstanceReference [instanceNr=18, getObjectReference()=true]]
methodCalls=[
]
targetSlot=0]

CP: Canonical Protocol Execution. Response
CP: CanonicalProtocolRequestData [
instanceReferences=[
#10->FilledDataInstanceReference [instanceNr=10, getObjectReference()=true]]
methodCalls=[
]
targetSlot=0]

CP: Canonical Protocol Execution. Request
CP: ServiceExecutionRequest [forwardCall=false, canonicalTransactionIdentificator=a2a508c5-5722-43eb-90e1-b86d90122e00, targetURL=http://localhost/transaction2, sourceURL=http://localhost/transaction0, data=CanonicalProtocolRequestData [
instanceReferences=[
#11->ServerPendingDataInstanceReference [instanceNr=11]]
methodCalls=[
MethodCall[#4.getPhotoData()#11]]
targetSlot=0]
]
CP: Canonical Protocol Execution. Request
CP: ServiceExecutionRequest [forwardCall=true, canonicalTransactionIdentificator=a2a508c5-5722-43eb-90e1-b86d90122e00, targetURL=http://localhost/transaction1, sourceURL=http://localhost/transaction2, data=CanonicalProtocolRequestData [
instanceReferences=[
#19->PendingDataInstanceReference [dataType=class [B, instanceNr=19]]
methodCalls=[
MethodCall[#13.getPhotoData()#19]]
targetSlot=1]
]
CP: Canonical Protocol Execution. Response
CP: CanonicalProtocolRequestData [
instanceReferences=[
#19->FilledDataInstanceReference [instanceNr=19, getObjectReference()=[B@1fbbd7b2]]
methodCalls=[
]
targetSlot=0]

CP: Canonical Protocol Execution. Response
CP: CanonicalProtocolRequestData [
instanceReferences=[
#11->FilledDataInstanceReference [instanceNr=11, getObjectReference()=[B@1fbbd7b2]]
methodCalls=[
]
targetSlot=0]

Printing photo
WATER PHOTO
Sending photos to:
grandmother direction
Print submition done.
CP: Canonical Protocol Execution. Response
CP: CanonicalProtocolRequestData [
instanceReferences=[
#5->FilledDataInstanceReference [instanceNr=5, getObjectReference()=true]]
methodCalls=[
]
targetSlot=0]
</pre>









