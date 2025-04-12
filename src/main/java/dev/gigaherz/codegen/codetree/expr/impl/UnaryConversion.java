package dev.gigaherz.codegen.codetree.expr.impl;

import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.MethodVisitor;

import java.util.function.ToIntFunction;

@SuppressWarnings("UnstableApiUsage")
public class UnaryConversion<R, T, B> extends ValueExpressionImpl<R, B>
{
    private final TypeProxy<R> targetType;
    private final int opcode;
    private final ValueExpression<T, B> value;

    public UnaryConversion(CodeBlockInternal<B, ?> cb, TypeProxy<R> targetType, int opcode, ValueExpression<T, B> value)
    {
        super(cb);
        this.targetType = targetType;
        this.opcode = opcode;
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
        if (needsResult)
        {
            value.compile(defineConstant, mv, true, null);
            mv.visitInsn(opcode);
        }
        cb.afterExpressionCompile(needsResult);
    }
}
