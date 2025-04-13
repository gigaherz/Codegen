package dev.gigaherz.codegen.codetree.expr.impl;

import dev.gigaherz.codegen.codetree.expr.BooleanExpression;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import dev.gigaherz.codegen.codetree.impl.MethodImplementation;
import dev.gigaherz.codegen.codetree.impl.Return;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.ToIntFunction;

public class ConditionalExpression<T, B> extends ValueExpressionImpl<T, B>
{
    private final BooleanExpression<B> condition;
    private final ValueExpression<T, B> trueBranch;
    private final ValueExpression<T, B> falseBranch;

    public ConditionalExpression(CodeBlockInternal<B, ?> cb, BooleanExpression<B> condition, ValueExpression<T, B> trueBranch, ValueExpression<T, B> falseBranch)
    {
        super(cb);
        this.condition = condition;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    @Override
    public TypeProxy<T> effectiveType()
    {
        return trueBranch.effectiveType();
    }

    @Override
    public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult, TypeProxy<?> returnInsnType)
    {
        cb.beforeExpressionCompile();

        var jumpEnd = new Label();
        var jumpFalse = new Label();

        condition.compile(defineConstant, mv, null, jumpFalse);
        trueBranch.compile(defineConstant, mv, !MethodImplementation.isVoid(trueBranch.effectiveType()), returnInsnType); // TODO: verify
        if (returnInsnType != null)
            Return.compileReturn(returnInsnType, mv);
        else
            mv.visitJumpInsn(Opcodes.GOTO, jumpEnd);
        cb.popStack(1);
        mv.visitLabel(jumpFalse);
        falseBranch.compile(defineConstant, mv, !MethodImplementation.isVoid(falseBranch.effectiveType()), null);
        if (returnInsnType == null)
            mv.visitLabel(jumpEnd);

        cb.afterExpressionCompile(needsResult);
    }
}
