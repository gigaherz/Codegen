package dev.gigaherz.codegen.codetree.expr.impl;

import com.google.common.reflect.TypeToken;
import dev.gigaherz.codegen.api.codetree.info.FieldInfo;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import dev.gigaherz.codegen.codetree.impl.FieldLoad;
import org.objectweb.asm.MethodVisitor;

import java.util.function.ToIntFunction;

@SuppressWarnings("UnstableApiUsage")
public class FieldExpression<T, B> extends ValueExpressionImpl<T, B>
{
    private final ValueExpression<?, B> objRef;
    private final FieldInfo<?> field;

    public FieldExpression(CodeBlockInternal<B, ?> cb, ValueExpression<?, B> objRef, FieldInfo<?> field)
    {
        super(cb);
        this.objRef = objRef;
        this.field = field;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public TypeToken<T> effectiveType()
    {
        return (TypeToken) field.type();
    }

    @Override
    public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult, TypeToken<?> returnInsnType)
    {
        cb.beforeExpressionCompile();

        if (needsResult)
        {
            objRef.compile(defineConstant, mv, true, null);
            FieldLoad.compile(field, mv);
            cb.popStack();
            cb.pushStack(field.type());
        }

        cb.afterExpressionCompile(needsResult);
    }
}
