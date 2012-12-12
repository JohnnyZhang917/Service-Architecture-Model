package pmsoft.sam.see.injectionUtils;

import java.util.Comparator;
import java.util.List;

import pmsoft.sam.architecture.model.ServiceKey;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

public class ServiceKeyOrder {
	
	public static final Comparator<ServiceKey> serviceKeyComparator = new Comparator<ServiceKey>() {
		@Override
		public int compare(ServiceKey arg0, ServiceKey arg1) {
			return arg0.getServiceDefinitionSignature().compareTo(arg1.getServiceDefinitionSignature());
		}
	};
	
	private static final Ordering<ServiceKey> order = Ordering.from(serviceKeyComparator);

	public static ImmutableList<ServiceKey> orderAndRequiereUnique(List<ServiceKey> keys) {
		
		List<ServiceKey> orderedKeys = order.sortedCopy(keys);
		if( !order.isStrictlyOrdered(orderedKeys) ) throw new RuntimeException("not unique keys order");
		return ImmutableList.copyOf(orderedKeys);
	}
}
