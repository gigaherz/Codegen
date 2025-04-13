package dev.gigaherz.codegen.codetree.expr.impl;

import dev.gigaherz.codegen.codetree.expr.BooleanExpression;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nullable;
import java.util.function.ToIntFunction;

public class CastExpression<T, B> extends ValueExpressionImpl<T, B>
{
    protected final ValueExpression<?, B> expression;
    protected final TypeProxy<T> targetClass;

    public CastExpression(CodeBlockInternal<B, ?> cb, ValueExpression<?, B> expression, TypeProxy<T> targetClass)
    {
        super(cb);
        this.expression = expression;
        this.targetClass = targetClass;
    }

    @Override
    public TypeProxy<T> effectiveType()
    {
        return targetClass;
    }

    @Override
    public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult, TypeProxy<?> returnInsnType)
    {
        cb.beforeExpressionCompile();
        if (expression.effectiveType().equals(targetClass))
        {
            expression.compile(defineConstant, mv, needsResult, returnInsnType);
        }
        else if (targetClass.isAssignableFrom(expression.effectiveType()))
        {
            throw new IllegalStateException("TODO - Not implemented");
        }
        else
        {
            throw new IllegalStateException("Cannot cast from type " + expression.effectiveType() + " to type " + targetClass);
        }
        cb.afterExpressionCompile(needsResult);
    }

    public static class Bool<B> extends CastExpression<Boolean, B> implements BooleanExpression<B>
    {
        public Bool(CodeBlockInternal<B, ?> cb, ValueExpression<?, B> expression)
        {
            super(cb, expression, TypeProxy.of(boolean.class));
        }

        @Override
        public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, @Nullable Label jumpTrue, @Nullable Label jumpFalse)
        {
            if (jumpFalse == null && jumpTrue == null)
                throw new IllegalStateException("Comparison compile called with both labels null");

            cb.beforeExpressionCompile();
            if (expression instanceof BooleanExpression bexp)
            {
                //noinspection unchecked
                bexp.compile(defineConstant, mv, jumpTrue, jumpFalse);
            }
            else if (expression.effectiveType().getSafeRawType().equals(boolean.class))
            {
                expression.compile(defineConstant, mv, true, null);

                cb.popStack(1);

                if (jumpTrue == null)
                {
                    mv.visitJumpInsn(Opcodes.IFEQ, jumpFalse);
                }
                else
                {
                    mv.visitJumpInsn(Opcodes.IFNE, jumpTrue);
                    if (jumpFalse != null)
                        mv.visitJumpInsn(Opcodes.GOTO, jumpFalse);
                }
            }
            else
            {
                throw new IllegalStateException("Cannot cast from type " + expression.effectiveType() + " to type " + targetClass);
            }
            cb.afterExpressionCompile(false);
        }
    }
}
