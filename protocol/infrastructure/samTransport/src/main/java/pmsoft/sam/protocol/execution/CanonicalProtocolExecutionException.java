package pmsoft.sam.protocol.execution;


/**
 * TODO have a exception handling policy
 * @author pawel
 *
 */
public class CanonicalProtocolExecutionException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 518884599621601620L;

	public CanonicalProtocolExecutionException() {
		super();
	}

	public CanonicalProtocolExecutionException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public CanonicalProtocolExecutionException(String arg0) {
		super(arg0);
	}

	public CanonicalProtocolExecutionException(Throwable arg0) {
		super(arg0);
	}

	
}
