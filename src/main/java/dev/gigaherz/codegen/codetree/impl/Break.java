package dev.gigaherz.codegen.codetree.impl;

import dev.gigaherz.codegen.codetree.CompileTerminationMode;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.ToIntFunction;

public class Break extends InstructionSource
{
    public Break()
    {
    }

    @Override
    public CompileTerminationMode compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, Label jumpEnd, boolean needsResult)
    {
        mv.visitJumpInsn(Opcodes.GOTO, jumpEnd);
        return CompileTerminationMode.BREAK;
    }
}
