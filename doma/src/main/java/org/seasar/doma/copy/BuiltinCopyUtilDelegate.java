/*
 * Copyright 2004-2009 the Seasar Foundation and the Others.
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
package org.seasar.doma.copy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.seasar.doma.DomaNullPointerException;
import org.seasar.doma.bean.BeanPropertyWrapper;
import org.seasar.doma.bean.BeanUtil;
import org.seasar.doma.bean.BeanWrapper;
import org.seasar.doma.bean.BeanWrapperFactory;
import org.seasar.doma.converter.ConversionException;
import org.seasar.doma.converter.Converter;
import org.seasar.doma.converter.Converters;
import org.seasar.doma.domain.Domain;
import org.seasar.doma.domain.DomainUtil;
import org.seasar.doma.entity.Entity;
import org.seasar.doma.entity.EntityProperty;
import org.seasar.doma.internal.util.ClassUtil;

/**
 * {@link CopyUtilDelegate} のデフォルトの実装です。
 * <p>
 * 次の型への変換をサポートします。
 * <ul>
 * <li> {@link String}
 * <li> {@link BigDecimal}
 * <li> {@link BigInteger}
 * <li> {@link Boolean}
 * <li> {@link Byte}
 * <li> {@link Short}
 * <li> {@link Integer}
 * <li> {@link Long}
 * <li> {@link Float}
 * <li> {@link Double}
 * <li> {@link java.util.Date}
 * <li> {@link Date}
 * <li> {@link Time}
 * <li> {@link Timestamp}
 * </ul>
 * 
 * コピー先がエンティティのプロパティ、つまり {@link Domain} の場合、 {@link Domain}
 * の値の型が上記のリストに含まれればコピー対象となります。
 * 
 * @author taedium
 * 
 */
public class BuiltinCopyUtilDelegate implements CopyUtilDelegate {

    /** 変換先のクラスをキー、コンバーターを値とするマップ */
    protected final Map<Class<?>, Converter<?>> converterMap = new HashMap<Class<?>, Converter<?>>();

    /**
     * インスタンス化します。
     */
    protected BuiltinCopyUtilDelegate() {
        converterMap.put(String.class, Converters.STRING);
        converterMap.put(BigDecimal.class, Converters.BIG_DECIMAL);
        converterMap.put(BigInteger.class, Converters.BIG_INTEGER);
        converterMap.put(Boolean.class, Converters.BOOLEAN);
        converterMap.put(Byte.class, Converters.BYTE);
        converterMap.put(Short.class, Converters.SHORT);
        converterMap.put(Integer.class, Converters.INTEGER);
        converterMap.put(Long.class, Converters.LONG);
        converterMap.put(Float.class, Converters.FLOAT);
        converterMap.put(Double.class, Converters.DOUBLE);
        converterMap.put(java.util.Date.class, Converters.UTIL_DATE);
        converterMap.put(Date.class, Converters.DATE);
        converterMap.put(Time.class, Converters.TIME);
        converterMap.put(Timestamp.class, Converters.TIMESTAMP);
    }

    @Override
    public void copy(Object src, Object dest, CopyOptions copyOptions)
            throws DomaNullPointerException, CopyException {
        if (src == null) {
            throw new DomaNullPointerException("src");
        }
        if (dest == null) {
            throw new DomaNullPointerException("dest");
        }
        if (copyOptions == null) {
            throw new DomaNullPointerException("copyOptions");
        }
        if (Entity.class.isInstance(src)) {
            Entity<?> srcEntity = Entity.class.cast(src);
            if (Entity.class.isInstance(dest)) {
                copyFromEntityToEntity(srcEntity, Entity.class.cast(dest),
                        copyOptions);
            } else {
                copyFromEntityToBean(srcEntity, wrap(dest, copyOptions),
                        copyOptions);
            }
        } else {
            BeanWrapper srcBean = wrap(src, copyOptions);
            if (Entity.class.isInstance(dest)) {
                copyFromBeanToEntity(srcBean, Entity.class.cast(dest),
                        copyOptions);
            } else {
                copyFromBeanToBean(srcBean, wrap(dest, copyOptions),
                        copyOptions);
            }
        }
    }

    @Override
    public void copy(Object src, Map<String, Object> dest,
            CopyOptions copyOptions) throws DomaNullPointerException,
            CopyException {
        if (src == null) {
            throw new DomaNullPointerException("src");
        }
        if (dest == null) {
            throw new DomaNullPointerException("dest");
        }
        if (copyOptions == null) {
            throw new DomaNullPointerException("copyOptions");
        }
        if (Entity.class.isInstance(src)) {
            copyFromEntityToMap(Entity.class.cast(src), dest, copyOptions);
        } else {
            copyFromBeanToMap(wrap(src, copyOptions), dest, copyOptions);
        }
    }

    @Override
    public void copy(Map<String, Object> src, Object dest,
            CopyOptions copyOptions) throws DomaNullPointerException,
            CopyException {
        if (src == null) {
            throw new DomaNullPointerException("src");
        }
        if (dest == null) {
            throw new DomaNullPointerException("dest");
        }
        if (copyOptions == null) {
            throw new DomaNullPointerException("copyOptions");
        }
        if (Entity.class.isInstance(dest)) {
            copyFromMapToEntity(src, Entity.class.cast(dest), copyOptions);
        } else {
            copyFromMapToBean(src, wrap(dest, copyOptions), copyOptions);
        }
    }

    /**
     * エンティティからエンティティにコピーします。
     * 
     * @param src
     *            コピー元
     * @param dest
     *            コピー先
     * @param copyOptions
     *            オプション
     * @throws CopyException
     *             コピーに失敗した場合
     */
    protected void copyFromEntityToEntity(Entity<?> src, Entity<?> dest,
            CopyOptions copyOptions) throws CopyException {
        Class<?> srcClass = src.getClass();
        for (EntityProperty<?> srcProperty : src.__getEntityProperties()) {
            if (!copyOptions.isTargetProperty(srcProperty.getName())) {
                continue;
            }
            copyToEntityProperty(srcClass, srcProperty.getName(), srcProperty
                    .getDomain().get(), dest, copyOptions);
        }
    }

    /**
     * エンティティから {@link Map} にコピーします。
     * 
     * @param src
     *            コピー元
     * @param dest
     *            コピー先
     * @param copyOptions
     *            オプション
     */
    protected void copyFromEntityToMap(Entity<?> src, Map<String, Object> dest,
            CopyOptions copyOptions) {
        for (EntityProperty<?> srcProperty : src.__getEntityProperties()) {
            if (!copyOptions.isTargetProperty(srcProperty.getName())) {
                continue;
            }
            if (!copyOptions.isTargetValue(srcProperty.getDomain().get())) {
                continue;
            }
            dest.put(srcProperty.getName(), srcProperty.getDomain().get());
        }
    }

    /**
     * エンティティから {@literal JavaBeans} にコピーします。
     * 
     * @param src
     *            コピー元
     * @param dest
     *            コピー先
     * @param copyOptions
     *            オプション
     * @throws CopyException
     *             コピーに失敗した場合
     */
    protected void copyFromEntityToBean(Entity<?> src, BeanWrapper dest,
            CopyOptions copyOptions) throws CopyException {
        Class<?> srcClass = src.getClass();
        for (EntityProperty<?> srcProperty : src.__getEntityProperties()) {
            if (!copyOptions.isTargetProperty(srcProperty.getName())) {
                continue;
            }
            if (!copyOptions.isTargetValue(srcProperty.getDomain().get())) {
                continue;
            }
            copyToBeanProperty(srcClass, srcProperty.getName(), srcProperty
                    .getDomain().get(), dest, copyOptions);
        }
    }

    /**
     * {@literal JavaBeans} から エンティティにコピーします。
     * 
     * @param src
     *            コピー元
     * @param dest
     *            コピー先
     * @param copyOptions
     *            オプション
     * @throws CopyException
     *             コピーに失敗した場合
     */
    protected void copyFromBeanToEntity(BeanWrapper src, Entity<?> dest,
            CopyOptions copyOptions) throws CopyException {
        Class<?> srcClass = src.getBeanClass();
        for (BeanPropertyWrapper srcProperty : src.getBeanPropertyWrappers()) {
            if (!copyOptions.isTargetProperty(srcProperty.getName())) {
                continue;
            }
            if (!copyOptions.isTargetValue(srcProperty.getValue())) {
                continue;
            }
            if (!srcProperty.isValueGettable()) {
                continue;
            }
            copyToEntityProperty(srcClass, srcProperty.getName(), srcProperty
                    .getValue(), dest, copyOptions);
        }
    }

    /**
     * {@literal JavaBeans} から {@link Map} にコピーします。
     * 
     * @param src
     *            コピー元
     * @param dest
     *            コピー先
     * @param copyOptions
     *            オプション
     */
    protected void copyFromBeanToMap(BeanWrapper src, Map<String, Object> dest,
            CopyOptions copyOptions) {
        for (BeanPropertyWrapper srcProperty : src.getBeanPropertyWrappers()) {
            if (!copyOptions.isTargetProperty(srcProperty.getName())) {
                continue;
            }
            if (!copyOptions.isTargetValue(srcProperty.getValue())) {
                continue;
            }
            if (!srcProperty.isValueGettable()) {
                continue;
            }
            dest.put(srcProperty.getName(), srcProperty.getValue());
        }
    }

    /**
     * {@literal JavaBeans} から {@literal JavaBeans} にコピーします。
     * 
     * @param src
     *            コピー元
     * @param dest
     *            コピー先
     * @param copyOptions
     *            オプション
     * @throws CopyException
     *             コピーに失敗した場合
     */
    protected void copyFromBeanToBean(BeanWrapper src, BeanWrapper dest,
            CopyOptions copyOptions) throws CopyException {
        Class<?> srcClass = src.getBeanClass();
        for (BeanPropertyWrapper srcProperty : src.getBeanPropertyWrappers()) {
            if (!copyOptions.isTargetProperty(srcProperty.getName())) {
                continue;
            }
            if (!copyOptions.isTargetValue(srcProperty.getValue())) {
                continue;
            }
            if (!srcProperty.isValueGettable()) {
                continue;
            }
            copyToBeanProperty(srcClass, srcProperty.getName(), srcProperty
                    .getValue(), dest, copyOptions);
        }
    }

    /**
     * {@link Map} からエンティティにコピーします。
     * 
     * @param src
     *            コピー元
     * @param dest
     *            コピー先
     * @param copyOptions
     *            オプション
     * @throws CopyException
     *             コピーに失敗した場合
     */
    protected void copyFromMapToEntity(Map<String, Object> src, Entity<?> dest,
            CopyOptions copyOptions) throws CopyException {
        Class<?> srcClass = src.getClass();
        for (Entry<String, Object> srcEntry : src.entrySet()) {
            if (!copyOptions.isTargetProperty(srcEntry.getKey())) {
                continue;
            }
            if (!copyOptions.isTargetValue(srcEntry.getValue())) {
                continue;
            }
            copyToEntityProperty(srcClass, srcEntry.getKey(), srcEntry
                    .getValue(), dest, copyOptions);
        }
    }

    /**
     * {@link Map} から {@literal JavaBeans} にコピーします。
     * 
     * @param src
     *            コピー元
     * @param dest
     *            コピー先
     * @param copyOptions
     *            オプション
     * @throws CopyException
     *             コピーに失敗した場合
     */
    protected void copyFromMapToBean(Map<String, Object> src, BeanWrapper dest,
            CopyOptions copyOptions) throws CopyException {
        Class<?> srcClass = src.getClass();
        for (Entry<String, Object> srcEntry : src.entrySet()) {
            if (!copyOptions.isTargetProperty(srcEntry.getKey())) {
                continue;
            }
            if (!copyOptions.isTargetValue(srcEntry.getValue())) {
                continue;
            }
            copyToBeanProperty(srcClass, srcEntry.getKey(),
                    srcEntry.getValue(), dest, copyOptions);
        }
    }

    /**
     * エンティティのプロパティにコピーします。
     * 
     * @param srcClass
     *            コピー元のクラス
     * @param srcPropertyName
     *            コピー元のプロパティ名
     * @param srcPropertyValue
     *            コピー元の値
     * @param dest
     *            コピー先
     * @param copyOptions
     *            オプション
     * @throws CopyException
     *             コピーに失敗した場合
     */
    protected void copyToEntityProperty(Class<?> srcClass,
            String srcPropertyName, Object srcPropertyValue, Entity<?> dest,
            CopyOptions copyOptions) throws CopyException {
        EntityProperty<?> destProperty = dest
                .__getEntityProperty(srcPropertyName);
        if (destProperty == null) {
            return;
        }
        Domain<?, ?> destDomain = destProperty.getDomain();
        Converter<?> converter = findConverter(destProperty.getName(),
                destDomain.getValueClass(), copyOptions);
        if (converter == null) {
            return;
        }
        Object destValue = convert(converter, srcClass, srcPropertyName,
                srcPropertyValue, copyOptions);
        DomainUtil.set(destDomain, destValue);
    }

    /**
     * {@literal JavaBeans}のプロパティにコピーします。
     * 
     * @param srcClass
     *            コピー元のクラス
     * @param srcPropertyName
     *            コピー元のプロパティ名
     * @param srcPropertyValue
     *            コピー元の値
     * @param dest
     *            コピー先
     * @param copyOptions
     *            オプション
     * @throws CopyException
     *             コピーに失敗した場合
     */
    protected void copyToBeanProperty(Class<?> srcClass,
            String srcPropertyName, Object srcPropertyValue, BeanWrapper dest,
            CopyOptions copyOptions) throws CopyException {
        BeanPropertyWrapper destProperty = dest
                .getBeanPropertyWrapper(srcPropertyName);
        if (destProperty == null) {
            return;
        }
        if (!destProperty.isValueSettable()) {
            return;
        }
        Converter<?> converter = findConverter(destProperty.getName(),
                destProperty.getPropertyClass(), copyOptions);
        if (converter == null) {
            return;
        }
        Object destValue = convert(converter, srcClass, srcPropertyName,
                srcPropertyValue, copyOptions);
        destProperty.setValue(destValue);
    }

    /**
     * 値を変換します。
     * 
     * @param converter
     *            変換に使用するコンバーター
     * @param srcClass
     *            コピー元のクラス
     * @param srcPropertyName
     *            コピー元のプロパティ名
     * @param srcPropertyValue
     *            コピー元の値
     * @param copyOptions
     *            オプション
     * @return 変換された値
     * @throws CopyException
     *             変換に失敗した場合
     */
    protected Object convert(Converter<?> converter, Class<?> srcClass,
            String srcPropertyName, Object srcPropertyValue,
            CopyOptions copyOptions) throws CopyException {
        String pattern = copyOptions.getPattern(srcPropertyName);
        try {
            return converter.convert(srcPropertyValue, pattern);
        } catch (ConversionException e) {
            throw new PropertyCopyException(srcClass.getName(),
                    srcPropertyName, srcPropertyValue, e);
        }
    }

    /**
     * コンバーターを探します。
     * 
     * @param propertyName
     *            プロパティ名
     * @param destPropertyClass
     *            コピー先のプロパティのクラス
     * @param copyOptions
     *            オプション
     * @return コンバーター、存在しない場合 {@code null}
     */
    protected Converter<?> findConverter(String propertyName,
            Class<?> destPropertyClass, CopyOptions copyOptions)
            throws CopyException {
        Converter<?> converter = copyOptions.getConverter(propertyName);
        if (converter == null) {
            Class<?> wrapperClass = ClassUtil
                    .getWrapperClassIfPrimitive(destPropertyClass);
            converter = converterMap.get(wrapperClass);
        }
        return converter;
    }

    /**
     * {@literal JavaBeans} のラッパーを返します。
     * 
     * @param bean
     *            {@literal JavaBeans}
     * @param copyOptions
     *            オプション
     * @return {@literal JavaBeans} のラッパー
     */
    protected BeanWrapper wrap(Object bean, CopyOptions copyOptions) {
        BeanWrapperFactory beanWrapperFactory = copyOptions.getBeanFactory();
        if (beanWrapperFactory != null) {
            return beanWrapperFactory.create(bean);
        }
        return BeanUtil.wrap(bean);
    }

}