package eu.pmsoft.sam.see.configuration;

import com.google.common.collect.ImmutableList;

public class SEENodeConfiguration {
    public final ImmutableList<SEEServiceSetupAction> setupActions;

    public SEENodeConfiguration(ImmutableList<SEEServiceSetupAction> setupActions) {
        this.setupActions = setupActions;
    }

    public static SEENodeConfiguration empty() {
        return new SEENodeConfiguration(ImmutableList.<SEEServiceSetupAction>of());
    }
}
