package dev.gigaherz.codegen.codetree.expr;

import dev.gigaherz.codegen.api.codetree.info.FieldInfo;
import dev.gigaherz.codegen.api.codetree.info.MethodInfo;
import dev.gigaherz.codegen.codetree.CompileTerminationMode;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.ToIntFunction;

@SuppressWarnings("UnstableApiUsage")
public interface CodeBlockInternal<B, M> extends CodeBlock<B, M>
{
    LRef<?> fieldRef(ValueExpression<?, B> objRef, FieldInfo<?> fieldInfo);

    <T> ValueExpression<T, B> field(ValueExpression<?, B> objRef, FieldInfo<?> field);

    CodeBlock<B, M> superCall(MethodInfo<?> method, List<ValueExpression<?, B>> values);

    <R> ValueExpression<R, B> methodCall(ValueExpression<?, B> objRef, MethodInfo<R> method, List<ValueExpression<?, B>> values);

    void popStack();

    void pushStack(int count);

    void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult);

    CompileTerminationMode compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, @Nullable Label jumpEnd);

    void pushStack(TypeProxy<?> returnType);

    default <T> CodeBlockInternal<T, M> childBlock()
    {
        return childBlock(null, null);
    }
    default <T> CodeBlockInternal<T, M> childBlock(Label breakLabel)
    {
        return childBlock(breakLabel, null);
    }
    <T> CodeBlockInternal<T, M> childBlock(@Nullable Label breakLabel, @Nullable Label continueLabel);

    boolean isEmpty();

    void beforeExpressionCompile();
    void afterExpressionCompile(boolean needsResult);

    @Nullable
    Label breakLabel();
    @Nullable
    Label continueLabel();
}
