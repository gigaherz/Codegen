package dev.gigaherz.codegen.codetree.expr.impl;

import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.MethodVisitor;

import java.util.function.ToIntFunction;

@SuppressWarnings("UnstableApiUsage")
public class UnaryOperator<R, B> extends ValueExpressionImpl<R, B>
{
    private final int opcode;
    private final ValueExpression<R, B> value;

    public UnaryOperator(CodeBlockInternal<B, ?> cb, int opcode, ValueExpression<R, B> value)
    {
        super(cb);
        this.opcode = opcode;
        this.value = value;
    }

    @Override
    public TypeProxy<R> effectiveType()
    {
        return value.effectiveType();
    }

    @Override
    public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult, TypeProxy<?> returnInsnType)
    {
        cb.beforeExpressionCompile();
        value.compile(defineConstant, mv, needsResult, null);
        if (needsResult)
        {
            mv.visitInsn(opcode);
        }
        cb.afterExpressionCompile(needsResult);
    }
}
