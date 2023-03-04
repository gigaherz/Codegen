package dev.gigaherz.codegen.codetree.expr.impl;

import dev.gigaherz.codegen.api.FieldToken;
import dev.gigaherz.codegen.codetree.MethodLookup;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.expr.LRef;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import dev.gigaherz.codegen.type.TypeProxy;

import java.util.List;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public abstract class ValueExpressionImpl<T, B> extends ExprBase<B> implements ValueExpression<T, B>
{
    public ValueExpressionImpl(CodeBlockInternal<B, ?, ?> cb)
    {
        super(cb);
    }

    @Override
    public TypeProxy<T> proxyType()
    {
        return TypeProxy.of(effectiveType());
    }

    @Override
    public LRef<?> fieldRef(String fieldName)
    {
        return cb.fieldRef(this, proxyType().classInfo().getField(fieldName));
    }

    @Override
    public ValueExpression<?, B> field(String fieldName)
    {
        return cb.field(this, proxyType().classInfo().getField(fieldName));
    }

    @Override
    public <F> ValueExpression<F, B> field(FieldToken<F> fieldToken)
    {
        return cb.field(this, proxyType().classInfo().getField(fieldToken.name()));
    }

    @Override
    public <R> ValueExpression<R, B> methodCall(String name, List<ValueExpression<?, B>> values)
    {
        return methodCall(name, ml -> {
            for (var val : values)
            {
                ml.withParam(val.effectiveType());
            }
            return ml;
        }, values);
    }

    @Override
    public <R> ValueExpression<R, B> methodCall(String name, Function<MethodLookup<T>, MethodLookup<T>> lookup, List<ValueExpression<?, B>> values)
    {
        var ml = new MethodLookup<>(proxyType().classInfo(), name);
        ml = lookup.apply(ml);
        return cb.methodCall(this, ml.result(), values);
    }
}
