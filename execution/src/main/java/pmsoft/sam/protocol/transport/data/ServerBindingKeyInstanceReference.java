package pmsoft.sam.protocol.transport.data;

import com.google.inject.Key;

public class ServerBindingKeyInstanceReference<T> extends BindingKeyInstanceReference<T> {

    /**
     *
     */
    private static final long serialVersionUID = 1641424347343666625L;

    public ServerBindingKeyInstanceReference(int instanceNr, Key<T> key) {
        super(instanceNr, key);
    }

    @Override
    public void visitToMergeOnInstanceRegistry(InstanceMergeVisitor api) {
        api.visitServerBindingKeyInstanceReference(this);
    }

    @Override
    public String toString() {
        return "#" + instanceNr + "->ServerBindingKeyInstanceReference [key=" + key + ", instanceNr=" + instanceNr + "]";
    }


}
