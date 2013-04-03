package eu.pmsoft.sam.see.api.model;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

public class SIURL implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -827101818297868614L;
    private final URL serviceInstanceReference;

    public static SIURL fromUrl(URL serviceLocation) {
        return new SIURL(serviceLocation);
    }

    public static SIURL fromUrlString(String serviceInstanceReference) throws MalformedURLException {
        return new SIURL(new URL(serviceInstanceReference));
    }

    private SIURL(URL serviceLocation) {
        this.serviceInstanceReference = serviceLocation;
    }

    public URL getServiceInstanceReference() {
        return serviceInstanceReference;
    }

    @Override
    public String toString() {
        return "SIURL [" + serviceInstanceReference + "]";
    }

    @Override
    public int hashCode() {
        return serviceInstanceReference.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SIURL other = (SIURL) obj;
        if (serviceInstanceReference == null) {
            if (other.serviceInstanceReference != null)
                return false;
        } else if (!serviceInstanceReference.equals(other.serviceInstanceReference))
            return false;
        return true;
    }

}
