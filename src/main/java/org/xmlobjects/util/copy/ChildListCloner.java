package org.xmlobjects.util.copy;

import org.xmlobjects.model.Child;
import org.xmlobjects.model.ChildList;

@SuppressWarnings("rawtypes")
public class ChildListCloner extends CollectionCloner<ChildList> {

    ChildListCloner(CopyBuilder builder) {
        super(builder);
    }

    @Override
    public ChildList newInstance(ChildList object, boolean shallowCopy) {
        Child parent = shallowCopy ? object.getParent() : deepCopy(object.getParent());
        return new ChildList(object.size(), parent);
    }
}
