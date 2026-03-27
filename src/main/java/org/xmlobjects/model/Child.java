/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.model;

import org.xmlobjects.copy.CopyCallback;
import org.xmlobjects.copy.CopyContext;
import org.xmlobjects.copy.CopyMode;

public interface Child extends CopyCallback {
    Child getParent();

    void setParent(Child parent);

    default <T extends Child> T getParent(Class<T> type) {
        Child parent = this;
        while ((parent = parent.getParent()) != null) {
            if (type.isInstance(parent)) {
                return type.cast(parent);
            }
        }

        return null;
    }

    @Override
    default void preCopy(CopyContext context, CopyMode mode, boolean isRoot) {
        if (isRoot) {
            context.exclude(getParent());
        }
    }

    @Override
    default void postCopy(CopyContext context, CopyMode mode, boolean isRoot) {
        if (isRoot) {
            context.include(getParent());
            setParent(null);
        }
    }
}
