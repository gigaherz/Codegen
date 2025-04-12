package dev.gigaherz.codegen.codetree.expr.impl;

import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nullable;
import java.util.function.ToIntFunction;

public class NotExpression<B> extends BooleanExpressionImpl<B>
{
    private final ValueExpression<?, B> first;

    public NotExpression(CodeBlockInternal<B, ?> cb, ValueExpression<?, B> first)
    {
        super(cb);
        this.first = first;
    }

    @Override
    public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult, TypeProxy<?> returnInsnType)
    {
        cb.beforeExpressionCompile();
        first.compile(defineConstant, mv, needsResult, null);
        if (needsResult)
        {
            var jumpFalse = new Label();
            var jumpEnd = new Label();
            mv.visitJumpInsn(Opcodes.IFNE, jumpFalse);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitJumpInsn(Opcodes.GOTO, jumpEnd);
            mv.visitLabel(jumpFalse);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitLabel(jumpEnd);
            cb.afterExpressionCompile(needsResult);
        }
    }

    @Override
    public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, @Nullable Label jumpTrue, @Nullable Label jumpFalse)
    {
        if (jumpFalse == null && jumpTrue == null)
            throw new IllegalStateException("Comparison compile called with both labels null");

        if (first instanceof BooleanExpressionImpl x)
        {
            x.compile(defineConstant, mv, jumpFalse, jumpTrue);
        }
        else
        {
            first.compile(defineConstant, mv, true, null);
            mv.visitJumpInsn(Opcodes.IFEQ, jumpTrue);
            cb.popStack();
            mv.visitJumpInsn(Opcodes.GOTO, jumpFalse);
        }
    }
}
