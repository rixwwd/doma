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
package org.seasar.doma.internal.apt.meta;

import static org.seasar.doma.internal.util.AssertionUtil.assertNotNull;

import javax.lang.model.element.ExecutableElement;

import org.seasar.doma.internal.apt.AptException;
import org.seasar.doma.internal.apt.Context;
import org.seasar.doma.internal.apt.cttype.BasicCtType;
import org.seasar.doma.internal.apt.cttype.CtType;
import org.seasar.doma.internal.apt.cttype.EntityCtType;
import org.seasar.doma.internal.apt.cttype.HolderCtType;
import org.seasar.doma.internal.apt.cttype.IterableCtType;
import org.seasar.doma.internal.apt.cttype.MapCtType;
import org.seasar.doma.internal.apt.cttype.OptionalCtType;
import org.seasar.doma.internal.apt.cttype.OptionalDoubleCtType;
import org.seasar.doma.internal.apt.cttype.OptionalIntCtType;
import org.seasar.doma.internal.apt.cttype.OptionalLongCtType;
import org.seasar.doma.internal.apt.cttype.SimpleCtTypeVisitor;
import org.seasar.doma.internal.apt.reflection.FunctionReflection;
import org.seasar.doma.message.Message;

/**
 * @author taedium
 * 
 */
public class AutoFunctionQueryMetaFactory extends
        AutoModuleQueryMetaFactory<AutoFunctionQueryMeta> {

    public AutoFunctionQueryMetaFactory(Context ctx) {
        super(ctx);
    }

    @Override
    public QueryMeta createQueryMeta(ExecutableElement method, DaoMeta daoMeta) {
        assertNotNull(method, daoMeta);
        FunctionReflection functionReflection = ctx.getReflections()
                .newFunctionReflection(method);
        if (functionReflection == null) {
            return null;
        }
        AutoFunctionQueryMeta queryMeta = new AutoFunctionQueryMeta(method,
                daoMeta.getDaoElement());
        queryMeta.setFunctionReflection(functionReflection);
        queryMeta.setQueryKind(QueryKind.AUTO_FUNCTION);
        doTypeParameters(queryMeta, method, daoMeta);
        doReturnType(queryMeta, method, daoMeta);
        doParameters(queryMeta, method, daoMeta);
        doThrowTypes(queryMeta, method, daoMeta);
        return queryMeta;
    }

    @Override
    protected void doReturnType(AutoFunctionQueryMeta queryMeta,
            ExecutableElement method, DaoMeta daoMeta) {
        QueryReturnMeta returnMeta = createReturnMeta(method);
        queryMeta.setReturnMeta(returnMeta);
        ResultParameterMeta resultParameterMeta = createResultParameterMeta(
                queryMeta, returnMeta);
        queryMeta.setResultParameterMeta(resultParameterMeta);
    }

    private ResultParameterMeta createResultParameterMeta(
            final AutoFunctionQueryMeta queryMeta,
            final QueryReturnMeta returnMeta) {
        return returnMeta.getCtType().accept(
                new ReturnCtTypeVisitor(queryMeta, returnMeta), false);
    }

    /**
     * 
     * @author nakamura-to
     * 
     */
    private class ReturnCtTypeVisitor extends
            SimpleCtTypeVisitor<ResultParameterMeta, Boolean, RuntimeException> {

        protected final AutoFunctionQueryMeta queryMeta;

        protected final QueryReturnMeta returnMeta;

        public ReturnCtTypeVisitor(AutoFunctionQueryMeta queryMeta,
                QueryReturnMeta returnMeta) {
            this.queryMeta = queryMeta;
            this.returnMeta = returnMeta;
        }

        @Override
        protected ResultParameterMeta defaultAction(CtType type, Boolean p)
                throws RuntimeException {
            throw new AptException(Message.DOMA4063, returnMeta.getMethodElement(),
                    new Object[] { returnMeta.getType() });
        }

        @Override
        public ResultParameterMeta visitBasicCtType(BasicCtType ctType,
                Boolean optional) throws RuntimeException {
            if (Boolean.TRUE == optional) {
                return new OptionalBasicSingleResultParameterMeta(ctType);
            }
            return new BasicSingleResultParameterMeta(ctType);
        }

        @Override
        public ResultParameterMeta visitHolderCtType(HolderCtType ctType,
                Boolean optional) throws RuntimeException {
            if (Boolean.TRUE == optional) {
                return new OptionalHolderSingleResultParameterMeta(ctType);
            }
            return new HolderSingleResultParameterMeta(ctType);
        }

        @Override
        public ResultParameterMeta visitIterableCtType(IterableCtType ctType,
                Boolean p) throws RuntimeException {
            if (!ctType.isList()) {
                defaultAction(ctType, p);
            }
            return ctType.getElementCtType().accept(
                    new IterableElementCtTypeVisitor(queryMeta, returnMeta),
                    false);
        }

        @Override
        public ResultParameterMeta visitOptionalCtType(OptionalCtType ctType,
                Boolean p) throws RuntimeException {
            return ctType.getElementCtType().accept(this, true);
        }

        @Override
        public ResultParameterMeta visitOptionalIntCtType(
                OptionalIntCtType ctType, Boolean p) throws RuntimeException {
            return new OptionalIntSingleResultParameterMeta();
        }

        @Override
        public ResultParameterMeta visitOptionalLongCtType(
                OptionalLongCtType ctType, Boolean p) throws RuntimeException {
            return new OptionalLongSingleResultParameterMeta();
        }

        @Override
        public ResultParameterMeta visitOptionalDoubleCtType(
                OptionalDoubleCtType ctType, Boolean p) throws RuntimeException {
            return new OptionalDoubleSingleResultParameterMeta();
        }

    }

    /**
     * 
     * @author nakamura-to
     * 
     */
    private class IterableElementCtTypeVisitor extends
            SimpleCtTypeVisitor<ResultParameterMeta, Boolean, RuntimeException> {

        protected final AutoFunctionQueryMeta queryMeta;

        protected final QueryReturnMeta returnMeta;

        public IterableElementCtTypeVisitor(AutoFunctionQueryMeta queryMeta,
                QueryReturnMeta returnMeta) {
            this.queryMeta = queryMeta;
            this.returnMeta = returnMeta;
        }

        @Override
        protected ResultParameterMeta defaultAction(CtType ctType, Boolean p)
                throws RuntimeException {
            throw new AptException(Message.DOMA4065, returnMeta.getMethodElement(),
                    new Object[] { ctType.getTypeName() });
        }

        @Override
        public ResultParameterMeta visitBasicCtType(BasicCtType ctType,
                Boolean optional) throws RuntimeException {
            if (Boolean.TRUE == optional) {
                return new OptionalBasicResultListParameterMeta(ctType);
            }
            return new BasicResultListParameterMeta(ctType);
        }

        @Override
        public ResultParameterMeta visitHolderCtType(HolderCtType ctType,
                Boolean optional) throws RuntimeException {
            if (Boolean.TRUE == optional) {
                return new OptionalHolderResultListParameterMeta(ctType);
            }
            return new HolderResultListParameterMeta(ctType);
        }

        @Override
        public ResultParameterMeta visitEntityCtType(EntityCtType ctType,
                Boolean p) throws RuntimeException {
            if (ctType.isAbstract()) {
                throw new AptException(Message.DOMA4156, returnMeta.getMethodElement(),
                        new Object[] { ctType.getTypeName() });
            }
            return new EntityResultListParameterMeta(ctType,
                    queryMeta.getEnsureResultMapping());
        }

        @Override
        public ResultParameterMeta visitMapCtType(MapCtType ctType, Boolean p)
                throws RuntimeException {
            return new MapResultListParameterMeta(ctType);
        }

        @Override
        public ResultParameterMeta visitOptionalCtType(OptionalCtType ctType,
                Boolean p) throws RuntimeException {
            return ctType.getElementCtType().accept(this, true);
        }

        @Override
        public ResultParameterMeta visitOptionalIntCtType(
                OptionalIntCtType ctType, Boolean p) throws RuntimeException {
            return new OptionalIntResultListParameterMeta();
        }

        @Override
        public ResultParameterMeta visitOptionalLongCtType(
                OptionalLongCtType ctType, Boolean p) throws RuntimeException {
            return new OptionalLongResultListParameterMeta();
        }

        @Override
        public ResultParameterMeta visitOptionalDoubleCtType(
                OptionalDoubleCtType ctType, Boolean p) throws RuntimeException {
            return new OptionalDoubleResultListParameterMeta();
        }

    }
}
