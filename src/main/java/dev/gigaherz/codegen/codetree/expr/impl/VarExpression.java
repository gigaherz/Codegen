package dev.gigaherz.codegen.codetree.expr.impl;

import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.impl.LocalLoad;
import dev.gigaherz.codegen.codetree.impl.LocalVariable;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.MethodVisitor;

import java.util.function.ToIntFunction;

public class VarExpression<T, B> extends ValueExpressionImpl<T, B>
{
    private final LocalVariable<T> localVariable;

    public VarExpression(CodeBlockInternal<B, ?> cb, LocalVariable<T> localVariable)
    {
        super(cb);
        this.localVariable = localVariable;
    }

    @Override
    public TypeProxy<T> effectiveType()
    {
        return localVariable.variableType;
    }

    @Override
    public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult, TypeProxy<?> returnInsnType)
    {
        cb.beforeExpressionCompile();
        if (needsResult)
        {
            LocalLoad.compile(localVariable, mv);
            cb.pushStack(localVariable.variableType);
        }
        cb.afterExpressionCompile(needsResult);
    }
}

