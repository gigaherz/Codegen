package dev.gigaherz.codegen.codetree.impl;

import dev.gigaherz.codegen.codetree.CompileTerminationMode;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.function.ToIntFunction;

public class Compute extends InstructionSource
{
    private final MethodImplementation<?> mi;
    private final ValueExpression<?, ?> methodCall;

    public Compute(MethodImplementation<?> mi, ValueExpression<?, ?> methodCall)
    {
        this.mi = mi;
        this.methodCall = methodCall;
    }

    @Override
    public CompileTerminationMode compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, Label jumpEnd, boolean needsResult)
    {
        mv.visitLabel(mi.makeLabel());

        methodCall.compile(defineConstant, mv, false, null);

        return CompileTerminationMode.NORMAL;
    }
}
