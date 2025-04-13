package dev.gigaherz.codegen.codetree.expr;

import dev.gigaherz.codegen.api.codetree.info.FieldInfo;
import dev.gigaherz.codegen.api.codetree.info.MethodInfo;
import dev.gigaherz.codegen.codetree.CompileTerminationMode;
import dev.gigaherz.codegen.codetree.impl.MethodImplementation;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.ToIntFunction;

public interface CodeBlockInternal<B, M> extends CodeBlock<B, M>
{
    LRef<?> fieldRef(ValueExpression<?, B> objRef, FieldInfo<?> fieldInfo);

    <T> ValueExpression<T, B> field(ValueExpression<?, B> objRef, FieldInfo<?> field);

    CodeBlock<B, M> superCall(MethodInfo<?> method, List<ValueExpression<?, B>> values);

    <R> ValueExpression<R, B> methodCall(ValueExpression<?, B> objRef, MethodInfo<R> method, List<ValueExpression<?, B>> values);

    default void pushStack(TypeProxy<?> type)
    {
        pushStack(MethodImplementation.slotCount(type));
    }

    void pushStack(int count);

    default void popStack(TypeProxy<?> expected)
    {
        popStack(MethodImplementation.slotCount(expected));
    }

    void popStack(int expected);

    default void dupStack(TypeProxy<?> type)
    {
        dupStack(MethodImplementation.slotCount(type));
    }

    void dupStack(int count);

    default void dupStackSkip(TypeProxy<?> type, int skip)
    {
        dupStackSkip(MethodImplementation.slotCount(type), skip);
    }

    void dupStackSkip(int count, int skip);

    void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult);

    CompileTerminationMode compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, @Nullable Label jumpEnd);

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
