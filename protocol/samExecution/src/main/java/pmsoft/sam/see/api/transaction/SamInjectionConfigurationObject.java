package pmsoft.sam.see.api.transaction;

import com.google.common.collect.ImmutableList;
import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.see.api.model.SIID;

class SamInjectionConfigurationObject implements SamInjectionConfiguration {

    private final ServiceKey exposedService;
    private final SIID exposedInstance;
    private final ImmutableList<BindPoint> bindPoints;

    SamInjectionConfigurationObject(ServiceKey exposedService, SIID exposedInstance, ImmutableList<BindPoint> bindPoints) {
        super();
        this.exposedService = exposedService;
        this.exposedInstance = exposedInstance;
        this.bindPoints = bindPoints;
    }

    @Override
    public ServiceKey getProvidedService() {
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
//		visitor.enterNested(this);
//		for (BindPoint bindPoint : bindPoints) {
//			bindPoint.accept(visitor);
//		}
//		visitor.exitNested(this);
    }

}
