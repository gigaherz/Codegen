package dev.gigaherz.codegen.codetree.expr.impl;

import dev.gigaherz.codegen.api.codetree.info.MethodInfo;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import dev.gigaherz.codegen.codetree.impl.MethodImplementation;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;

public class MethodCallExpression<R, B> extends ValueExpressionImpl<R, B>
{
    @Nullable
    private final ValueExpression<?, B> objRef;
    private final MethodInfo<R> method;
    private final List<ValueExpression<?, B>> values;

    public MethodCallExpression(CodeBlockInternal<B, ?> cb, @Nullable ValueExpression<?, B> objRef, MethodInfo<R> method, List<ValueExpression<?, B>> values)
    {
        super(cb);
        this.objRef = objRef;
        this.method = method;
        this.values = values;
    }

    @Override
    public TypeProxy<R> effectiveType()
    {
        return method.returnType();
    }

    @Override
    public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult, TypeProxy<?> returnInsnType)
    {
        cb.beforeExpressionCompile();
        if (method.isStatic())
        {
            values.forEach(val -> val.compile(defineConstant, mv, true, null));
            for (int i = values.size()-1; i >= 0; i--) cb.popStack(values.get(i).effectiveType());
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, method.owner().thisType().getInternalName(), method.name(), method.getDescriptor(), method.owner().thisType().isInterface());
        }
        else if (method.name().equals("<init>"))
        {
            Objects.requireNonNull(objRef).compile(defineConstant, mv, true, null);
            values.forEach(val -> val.compile(defineConstant, mv, true, null));
            for (int i = values.size(); i >= 0; i--) cb.popStack(i == values.size() ? 1 : MethodImplementation.slotCount(values.get(i).effectiveType()));
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, method.owner().thisType().getInternalName(), method.name(), method.getDescriptor(), method.owner().thisType().isInterface());
        }
        else
        {
            Objects.requireNonNull(objRef).compile(defineConstant, mv, true, null);
            values.forEach(val -> val.compile(defineConstant, mv, true, null));
            for (int i = values.size(); i >= 0; i--) cb.popStack(i == values.size() ? 1 : MethodImplementation.slotCount(values.get(i).effectiveType()));
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, method.owner().thisType().getInternalName(), method.name(), method.getDescriptor(), method.owner().thisType().isInterface());
        }
        if (!method.returnType().isVoid())
        {
            cb.pushStack(method.returnType());
            if (!needsResult)
            {
                int retSlots = MethodImplementation.slotCount(method.returnType());
                mv.visitInsn(retSlots == 2 ? Opcodes.POP2 : Opcodes.POP);
                cb.popStack(retSlots);
            }
        }
        else if (needsResult)
        {
            throw new IllegalStateException("Method calls needs result but the method return type is void!");
        }
        cb.afterExpressionCompile(needsResult);
    }
}
