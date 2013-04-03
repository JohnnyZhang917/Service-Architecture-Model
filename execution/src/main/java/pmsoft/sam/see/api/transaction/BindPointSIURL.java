package pmsoft.sam.see.api.transaction;

import pmsoft.sam.architecture.model.ServiceKey;
import pmsoft.sam.see.api.model.SIURL;

public class BindPointSIURL extends BindPoint {

    private final SIURL siurl;

    public BindPointSIURL(ServiceKey contract, SIURL siurl) {
        super(contract);
        this.siurl = siurl;
    }

    @Override
    public <T> void accept(SamInjectionModelVisitor<T> visitor) {
        visitor.visit(this);
    }

    public SIURL getSiurl() {
        return siurl;
    }

}
