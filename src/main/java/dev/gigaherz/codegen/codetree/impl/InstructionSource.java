package dev.gigaherz.codegen.codetree.impl;

import dev.gigaherz.codegen.codetree.CompileTerminationMode;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.function.ToIntFunction;

public abstract class InstructionSource
{
    public abstract CompileTerminationMode compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, Label jumpEnd, boolean needsResult);
}
