package doc.wjug;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class DependencyInjectionPoints implements FeatureSpecification {
    @Inject
    private InjectionPointField field;
    @Inject
    @Named("custom key")
    private InjectionPointField fieldWithAnnotatedKey;
    private final InjectionPointConstructor fromConstructor;

    @Inject
    public DependencyInjectionPoints(InjectionPointConstructor constArgument) {
        super();
        this.fromConstructor = constArgument;
    }
}
