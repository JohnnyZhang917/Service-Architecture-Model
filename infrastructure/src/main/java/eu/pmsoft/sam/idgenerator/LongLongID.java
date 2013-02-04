package eu.pmsoft.sam.idgenerator;

import java.io.Serializable;

public class LongLongID implements Serializable {
    private static final long serialVersionUID = -6852037388281809754L;
    private final long mark;
    private final long linear;

    public LongLongID(long mark, long linear) {
        this.mark = mark;
        this.linear = linear;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LongLongID)) return false;

        LongLongID that = (LongLongID) o;

        if (linear != that.linear) return false;
        if (mark != that.mark) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (mark ^ (mark >>> 32));
        result = 31 * result + (int) (linear ^ (linear >>> 32));
        return result;
    }

    public long getMark() {
        return mark;
    }

    public long getLinear() {
        return linear;
    }
}
