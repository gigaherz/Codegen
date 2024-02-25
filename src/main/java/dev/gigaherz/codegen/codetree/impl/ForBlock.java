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

public class ForBlock<T, B, M> extends InstructionSource
{
    private final CodeBlockInternal<B, M> cb;
    private final Consumer<CodeBlock<T, M>> init;
    private final BooleanExpression<?> condition;
    private final Consumer<CodeBlock<T, M>> step;
    private final Consumer<CodeBlock<T, M>> body;


    public ForBlock(CodeBlockInternal<B, M> cb, Consumer<CodeBlock<T, M>> init, BooleanExpression<?> condition, Consumer<CodeBlock<T, M>> step, Consumer<CodeBlock<T, M>> body)
    {
        this.cb = cb;
        this.init = init;
        this.condition = condition;
        this.step = step;
        this.body = body;
    }

    @Override
    public CompileTerminationMode compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, Label jumpEnd, boolean needsResult)
    {
        mv.visitLabel(cb.owner().makeLabel());

        var startLabel = cb.owner().makeLabel();
        var continueLabel = cb.owner().makeLabel();

        var cbInit = this.cb.<T>childBlock(jumpEnd, continueLabel); // FIXME: variables declared in init must be seen by step and body
        var cbStep = this.cb.<T>childBlock(jumpEnd, continueLabel);
        var cbBody = this.cb.<T>childBlock(jumpEnd, continueLabel);

        init.accept(cbInit);

        var brInit = cbInit.compile(defineConstant, mv, jumpEnd);

        mv.visitLabel(startLabel);

        condition.compile(defineConstant, mv, null, jumpEnd);

        body.accept(cbBody);

        var brBody = cbBody.compile(defineConstant, mv, jumpEnd);

        mv.visitLabel(continueLabel);

        step.accept(cbStep);

        var brStep = cbStep.compile(defineConstant, mv, jumpEnd);

        mv.visitJumpInsn(Opcodes.GOTO, startLabel);

        mv.visitLabel(jumpEnd);

        return (brInit.isBreak() || brBody.isBreak() || brStep.isBreak() || (cbInit.isEmpty() && cbStep.isEmpty() && cbBody.isEmpty())) ? CompileTerminationMode.BREAK : CompileTerminationMode.NORMAL;
    }
}
