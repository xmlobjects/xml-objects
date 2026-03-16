/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.util.copy;

public interface Copyable {
    default Copyable shallowCopy(CopyBuilder builder, CopyContext context) {
        return builder.shallowCopy(this);
    }

    default Copyable deepCopy(CopyBuilder builder, CopyContext context) {
        return builder.deepCopy(this);
    }
}
