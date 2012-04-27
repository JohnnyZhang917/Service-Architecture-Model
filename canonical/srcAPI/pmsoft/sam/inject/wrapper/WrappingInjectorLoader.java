package pmsoft.sam.inject.wrapper;

import java.util.List;

public interface WrappingInjectorLoader {

	public List<WrappingInjectorLoader> bindService(List<ServiceBindingDefinition> serviceBindings); 

}
