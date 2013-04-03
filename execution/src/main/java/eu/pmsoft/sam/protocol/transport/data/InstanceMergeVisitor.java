package eu.pmsoft.sam.protocol.transport.data;


import eu.pmsoft.sam.protocol.transport.CanonicalInstanceReference;

public interface InstanceMergeVisitor {

    public void merge(CanonicalInstanceReference instanceReference);
}
