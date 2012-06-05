package doc.wjug;
import com.google.inject.AbstractModule;
public class ModuleExample extends AbstractModule {
	@Override
	protected void configure() {
		bind(FeatureSpecification.class).to(DependencyInjectionPoints.class);
		bind(InjectionPointField.class).to(InjectionPointClass.class);
	}
}
