package org.xmlobjects.util.copy;

import java.io.Serializable;

public interface Copyable extends Serializable {
    default Copyable shallowCopy(CopyBuilder builder) {
        return builder.shallowCopy(this);
    }

    default Copyable deepCopy(CopyBuilder builder) {
        return builder.deepCopy(this);
    }
}
