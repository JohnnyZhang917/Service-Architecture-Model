package eu.pmsoft.sam.see;

import com.google.inject.Inject;
import eu.pmsoft.execution.ThreadExecutionContextInternalLogic;
import eu.pmsoft.execution.ThreadExecutionLogicProvider;
import eu.pmsoft.sam.protocol.CanonicalProtocolThreadExecutionContext;
import eu.pmsoft.sam.see.api.SamExecutionNode;
import eu.pmsoft.sam.see.api.model.SIURL;

import java.net.URL;

public class SEELogicContextLogic implements ThreadExecutionLogicProvider {

    private final SamExecutionNode localExecutionNode;

    @Inject
    public SEELogicContextLogic(SamExecutionNode localExecutionNode) {
        this.localExecutionNode = localExecutionNode;
    }

    @Override
    public ThreadExecutionContextInternalLogic open(URL targetURL) {
        SIURL siurl = SIURL.fromUrl(targetURL);
        CanonicalProtocolThreadExecutionContext recordTransactionContext = localExecutionNode.createTransactionExecutionContext(siurl);
        return recordTransactionContext;
    }
}
