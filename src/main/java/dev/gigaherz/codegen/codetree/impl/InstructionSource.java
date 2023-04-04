package dev.gigaherz.codegen.codetree.impl;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.function.ToIntFunction;

public abstract class InstructionSource
{
    public abstract boolean compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, Label jumpEnd, boolean needsResult);
}
