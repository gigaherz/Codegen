package dev.gigaherz.codegen.codetree.expr;

import com.google.common.reflect.TypeToken;
import dev.gigaherz.codegen.api.codetree.info.FieldInfo;
import dev.gigaherz.codegen.codetree.MethodImplementation;
import org.objectweb.asm.MethodVisitor;

@SuppressWarnings("UnstableApiUsage")
public class FieldRef<T, B> extends LRef<T, B>
{
    private final ValueExpression<?, B> objRef;
    private final FieldInfo<T> field;

    public FieldRef(CodeBlock<B,?,?> cb, ValueExpression<?, B> objRef, FieldInfo<T> field)
    {
        super(cb);
        this.objRef = objRef;
        this.field = field;
    }

    @Override
    public TypeToken<T> targetType()
    {
        return field.type();
    }

    public FieldInfo<T> getField()
    {
        return field;
    }

    @Override
    public void compileBefore(MethodVisitor mv)
    {
        objRef.compile(mv, true);
    }

    @Override
    public void compileAfter(MethodVisitor mv)
    {
        MethodImplementation.FieldStore.compile(field, mv);
    }
}
