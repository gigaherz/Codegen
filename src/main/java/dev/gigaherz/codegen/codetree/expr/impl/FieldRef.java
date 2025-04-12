package dev.gigaherz.codegen.codetree.expr.impl;

import dev.gigaherz.codegen.api.codetree.info.FieldInfo;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import dev.gigaherz.codegen.codetree.impl.FieldStore;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.MethodVisitor;

import java.util.function.ToIntFunction;

@SuppressWarnings("UnstableApiUsage")
public class FieldRef<T, B> extends LRefImpl<T, B>
{
    private final ValueExpression<?, B> objRef;
    private final FieldInfo<T> field;

    public FieldRef(CodeBlockInternal<B, ?> cb, ValueExpression<?, B> objRef, FieldInfo<T> field)
    {
        super(cb);
        this.objRef = objRef;
        this.field = field;
    }

    @Override
    public TypeProxy<T> targetType()
    {
        return field.type();
    }

    public FieldInfo<T> getField()
    {
        return field;
    }

    @Override
    public void compileBefore(ToIntFunction<Object> defineConstant, MethodVisitor mv)
    {
        objRef.compile(defineConstant, mv, true, null);
    }

    @Override
    public void compileAfter(ToIntFunction<Object> defineConstant, MethodVisitor mv)
    {
        FieldStore.compile(field, mv);
        cb.popStack();
        cb.popStack();
    }
}
