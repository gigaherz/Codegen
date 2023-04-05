package dev.gigaherz.codegen.codetree.impl;

import dev.gigaherz.codegen.codetree.CompileTerminationMode;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.ToIntFunction;

public class Continue extends InstructionSource
{
    private final CodeBlockInternal<?, ?> cb;

    public Continue(CodeBlockInternal<?, ?> cb)
    {
        this.cb = cb;
    }

    @Override
    public CompileTerminationMode compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, Label jumpEnd, boolean needsResult)
    {
        mv.visitLabel(cb.owner().makeLabel());

        var contLabel = cb.continueLabel();
        if (contLabel == null) throw new IllegalStateException("Cannot use `continue` from this scope.");
        mv.visitJumpInsn(Opcodes.GOTO, contLabel);

        return CompileTerminationMode.NORMAL;
    }
}
