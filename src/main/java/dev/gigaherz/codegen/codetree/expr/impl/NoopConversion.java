package dev.gigaherz.codegen.codetree.expr.impl;

import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.MethodVisitor;

import java.util.function.ToIntFunction;

public class NoopConversion<R, B> extends ValueExpressionImpl<R, B>
{
    private final TypeProxy<R> targetType;
    private final ValueExpression<?, B> value;

    public NoopConversion(CodeBlockInternal<B, ?> cb, TypeProxy<R> targetType, ValueExpression<?, B> value)
    {
        super(cb);
        this.targetType = targetType;
        this.value = value;
    }

    @Override
    public TypeProxy<R> effectiveType()
    {
        return targetType;
    }

    @Override
    public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult, TypeProxy<?> returnInsnType)
    {
        cb.beforeExpressionCompile();
        value.compile(defineConstant, mv, needsResult, returnInsnType);
        cb.afterExpressionCompile(needsResult);
    }
}

