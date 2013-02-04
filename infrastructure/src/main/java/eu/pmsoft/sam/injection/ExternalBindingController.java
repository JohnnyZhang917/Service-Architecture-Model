package eu.pmsoft.sam.injection;

public interface ExternalBindingController {

    public void bindRecordContext(DependenciesBindingContext context);

    public void unBindRecordContext();
}
