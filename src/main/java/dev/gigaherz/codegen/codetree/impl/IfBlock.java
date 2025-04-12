package dev.gigaherz.codegen.codetree.impl;

import dev.gigaherz.codegen.codetree.CompileTerminationMode;
import dev.gigaherz.codegen.codetree.expr.BooleanExpression;
import dev.gigaherz.codegen.codetree.expr.CodeBlock;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.Consumer;
import java.util.function.ToIntFunction;

public class IfBlock<T, P, R> extends InstructionSource
{
    private final CodeBlockInternal<P, R> cb;
    private final BooleanExpression<?> condition;
    private final Consumer<CodeBlock<T, R>> trueBranch;
    private final Consumer<CodeBlock<T, R>> falseBranch;

    public IfBlock(CodeBlockInternal<P, R> cb, BooleanExpression<?> condition, Consumer<CodeBlock<T, R>> trueBranch, Consumer<CodeBlock<T, R>> falseBranch)
    {
        this.cb = cb;
        this.condition = condition;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    @Override
    public CompileTerminationMode compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, Label jumpEnd, boolean needsResult)
    {
        mv.visitLabel(cb.owner().makeLabel());

        var cbTrue = cb.<T>childBlock();
        var cbFalse = cb.<T>childBlock();

        var jumpFalse = cb.owner().makeLabel();

        var b = (jumpEnd == null);
        if (b) jumpEnd = cb.owner().makeLabel();

        condition.compile(defineConstant, mv, null, jumpFalse);

        trueBranch.accept(cbTrue);
        var retTrue = cbTrue.compile(defineConstant, mv, null);

        if (!retTrue.isBreak())
            mv.visitJumpInsn(Opcodes.GOTO, jumpEnd);
        mv.visitLabel(jumpFalse);

        falseBranch.accept(cbFalse);
        var retFalse = cbFalse.compile(defineConstant, mv, jumpEnd);

        if (b) mv.visitLabel(jumpEnd);

        return ((retTrue.isBreak() && retFalse.isBreak()) || (cbTrue.isEmpty() && retFalse.isBreak()) || (cbFalse.isEmpty() && retTrue.isBreak()))
                ? CompileTerminationMode.BREAK : CompileTerminationMode.NORMAL;
    }
}
