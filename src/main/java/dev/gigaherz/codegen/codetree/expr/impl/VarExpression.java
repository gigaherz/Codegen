package dev.gigaherz.codegen.codetree.expr.impl;

import com.google.common.reflect.TypeToken;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.impl.LocalLoad;
import dev.gigaherz.codegen.codetree.impl.LocalVariable;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.MethodVisitor;

@SuppressWarnings("UnstableApiUsage")
public class VarExpression<T, B> extends ValueExpressionImpl<T, B>
{
    private final LocalVariable<T> localVariable;

    public VarExpression(CodeBlockInternal<B, ?> cb, LocalVariable<T> localVariable)
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
        cb.beforeExpressionCompile();
        if (needsResult)
        {
            cb.pushStack(localVariable.variableType);
            LocalLoad.compile(localVariable, mv);
        }
        cb.afterExpressionCompile(needsResult);
    }
}

