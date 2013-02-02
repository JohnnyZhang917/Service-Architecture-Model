package pmsoft.sam.protocol.transport.data;

import com.google.inject.Key;

public class BindingKeyInstanceReference<T> extends AbstractInstanceReference {

    /**
     *
     */
    private static final long serialVersionUID = 7482158120617411694L;
    protected final SerializableKey<T> key;

    public BindingKeyInstanceReference(int instanceNr, Key<T> key) {
        super(instanceNr);
        this.key = SerializableKey.fromKey(key);
    }

    @Override
    public void visitToMergeOnInstanceRegistry(InstanceMergeVisitor api) {
        api.visitBindingKey(this);
    }

    @SuppressWarnings("unchecked")
    public Key<T> getKey() {
        return (Key<T>) key.getKey();
    }

    @Override
    public String toString() {
        return "#" + instanceNr + "->BindingKeyInstanceReference [key=" + key + ", instanceNr=" + instanceNr + "]";
    }
}
