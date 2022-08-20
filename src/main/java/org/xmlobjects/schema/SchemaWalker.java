/*
 * xml-objects - A simple and lightweight XML-to-object mapping library
 * https://github.com/xmlobjects
 *
 * Copyright 2019-2022 Claus Nagel <claus.nagel@gmail.com>
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

package org.xmlobjects.schema;

import com.sun.xml.xsom.*;
import com.sun.xml.xsom.visitor.XSVisitor;

import java.util.HashSet;
import java.util.Set;

public abstract class SchemaWalker implements XSVisitor {
    private final Set<Object> visited = new HashSet<>();
    private boolean shouldWalk = true;

    public boolean shouldWalk() {
        return shouldWalk;
    }

    public void setShouldWalk(boolean shouldWalk) {
        this.shouldWalk = shouldWalk;
    }

    public void reset() {
        visited.clear();
        shouldWalk = true;
    }

    public void visit(XSSchema schema) {
        if (shouldWalk && visited.add(schema))
            schema(schema);
    }

    public void visit(XSSchemaSet schemas) {
        for (XSSchema schema : schemas.getSchemas())
            if (shouldWalk && visited.add(schema))
                schema(schema);
    }

    @Override
    public void annotation(XSAnnotation ann) {
    }

    @Override
    public void attGroupDecl(XSAttGroupDecl decl) {
        for (XSAttributeUse use : decl.getAttributeUses())
            if (shouldWalk && visited.add(use))
                attributeUse(use);
    }

    @Override
    public void attributeDecl(XSAttributeDecl decl) {
        if (shouldWalk && visited.add(decl.getType()))
            simpleType(decl.getType());
    }

    @Override
    public void attributeUse(XSAttributeUse use) {
        if (shouldWalk && visited.add(use.getDecl()))
            attributeDecl(use.getDecl());
    }

    @Override
    public void complexType(XSComplexType type) {
        if (shouldWalk && visited.add(type.getContentType())) {
            type.getContentType().visit(this);
            for (XSAttributeUse use : type.getAttributeUses())
                if (shouldWalk && visited.add(use))
                    attributeUse(use);
        }
    }

    @Override
    public void schema(XSSchema schema) {
        for (XSElementDecl decl : schema.getElementDecls().values())
            if (shouldWalk && visited.add(decl))
                elementDecl(decl);

        for (XSAttributeDecl decl : schema.getAttributeDecls().values())
            if (shouldWalk && visited.add(decl))
                attributeDecl(decl);

        for (XSAttGroupDecl decl : schema.getAttGroupDecls().values())
            if (shouldWalk && visited.add(decl))
                attGroupDecl(decl);

        for (XSModelGroupDecl decl : schema.getModelGroupDecls().values())
            if (shouldWalk && visited.add(decl))
                modelGroupDecl(decl);

        for (XSType type : schema.getTypes().values())
            if (shouldWalk && visited.add(type))
                type.visit(this);

        for (XSNotation notation : schema.getNotations().values())
            if (shouldWalk && visited.add(notation))
                notation(notation);
    }

    @Override
    public void facet(XSFacet facet) {
    }

    @Override
    public void notation(XSNotation notation) {
    }

    @Override
    public void identityConstraint(XSIdentityConstraint decl) {
    }

    @Override
    public void xpath(XSXPath xp) {
    }

    @Override
    public void simpleType(XSSimpleType simpleType) {
        if (shouldWalk && visited.add(simpleType.getBaseType()))
            simpleType.getBaseType().visit(this);
    }

    @Override
    public void particle(XSParticle particle) {
        if (shouldWalk && visited.add(particle.getTerm()))
            particle.getTerm().visit(this);
    }

    @Override
    public void empty(XSContentType empty) {
    }

    @Override
    public void wildcard(XSWildcard wc) {
    }

    @Override
    public void modelGroupDecl(XSModelGroupDecl decl) {
        if (shouldWalk && visited.add(decl.getModelGroup()))
            modelGroup(decl.getModelGroup());
    }

    @Override
    public void modelGroup(XSModelGroup group) {
        for (XSParticle particle : group.getChildren())
            if (shouldWalk && visited.add(particle))
                particle(particle);
    }

    @Override
    public void elementDecl(XSElementDecl decl) {
        if (shouldWalk && visited.add(decl.getType()))
            decl.getType().visit(this);
    }
}
