/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.doma.internal.apt.cttype;

import static org.seasar.doma.internal.util.AssertionUtil.assertNotNull;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import org.seasar.doma.internal.apt.Context;

/**
 * @author taedium
 * 
 */
public abstract class AbstractCtType implements CtType {

    protected final TypeMirror typeMirror;

    protected final String typeName;

    protected final TypeElement typeElement;

    protected final String qualifiedName;

    protected final String metaTypeName;

    AbstractCtType(Context ctx, TypeMirror typeMirror) {
        assertNotNull(ctx, typeMirror);
        this.typeMirror = typeMirror;
        this.typeName = ctx.getTypes().getTypeName(typeMirror);
        this.typeElement = ctx.getTypes().toTypeElement(typeMirror);
        if (typeElement != null) {
            qualifiedName = typeElement.getQualifiedName().toString();
        } else {
            qualifiedName = typeName;
        }
        this.metaTypeName = getMetaTypeName(ctx, typeMirror);
    }

    private static String getMetaTypeName(Context ctx, TypeMirror typeMirror) {
        assertNotNull(ctx, typeMirror);
        String typeName = ctx.getTypes().getTypeName(typeMirror);
        TypeElement typeElement = ctx.getTypes().toTypeElement(typeMirror);
        if (typeElement == null) {
            return typeName;
        }
        return ctx.getMetas().toFullMetaName(typeElement)
                + makeTypeParamDecl(typeName);
    }

    private static String makeTypeParamDecl(String typeName) {
        int pos = typeName.indexOf("<");
        if (pos == -1) {
            return "";
        }
        return typeName.substring(pos);
    }

    @Override
    public TypeMirror getTypeMirror() {
        return typeMirror;
    }

    @Override
    public TypeElement getTypeElement() {
        return typeElement;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public String getQualifiedName() {
        return qualifiedName;
    }

}
