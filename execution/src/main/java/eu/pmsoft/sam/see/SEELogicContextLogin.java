package eu.pmsoft.sam.see;

import com.google.inject.Inject;
import eu.pmsoft.execution.ThreadExecutionContextInternalLogic;
import eu.pmsoft.execution.ThreadExecutionLoginProvider;
import eu.pmsoft.sam.protocol.CanonicalProtocolThreadExecutionContext;
import eu.pmsoft.sam.see.api.SamExecutionNode;
import eu.pmsoft.sam.see.api.model.SIURL;

import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: pawel
 * Date: 1/4/13
 * Time: 4:30 PM
 */
public class SEELogicContextLogin implements ThreadExecutionLoginProvider {

    private final SamExecutionNode localExecutionNode;

    @Inject
    public SEELogicContextLogin(SamExecutionNode localExecutionNode) {
        this.localExecutionNode = localExecutionNode;
    }

    @Override
    public ThreadExecutionContextInternalLogic open(URL targetURL) {
        SIURL siurl = SIURL.fromUrl(targetURL);
        CanonicalProtocolThreadExecutionContext recordTransactionContext = localExecutionNode.createTransactionExecutionContext(siurl);
        return recordTransactionContext;
    }
}
