/*
 * Copyright (c) 2015. Paweł Cesar Sanjuan Szklarz.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    @Override
    public String toString() {
        return "LLID" + mark + "=" + linear;
    }
}
