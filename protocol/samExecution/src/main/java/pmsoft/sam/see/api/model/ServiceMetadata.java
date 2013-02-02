package pmsoft.sam.see.api.model;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class ServiceMetadata {

	private final ImmutableMap<String, String> metadata;

    //TODO use metadata or delete
	public ServiceMetadata(Map<String, String> metadata) {
		this.metadata = ImmutableMap.copyOf(metadata);
	}
	
	public ServiceMetadata(){
		this.metadata = ImmutableMap.of();
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public boolean match(ServiceMetadata requiered) {
		for (String reqKey : requiered.metadata.keySet()) {
			if( !this.metadata.containsKey(reqKey)) return false;
			if( this.metadata.get(reqKey).compareTo(requiered.metadata.get(reqKey)) != 0) return false;
		}
		return true;
	}

	public boolean isEmpty() {
		return metadata.isEmpty();
	}
	
	
}
