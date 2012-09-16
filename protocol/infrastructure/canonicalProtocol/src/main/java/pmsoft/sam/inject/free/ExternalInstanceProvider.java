package pmsoft.sam.inject.free;

import com.google.inject.Key;

public interface ExternalInstanceProvider {

	public <T> T getReference(Key<T> key, long instanceReferenceNr, int serviceSlotNr);
	
}
