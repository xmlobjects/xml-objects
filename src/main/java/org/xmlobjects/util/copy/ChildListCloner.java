/*
 * xml-objects - A simple and lightweight XML-to-object mapping library
 * https://github.com/xmlobjects
 *
 * Copyright 2019-2024 Claus Nagel <claus.nagel@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
