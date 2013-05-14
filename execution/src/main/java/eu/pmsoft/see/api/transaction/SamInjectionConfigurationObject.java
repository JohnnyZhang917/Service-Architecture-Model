package eu.pmsoft.see.api.transaction;

import com.google.common.collect.ImmutableList;
import eu.pmsoft.sam.architecture.model.ServiceKeyDeprecated;
import eu.pmsoft.see.api.model.SIID;

class SamInjectionConfigurationObject implements SamInjectionConfiguration {

    private final ServiceKeyDeprecated exposedService;
    private final SIID exposedInstance;
    private final ImmutableList<BindPoint> bindPoints;

    SamInjectionConfigurationObject(ServiceKeyDeprecated exposedService, SIID exposedInstance, ImmutableList<BindPoint> bindPoints) {
        super();
        this.exposedService = exposedService;
        this.exposedInstance = exposedInstance;
        this.bindPoints = bindPoints;
    }

    @Override
    public ServiceKeyDeprecated getProvidedService() {
        return exposedService;
    }

    @Override
    public SIID getExposedServiceInstance() {
        return exposedInstance;
    }

    @Override
    public ImmutableList<BindPoint> getBindPoints() {
        return bindPoints;
    }

    @Override
    public <T> void accept(SamInjectionModelVisitor<T> visitor) {
        visitor.visit(this);
    }

}
