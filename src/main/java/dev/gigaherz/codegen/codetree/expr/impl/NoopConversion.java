package dev.gigaherz.codegen.codetree.expr.impl;

import com.google.common.reflect.TypeToken;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import org.objectweb.asm.MethodVisitor;

import java.util.function.ToIntFunction;

@SuppressWarnings("UnstableApiUsage")
public class NoopConversion<R, B> extends ValueExpressionImpl<R, B>
{
    private final TypeToken<R> targetType;
    private final ValueExpression<?, B> value;

    public NoopConversion(CodeBlockInternal<B, ?> cb, TypeToken<R> targetType, ValueExpression<?, B> value)
    {
        super(cb);
        this.targetType = targetType;
        this.value = value;
    }

    @Override
    public TypeToken<R> effectiveType()
    {
        return targetType;
    }

    @Override
    public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult, TypeToken<?> returnInsnType)
    {
        cb.beforeExpressionCompile();
        value.compile(defineConstant, mv, needsResult, returnInsnType);
        cb.afterExpressionCompile(needsResult);
    }
}

