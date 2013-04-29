package eu.pmsoft.sam.see.configuration;

public interface SEENodeConfigurationGrammar {

    public SEENodeConfigurationGrammar setupAction(SEEServiceSetupAction action);

    SEENodeConfiguration build();
}
