package dev.gigaherz.codegen.codetree.expr.impl;

import com.google.common.reflect.TypeToken;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import org.objectweb.asm.MethodVisitor;

import java.util.function.ToIntFunction;

@SuppressWarnings("UnstableApiUsage")
public class CodeBlockExpression<B, P> extends ValueExpressionImpl<B, P>
{
    private final CodeBlockInternal<B, ?> thisBlock;

    public CodeBlockExpression(CodeBlockInternal<P, ?> cb, CodeBlockInternal<B, ?> childCb)
    {
        super(cb);
        this.thisBlock = childCb;
    }

    @Override
    public TypeToken<B> effectiveType()
    {
        return thisBlock.returnType();
    }

    @Override
    public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult)
    {
        cb.beforeExpressionCompile();
        thisBlock.compile(defineConstant, mv, needsResult);
        cb.afterExpressionCompile(needsResult);
    }
}
