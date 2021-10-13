package dev.gigaherz.codegen.codetree.impl;

import dev.gigaherz.codegen.codetree.expr.ValueExpression;

public class SuperCall extends ExecuteExpression
{
    public SuperCall(MethodImplementation<?> mi, ValueExpression<?, ?> methodCall)
    {
        super(mi, methodCall);
    }
}
