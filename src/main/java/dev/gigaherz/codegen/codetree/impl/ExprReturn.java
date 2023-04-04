package dev.gigaherz.codegen.codetree.impl;

import com.google.common.reflect.TypeToken;
import dev.gigaherz.codegen.codetree.expr.CodeBlock;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.function.ToIntFunction;

@SuppressWarnings("UnstableApiUsage")
public class ExprReturn extends InstructionSource
{
    private final CodeBlock<?, ?> cb;
    private final TypeToken<?> returnType;

    private final ValueExpression<?, ?> value;

    public ExprReturn(CodeBlock<?, ?> cb, TypeToken<?> returnType, ValueExpression<?, ?> value)
    {
        this.cb = cb;
        this.returnType = returnType;
        this.value = value;
    }

    @Override
    public boolean compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, Label jumpEnd, boolean needsResult)
    {
        mv.visitLabel(cb.owner().makeLabel());
        value.compile(defineConstant, mv, true);
        Return.compileReturn(returnType, mv);
        return true;
    }
}
