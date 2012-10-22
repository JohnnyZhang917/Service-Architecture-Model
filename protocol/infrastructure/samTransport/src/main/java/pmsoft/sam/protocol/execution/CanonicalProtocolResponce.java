package pmsoft.sam.protocol.execution;

public class CanonicalProtocolResponce {

	private final CanonicalProtocolRequestData data;

	public CanonicalProtocolResponce(CanonicalProtocolRequestData data) {
		super();
		this.data = data;
	}

	public CanonicalProtocolRequestData getData() {
		return data;
	}

}
