package org.xmlobjects.util.copy;

import org.xmlobjects.model.Child;
import org.xmlobjects.model.ChildList;

import java.util.Map;

public class ChildListCloner extends CollectionCloner<ChildList> {

    ChildListCloner() {
    }

    @Override
    public ChildList copy(ChildList src, ChildList dest, Map<Object, Object> clones, boolean shallowCopy, CopyBuilder builder) throws Exception {
        if (dest == null) {
            Child parent = deepCopy(src.getParent(), builder);
            dest = new ChildList(parent);
        }

        return super.copy(src, dest, clones, shallowCopy, builder);
    }
}
