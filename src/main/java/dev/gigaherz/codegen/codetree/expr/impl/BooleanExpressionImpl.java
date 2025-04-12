package dev.gigaherz.codegen.codetree.expr.impl;

import dev.gigaherz.codegen.codetree.expr.BooleanExpression;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.type.TypeProxy;

@SuppressWarnings("UnstableApiUsage")
public abstract class BooleanExpressionImpl<B> extends ValueExpressionImpl<Boolean, B> implements BooleanExpression<B>
{
    public static final TypeProxy<Boolean> BOOLEAN_TYPE_TOKEN = TypeProxy.of(boolean.class);

    public BooleanExpressionImpl(CodeBlockInternal<B, ?> cb)
    {
        super(cb);
    }

    @Override
    public TypeProxy<Boolean> effectiveType()
    {
        return BOOLEAN_TYPE_TOKEN;
    }
}
