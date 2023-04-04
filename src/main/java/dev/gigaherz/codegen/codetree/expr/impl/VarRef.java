package dev.gigaherz.codegen.codetree.expr.impl;

import com.google.common.reflect.TypeToken;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.impl.LocalStore;
import dev.gigaherz.codegen.codetree.impl.LocalVariable;
import org.objectweb.asm.MethodVisitor;

import java.util.function.ToIntFunction;

@SuppressWarnings("UnstableApiUsage")
public class VarRef<T, B> extends LRefImpl<T, B>
{
    private final LocalVariable<T> localVariable;

    public VarRef(CodeBlockInternal<B, ?> cb, LocalVariable<T> localVariable)
    {
        super(cb);

        this.localVariable = localVariable;
    }

    @Override
    public TypeToken<T> targetType()
    {
        return localVariable.variableType.actualType();
    }

    @Override
    public void compileBefore(ToIntFunction<Object> defineConstant, MethodVisitor mv)
    {
        // nothing needed before
    }

    @Override
    public void compileAfter(ToIntFunction<Object> defineConstant, MethodVisitor mv)
    {
        LocalStore.compile(localVariable, mv);
        cb.popStack();
    }
}
