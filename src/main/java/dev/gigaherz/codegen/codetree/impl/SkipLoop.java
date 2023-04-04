package dev.gigaherz.codegen.codetree.impl;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.ToIntFunction;

public class SkipLoop extends InstructionSource
{
    private final boolean breakLoop;

    public SkipLoop(boolean breakLoop)
    {

        this.breakLoop = breakLoop;
    }

    @Override
    public boolean compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, Label jumpEnd, boolean needsResult)
    {
        mv.visitJumpInsn(Opcodes.GOTO, jumpEnd);
        return breakLoop;
    }
}
