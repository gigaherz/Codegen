package dev.gigaherz.codegen.codetree.expr.impl;

import dev.gigaherz.codegen.api.FieldToken;
import dev.gigaherz.codegen.codetree.MethodLookup;
import dev.gigaherz.codegen.codetree.expr.BooleanExpression;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.expr.LRef;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import dev.gigaherz.codegen.type.TypeProxy;

import java.util.List;
import java.util.function.Function;

public abstract class ValueExpressionImpl<T, B> extends ExprBase<B> implements ValueExpression<T, B>
{
    public ValueExpressionImpl(CodeBlockInternal<B, ?> cb)
    {
        super(cb);
    }

    @Override
    public <T1> ValueExpression<T1, B> cast(TypeProxy<T1> targetClass)
    {
        return new CastExpression<>(cb, this, targetClass);
    }

    @Override
    public BooleanExpression<B> castToBool()
    {
        return new CastExpression.Bool<>(cb, this);
    }

    @Override
    public LRef<?> fieldRef(String fieldName)
    {
        return cb.fieldRef(this, effectiveType().classInfo().getField(fieldName));
    }

    @Override
    public <F> ValueExpression<F, B> field(String fieldName)
    {
        return cb.field(this, effectiveType().classInfo().getField(fieldName));
    }

    @Override
    public <F> ValueExpression<F, B> field(FieldToken<F> fieldToken)
    {
        return cb.field(this, effectiveType().classInfo().getField(fieldToken.name()));
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
        var ml = new MethodLookup<>(effectiveType().classInfo(), name);
        ml = lookup.apply(ml);
        return cb.methodCall(this, ml.result(), values);
    }
}
