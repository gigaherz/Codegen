package dev.gigaherz.codegen.codetree.impl;

import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.ToIntFunction;

public class ExprBreak extends InstructionSource
{
    private final CodeBlockInternal<?, ?> cb;

    private final ValueExpression<?, ?> value;

    public ExprBreak(CodeBlockInternal<?, ?> cb, ValueExpression<?, ?> value)
    {
        this.cb = cb;
        this.value = value;
    }

    @Override
    public boolean compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, Label jumpEnd, boolean needsResult)
    {
        mv.visitLabel(cb.owner().makeLabel());
        value.compile(defineConstant, mv, needsResult);
        mv.visitJumpInsn(Opcodes.GOTO, jumpEnd);
        return true;
    }
}
