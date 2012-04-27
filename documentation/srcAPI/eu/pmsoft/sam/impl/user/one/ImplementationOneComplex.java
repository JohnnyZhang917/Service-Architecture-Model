package eu.pmsoft.sam.impl.user.one;

import eu.pmsoft.sam.ds.DomainData;
import eu.pmsoft.sam.ds.DomainTypeExample;
import eu.pmsoft.sam.service.user.UserDataComplexAPI;

public class ImplementationOneComplex implements UserDataComplexAPI {

	private Integer counter = 0;
	private Integer expected = 0;

	public ImplementationOneComplex(Integer mainCounter) {
		expected = mainCounter;
	}

	public void clientInformation(int data) {
		counter += data;
	}

	public boolean finalizeComplexClientInteraction() {
		return counter > expected;
	}

	public DomainTypeExample getDomainData(DomainData data) {
		return new ImplementationOneDomainTypeExample(counter,expected,data);
	}

}
