package dev.gigaherz.codegen.codetree.expr.impl;

import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import dev.gigaherz.codegen.codetree.impl.MethodImplementation;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.MethodVisitor;

import java.util.function.ToIntFunction;

public class BinaryOperator<T, B> extends ValueExpressionImpl<T, B>
{
    private final int opcode;
    private final ValueExpression<?, B> first;
    private final ValueExpression<?, B> second;
    private final TypeProxy<T> resultType;

    public BinaryOperator(CodeBlockInternal<B, ?> cb, int opcode, ValueExpression<?, B> first, ValueExpression<T, B> second, TypeProxy<T> resultType)
    {
        super(cb);
        this.opcode = opcode;
        this.first = first;
        this.second = second;
        this.resultType = resultType;
    }

    @Override
    public TypeProxy<T> effectiveType()
    {
        return resultType;
    }

    @Override
    public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult, TypeProxy<?> returnInsnType)
    {
        cb.beforeExpressionCompile();

        first.compile(defineConstant, mv, needsResult, null);
        second.compile(defineConstant, mv, needsResult, null);

        if (needsResult)
        {
            cb.popStack(second.effectiveType());
            cb.popStack(first.effectiveType());
            mv.visitInsn(opcode);
            cb.pushStack(resultType);
        }

        cb.afterExpressionCompile(needsResult);
    }
}


