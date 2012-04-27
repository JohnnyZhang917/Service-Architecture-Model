package eu.pmsoft.sam.impl.user.one;

import eu.pmsoft.sam.ds.DomainData;
import eu.pmsoft.sam.ds.DomainTypeExample;

public class ImplementationOneDomainTypeExample implements DomainTypeExample {

	private String name;
	private Integer counter;
	private Integer expected;
	private DomainData dataReference;

	public ImplementationOneDomainTypeExample(Integer counter,
			Integer expected, DomainData data) {
		this.counter = counter;
		this.expected = expected;
		this.dataReference = data;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isValid() {
		if (dataReference.ignoreConstrains())
			return true;
		return counter < expected;
	}

}
