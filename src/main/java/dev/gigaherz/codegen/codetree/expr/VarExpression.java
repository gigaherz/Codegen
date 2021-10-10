package dev.gigaherz.codegen.codetree.expr;

import com.google.common.reflect.TypeToken;
import dev.gigaherz.codegen.codetree.MethodImplementation;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.MethodVisitor;

@SuppressWarnings("UnstableApiUsage")
public class VarExpression<T,B> extends ValueExpression<T,B>
{
    private final MethodImplementation.LocalVariable<T> localVariable;

    public VarExpression(CodeBlock<B,?,?> cb, MethodImplementation.LocalVariable<T> localVariable)
    {
        super(cb);
        this.localVariable = localVariable;
    }

    @Override
    public TypeToken<T> effectiveType()
    {
        return localVariable.variableType.actualType();
    }

    @Override
    public TypeProxy<T> proxyType()
    {
        return localVariable.variableType;
    }

    @Override
    public void compile(MethodVisitor mv, boolean needsResult)
    {
        if (needsResult) MethodImplementation.LocalLoad.compile(localVariable, mv);
    }
}

