package dev.gigaherz.codegen.codetree.expr.impl;

import com.google.common.reflect.TypeToken;
import dev.gigaherz.codegen.codetree.expr.BooleanExpression;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nullable;

/** @noinspection UnstableApiUsage*/
public class CastExpression<T, B> extends ValueExpressionImpl<T, B>
{
    protected final ValueExpression<?, B> expression;
    protected final TypeToken<T> targetClass;

    public CastExpression(CodeBlockInternal<B, ?> cb, ValueExpression<?, B> expression, TypeToken<T> targetClass)
    {
        super(cb);
        this.expression = expression;
        this.targetClass = targetClass;
    }

    @Override
    public TypeToken<T> effectiveType()
    {
        return targetClass;
    }

    @Override
    public void compile(MethodVisitor mv, boolean needsResult)
    {
        cb.beforeExpressionCompile();
        if (expression.effectiveType().equals(targetClass))
        {
            expression.compile(mv,needsResult);
        }
        else if (targetClass.getRawType().isAssignableFrom(expression.effectiveType().getRawType()))
        {
            throw new IllegalStateException("Not implemented");
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
            super(cb, expression, TypeToken.of(boolean.class));
        }

        @Override
        public void compile(MethodVisitor mv, @Nullable Label jumpTrue, @Nullable Label jumpFalse)
        {
            if (jumpFalse == null && jumpTrue == null)
                throw new IllegalStateException("Comparison compile called with both labels null");

            cb.beforeExpressionCompile();
            if (expression instanceof BooleanExpression bexp)
            {
                bexp.compile(mv, jumpTrue, jumpFalse);
            }
            else if (expression.effectiveType().getRawType().equals(boolean.class))
            {
                //throw new IllegalStateException("Not implemented");

                expression.compile(mv, true);

                cb.popStack();

                if (jumpTrue == null)
                {
                    mv.visitJumpInsn(Opcodes.IFEQ, jumpFalse);
                }
                else
                {
                    mv.visitJumpInsn(Opcodes.IFNE, jumpTrue);
                    if (jumpFalse != null) mv.visitJumpInsn(Opcodes.GOTO, jumpFalse);
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