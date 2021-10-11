package dev.gigaherz.codegen.codetree.impl;

import dev.gigaherz.codegen.codetree.expr.MethodCallExpression;

public class SuperCall extends ExecuteExpression
{
    public SuperCall(MethodImplementation<?> mi, MethodCallExpression<?, ?> methodCall)
    {
        super(mi, methodCall);
    }
}
