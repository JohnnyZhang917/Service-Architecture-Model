package pmsoft.sam.inject.wrapper.deprecated;

import java.util.List;

public interface WrappingInjectorLoader {

	public List<WrappingInjectorLoader> bindService(List<ServiceBindingDefinition> serviceBindings); 

}
