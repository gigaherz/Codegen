package dev.gigaherz.codegen.codetree.expr.impl;

import com.google.common.reflect.TypeToken;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.expr.LRef;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import dev.gigaherz.codegen.codetree.impl.MethodImplementation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.ToIntFunction;

@SuppressWarnings("UnstableApiUsage")
public class AssignExpression<T, B> extends ValueExpressionImpl<T, B>
{
    LRef<T> target;
    ValueExpression<T, B> value;

    public AssignExpression(CodeBlockInternal<B, ?> cb, LRef<T> target, ValueExpression<T, B> value)
    {
        super(cb);
        this.target = target;
        this.value = value;
    }

    @Override
    public TypeToken<T> effectiveType()
    {
        return target.targetType();
    }

    @Override
    public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult, TypeToken<?> returnInsnType)
    {
        cb.beforeExpressionCompile();

        target.compileBefore(defineConstant, mv);

        int valueSize = MethodImplementation.slotCount(value.effectiveType());

        cb.pushStack(valueSize);

        value.compile(defineConstant, mv, true, null);

        if (needsResult)
        {
            cb.pushStack(valueSize);
            mv.visitInsn(valueSize == 2 ? Opcodes.DUP2 : Opcodes.DUP);
        }

        target.compileAfter(defineConstant, mv);
        cb.popStack();

        cb.afterExpressionCompile(needsResult);
    }
}
