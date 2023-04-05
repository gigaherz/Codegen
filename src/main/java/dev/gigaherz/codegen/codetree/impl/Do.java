package dev.gigaherz.codegen.codetree.impl;

import dev.gigaherz.codegen.codetree.CompileTerminationMode;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.function.ToIntFunction;

public class Do extends InstructionSource
{
    private final MethodImplementation<?> mi;
    private final ValueExpression<?, ?> expression;

    public Do(MethodImplementation<?> mi, ValueExpression<?, ?> expression)
    {
        this.mi = mi;
        this.expression = expression;
    }

    @Override
    public CompileTerminationMode compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, Label jumpEnd, boolean needsResult)
    {
        mv.visitLabel(mi.makeLabel());

        expression.compile(defineConstant, mv, false);

        return CompileTerminationMode.NORMAL;
    }
}
