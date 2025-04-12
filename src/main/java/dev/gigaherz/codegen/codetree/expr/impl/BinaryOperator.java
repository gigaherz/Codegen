package dev.gigaherz.codegen.codetree.expr.impl;

import com.google.common.reflect.TypeToken;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import org.objectweb.asm.MethodVisitor;

import java.util.function.ToIntFunction;

/** @noinspection UnstableApiUsage*/
public class BinaryOperator<T, B> extends ValueExpressionImpl<T, B>
{
    private final int opcode;
    private final ValueExpression<?, B> first;
    private final ValueExpression<?, B> second;
    private final TypeToken<T> resultType;

    public BinaryOperator(CodeBlockInternal<B, ?> cb, int opcode, ValueExpression<?, B> first, ValueExpression<T, B> second, TypeToken<T> resultType)
    {
        super(cb);
        this.opcode = opcode;
        this.first = first;
        this.second = second;
        this.resultType = resultType;
    }

    @Override
    public TypeToken<T> effectiveType()
    {
        return resultType;
    }

    @Override
    public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult, TypeToken<?> returnInsnType)
    {
        cb.beforeExpressionCompile();

        first.compile(defineConstant, mv, needsResult, null);
        second.compile(defineConstant, mv, needsResult, null);

        if (needsResult)
        {
            cb.popStack();
            cb.popStack();
            mv.visitInsn(opcode);
            cb.pushStack(resultType);
        }

        cb.afterExpressionCompile(needsResult);
    }
}


