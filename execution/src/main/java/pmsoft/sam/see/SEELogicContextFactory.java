package pmsoft.sam.see;

import com.google.inject.Inject;
import pmsoft.execution.ExecutionContextInternalLogic;
import pmsoft.execution.InternalLogicContextFactory;
import pmsoft.sam.protocol.CanonicalProtocolExecutionContext;
import pmsoft.sam.see.api.SamExecutionNode;
import pmsoft.sam.see.api.model.SIURL;

import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: pawel
 * Date: 1/4/13
 * Time: 4:30 PM
 */
public class SEELogicContextFactory implements InternalLogicContextFactory {

    private final SamExecutionNode localExecutionNode;

    @Inject
    public SEELogicContextFactory(SamExecutionNode localExecutionNode) {
        this.localExecutionNode = localExecutionNode;
    }

    @Override
    public ExecutionContextInternalLogic open(URL targetURL) {
        SIURL siurl = SIURL.fromUrl(targetURL);
        CanonicalProtocolExecutionContext recordTransactionContext = localExecutionNode.createTransactionExecutionContext(siurl);
        return recordTransactionContext;
    }
}
