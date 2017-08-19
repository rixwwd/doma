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
package org.seasar.doma.jdbc.entity;

import org.seasar.doma.internal.jdbc.scalar.BasicScalar;
import org.seasar.doma.wrapper.IntegerWrapper;
import org.seasar.doma.wrapper.StringWrapper;

import junit.framework.TestCase;

/**
 * @author nakamura-to
 * 
 */
public class DefaultPropertyDescTest extends TestCase {

    @SuppressWarnings("unused")
    private String hoge;

    @SuppressWarnings("unused")
    private Foo foo;

    @SuppressWarnings("unused")
    private int primitiveInt;

    @SuppressWarnings("unused")
    private Integer integer;

    public void testIsQuoteRequired_true() throws Exception {
        boolean isQuoteRequired = true;
        DefaultPropertyDesc<DefaultPropertyDescTest, String, String> propertyDesc = new DefaultPropertyDesc<>(
                DefaultPropertyDescTest.class, () -> new BasicScalar<>(new StringWrapper(), false),
                "hoge", "hoge", NamingType.NONE, true, true, isQuoteRequired);
        assertEquals("hoge", propertyDesc.getColumnName((n, t) -> t));
        assertEquals("[hoge]", propertyDesc.getColumnName((n, t) -> t, s -> "[" + s + "]"));
    }

    public void testIsQuoteRequired_false() throws Exception {
        boolean isQuoteRequired = false;
        DefaultPropertyDesc<DefaultPropertyDescTest, String, String> propertyDesc = new DefaultPropertyDesc<>(
                DefaultPropertyDescTest.class, () -> new BasicScalar<>(new StringWrapper(), false),
                "hoge", "hoge", NamingType.NONE, true, true, isQuoteRequired);
        assertEquals("hoge", propertyDesc.getColumnName((n, t) -> t));
        assertEquals("hoge", propertyDesc.getColumnName((n, t) -> t, s -> "[" + s + "]"));
    }

    public void testGetColumnName_naming_columnNameDefined() throws Exception {
        DefaultPropertyDesc<DefaultPropertyDescTest, String, String> propertyDesc = new DefaultPropertyDesc<>(
                DefaultPropertyDescTest.class, () -> new BasicScalar<>(new StringWrapper(), false),
                "hoge", "foo", NamingType.UPPER_CASE, true, true, false);
        assertEquals("foo",
                propertyDesc.getColumnName((namingType, text) -> namingType.apply(text)));
    }

    public void testGetColumnName_naiming_columnNotDefined() throws Exception {
        DefaultPropertyDesc<DefaultPropertyDescTest, String, String> propertyDesc = new DefaultPropertyDesc<>(
                DefaultPropertyDescTest.class, () -> new BasicScalar<>(new StringWrapper(), false),
                "hoge", "", NamingType.UPPER_CASE, true, true, false);
        assertEquals("HOGE",
                propertyDesc.getColumnName((namingType, text) -> namingType.apply(text)));
    }

    public void testGetColumnName_naiming_quote_quoteRequired() throws Exception {
        DefaultPropertyDesc<DefaultPropertyDescTest, String, String> propertyDesc = new DefaultPropertyDesc<>(
                DefaultPropertyDescTest.class, () -> new BasicScalar<>(new StringWrapper(), false),
                "hoge", "", NamingType.UPPER_CASE, true, true, true);
        assertEquals("[HOGE]", propertyDesc.getColumnName(
                (namingType, text) -> namingType.apply(text), text -> "[" + text + "]"));
    }

    public void testGetColumnName_naiming_quote_quoteNotRequired() throws Exception {
        DefaultPropertyDesc<DefaultPropertyDescTest, String, String> propertyDesc = new DefaultPropertyDesc<>(
                DefaultPropertyDescTest.class, () -> new BasicScalar<>(new StringWrapper(), false),
                "hoge", "", NamingType.UPPER_CASE, true, true, false);
        assertEquals("HOGE", propertyDesc.getColumnName(
                (namingType, text) -> namingType.apply(text), text -> "[" + text + "]"));
    }

    public void testPrimitivePropertyDefaultValue() throws Exception {
        DefaultPropertyDesc<DefaultPropertyDescTest, Integer, Integer> propertyDesc = new DefaultPropertyDesc<>(
                DefaultPropertyDescTest.class, () -> new BasicScalar<>(new IntegerWrapper(), true),
                "primitiveInt", "primitiveInt", NamingType.NONE, true, true, false);
        Property<DefaultPropertyDescTest, Integer> property = propertyDesc.createProperty();
        assertEquals(0, property.get());
    }

    public void testWrapperPropertyDefaultValue() throws Exception {
        DefaultPropertyDesc<DefaultPropertyDescTest, Integer, Integer> propertyDesc = new DefaultPropertyDesc<>(
                DefaultPropertyDescTest.class, () -> new BasicScalar<>(new IntegerWrapper(), false),
                "integer", "integer", NamingType.NONE, true, true, false);
        Property<DefaultPropertyDescTest, Integer> property = propertyDesc.createProperty();
        assertNull(property.get());
    }

    public static class Foo {
        String hoge;
    }
}