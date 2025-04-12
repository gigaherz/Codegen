package dev.gigaherz.codegen.codetree.expr.impl;

import com.google.common.reflect.TypeToken;
import dev.gigaherz.codegen.api.codetree.info.MethodInfo;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import dev.gigaherz.codegen.codetree.impl.MethodImplementation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;

@SuppressWarnings("UnstableApiUsage")
public class MethodCallExpression<R, B> extends ValueExpressionImpl<R, B>
{
    @Nullable
    private final ValueExpression<?, B> objRef;
    private final MethodInfo<R> method;
    private final List<ValueExpression<?, B>> lValues;

    public MethodCallExpression(CodeBlockInternal<B, ?> cb, @Nullable ValueExpression<?, B> objRef, MethodInfo<R> method, List<ValueExpression<?, B>> lValues)
    {
        super(cb);
        this.objRef = objRef;
        this.method = method;
        this.lValues = lValues;
    }

    @Override
    public TypeToken<R> effectiveType()
    {
        return method.returnType();
    }

    @Override
    public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult, TypeToken<?> returnInsnType)
    {
        cb.beforeExpressionCompile();
        if (method.isStatic())
        {
            lValues.forEach(val -> val.compile(defineConstant, mv, true, null));
            for (int i = 0; i < lValues.size(); i++) cb.popStack();
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, method.owner().thisType().getInternalName(), method.name(), method.getDescriptor(), method.owner().thisType().isInterface());
        }
        else if (method.name().equals("<init>"))
        {
            Objects.requireNonNull(objRef).compile(defineConstant, mv, true, null);
            lValues.forEach(val -> val.compile(defineConstant, mv, true, null));
            for (int i = 0; i <= lValues.size(); i++) cb.popStack();
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, method.owner().thisType().getInternalName(), method.name(), method.getDescriptor(), method.owner().thisType().isInterface());
        }
        else
        {
            Objects.requireNonNull(objRef).compile(defineConstant, mv, true, null);
            lValues.forEach(val -> val.compile(defineConstant, mv, true, null));
            for (int i = 0; i <= lValues.size(); i++) cb.popStack();
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, method.owner().thisType().getInternalName(), method.name(), method.getDescriptor(), method.owner().thisType().isInterface());
        }
        if (!MethodImplementation.isVoid(method.returnType()))
        {
            cb.pushStack(method.returnType());
            if (!needsResult)
            {
                mv.visitInsn(MethodImplementation.slotCount(method.returnType()) == 2 ? Opcodes.POP2 : Opcodes.POP);
                cb.popStack();
            }
        }
        else if (needsResult)
        {
            throw new IllegalStateException("Method calls needs result but the method return type is void!");
        }
        cb.afterExpressionCompile(needsResult);
    }
}
