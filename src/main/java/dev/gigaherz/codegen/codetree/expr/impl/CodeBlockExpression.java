package dev.gigaherz.codegen.codetree.expr.impl;

import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.MethodVisitor;

import java.util.function.ToIntFunction;

public class CodeBlockExpression<B, P> extends ValueExpressionImpl<B, P>
{
    private final CodeBlockInternal<B, ?> thisBlock;

    public CodeBlockExpression(CodeBlockInternal<P, ?> cb, CodeBlockInternal<B, ?> childCb)
    {
        super(cb);
        this.thisBlock = childCb;
    }

    @Override
    public TypeProxy<B> effectiveType()
    {
        return thisBlock.returnType();
    }

    @Override
    public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult, TypeProxy<?> returnInsnType)
    {
        cb.beforeExpressionCompile();
        thisBlock.compile(defineConstant, mv, needsResult);
        cb.afterExpressionCompile(needsResult);
    }
}
