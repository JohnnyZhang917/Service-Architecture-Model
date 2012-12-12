package pmsoft.sam.see.api.model;

import com.google.inject.Module;

/**
 * @author pawel
 * 
 */
public class SamServiceImplementationKey {

	private final String key;

	public SamServiceImplementationKey(String key) {
		this.key = key;
	}

	public SamServiceImplementationKey(Class<? extends Module> implementationModule) {
		this.key = implementationModule.getName();
	}

	@Override
	public String toString() {
		return "SamServiceImplementationKey [" + key + "]";
	}

	public String getKey() {
		return key;
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SamServiceImplementationKey other = (SamServiceImplementationKey) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (key.compareTo(other.key) != 0)
			return false;
		return true;
	}
}
