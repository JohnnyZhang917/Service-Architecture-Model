package eu.pmsoft.see.api.model;

import com.google.common.base.Objects;

import java.util.UUID;

public class STID {

    private final UUID serviceInstanceUniqueID;

    public STID(UUID serviceInstanceUniqueID) {
        assert serviceInstanceUniqueID != null;
        this.serviceInstanceUniqueID = serviceInstanceUniqueID;
    }

    public static STID createNewUnique(){
        return new STID(UUID.randomUUID());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        STID stid = (STID) o;

        if (serviceInstanceUniqueID != null ? !serviceInstanceUniqueID.equals(stid.serviceInstanceUniqueID) : stid.serviceInstanceUniqueID != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return serviceInstanceUniqueID != null ? serviceInstanceUniqueID.hashCode() : 0;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("serviceInstanceUniqueID", serviceInstanceUniqueID)
                .toString();
    }
}
