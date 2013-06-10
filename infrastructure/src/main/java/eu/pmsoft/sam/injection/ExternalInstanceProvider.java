package eu.pmsoft.sam.injection;

import com.google.inject.Key;

public interface ExternalInstanceProvider {

    public <T> T getReference(int slotNr, Key<T> key, int instanceReferenceNr);

}
