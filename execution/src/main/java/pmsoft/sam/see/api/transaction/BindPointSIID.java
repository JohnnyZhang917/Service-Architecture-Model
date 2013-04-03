package pmsoft.sam.see.api.transaction;

import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.see.api.model.SIID;

public class BindPointSIID extends BindPoint {

    private final SIID siid;

    public BindPointSIID(ServiceKey contract, SIID siid) {
        super(contract);
        this.siid = siid;
    }

    public SIID getSiid() {
        return siid;
    }

    @Override
    public <T> void accept(SamInjectionModelVisitor<T> visitor) {
        visitor.visit(this);
    }

}
