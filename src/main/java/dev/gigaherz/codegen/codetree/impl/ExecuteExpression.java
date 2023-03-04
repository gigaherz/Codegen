package dev.gigaherz.codegen.codetree.impl;

import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class ExecuteExpression extends InstructionSource
{
    private final MethodImplementation<?> mi;
    private final ValueExpression<?, ?> methodCall;

    public ExecuteExpression(MethodImplementation<?> mi, ValueExpression<?, ?> methodCall)
    {
        this.mi = mi;
        this.methodCall = methodCall;
    }

    @Override
    public boolean compile(MethodVisitor mv, Label jumpEnd)
    {
        mv.visitLabel(mi.makeLabel());
        methodCall.compile(mv, false);

        return false;
    }
}
