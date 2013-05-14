package eu.pmsoft.sam.injection;

public interface ExternalBindingController {

    public void bindRecordContext(ExternalInstanceProvider extrenalProvider);

    public void unBindRecordContext();
}
