package dev.gigaherz.codegen.codetree.expr.impl;

import dev.gigaherz.codegen.api.codetree.info.MethodInfo;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import dev.gigaherz.codegen.codetree.impl.MethodImplementation;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.function.ToIntFunction;

public class NewExpression<R, B> extends ValueExpressionImpl<R, B>
{
    private final TypeProxy<R> classToken;
    private final MethodInfo<Void> method;
    private final List<ValueExpression<?, B>> values;

    public NewExpression(CodeBlockInternal<B, ?> cb, TypeProxy<R> classToken, MethodInfo<Void> method, List<ValueExpression<?, B>> values)
    {
        super(cb);
        this.classToken = classToken;
        this.method = method;
        this.values = values;
    }

    @Override
    public TypeProxy<R> effectiveType()
    {
        return classToken;
    }

    @Override
    public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult, TypeProxy<?> returnInsnType)
    {
        cb.beforeExpressionCompile();

        mv.visitTypeInsn(Opcodes.NEW, classToken.getInternalName());
        cb.pushStack(1);
        mv.visitInsn(Opcodes.DUP);
        cb.dupStack(1);

        values.forEach(val -> val.compile(defineConstant, mv, true, null));
        for (int i = values.size(); i >= 0; i--) cb.popStack(i == values.size() ? 1 : MethodImplementation.slotCount(values.get(i).effectiveType()));
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, method.owner().thisType().getInternalName(), method.name(), method.getDescriptor(), method.owner().thisType().isInterface());

        cb.afterExpressionCompile(needsResult);
    }
}
