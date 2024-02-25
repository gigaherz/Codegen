package dev.gigaherz.codegen.codetree.expr;

import com.google.common.reflect.TypeToken;
import dev.gigaherz.codegen.api.VarToken;
import dev.gigaherz.codegen.codetree.MethodLookup;
import dev.gigaherz.codegen.codetree.impl.ForBlock;
import dev.gigaherz.codegen.codetree.impl.InstructionSource;
import dev.gigaherz.codegen.codetree.impl.LocalVariable;
import dev.gigaherz.codegen.codetree.impl.MethodImplementation;

import javax.annotation.Nullable;
import javax.management.ValueExp;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public interface CodeBlock<B, M> extends ExpressionBuilder<B, M>
{
    TypeToken<B> returnType();

    List<InstructionSource> instructions();

    CodeBlock<B, M> local(String name, TypeToken<?> varType);

    CodeBlock<B, M> local(String name, TypeToken<?> varType, ValueExpression<?, B> initializer);

    default <T> CodeBlock<B, M> local(VarToken<T> varToken)
    {
        return local(varToken.name(), varToken.type());
    }

    default CodeBlock<B, M> local(String name, Class<?> varType)
    {
        return local(name, TypeToken.of(varType));
    }

    default CodeBlock<B, M> local(String name, Class<?> varType, ValueExpression<?, B> initializer)
    {
        return local(name, TypeToken.of(varType), initializer);
    }

    void returnVoid();

    <T> void returnVal(ValueExpression<T, M> value);

    CodeBlock<B, M> breakLoop();

    CodeBlock<B, M> continueLoop();

    <T> void breakVal(ValueExpression<?, M> value);

    <T, S> ValueExpression<T, B> set(LRef<T> target, ValueExpression<S, B> value);

    CodeBlock<B, M> compute(ValueExpression<?, B> value);

    default CodeBlock<B, M> assign(LRef<?> target, ValueExpression<?, B> value)
    {
        return compute(set(target, value));
    }


    LRef<?> fieldRef(String fieldName);

    LRef<?> fieldRef(ValueExpression<?, B> objRef, String fieldName);

    LRef<?> localRef(String localName);

    CodeBlock<B, M> exec(ValueExpression<?, B> value);

    CodeBlock<B, M> autoSuperCall();

    default CodeBlock<B, M> superCall(Function<MethodLookup<?>, MethodLookup<?>> methodLookup)
    {
        return superCall(methodLookup, List.of());
    }

    default CodeBlock<B, M> superCall(Function<MethodLookup<?>, MethodLookup<?>> methodLookup, ValueExpression<?, B> val0)
    {
        return superCall(methodLookup, List.of(val0));
    }

    default CodeBlock<B, M> superCall(Function<MethodLookup<?>, MethodLookup<?>> methodLookup, ValueExpression<?, B> val0, ValueExpression<?, B> val1)
    {
        return superCall(methodLookup, List.of(val0, val1));
    }

    default CodeBlock<B, M> superCall(Function<MethodLookup<?>, MethodLookup<?>> methodLookup, ValueExpression<?, B> val0, ValueExpression<?, B> val1, ValueExpression<?, B> val2)
    {
        return superCall(methodLookup, List.of(val0, val1, val2));
    }

    default CodeBlock<B, M> superCall(Function<MethodLookup<?>, MethodLookup<?>> methodLookup, ValueExpression<?, B> val0, ValueExpression<?, B> val1, ValueExpression<?, B> val2, ValueExpression<?, B> val3)
    {
        return superCall(methodLookup, List.of(val0, val1, val2, val3));
    }

    @SuppressWarnings("unchecked")
    CodeBlock<B, M> superCall(Function<MethodLookup<?>, MethodLookup<?>> methodLookup, List<ValueExpression<?, B>> values);

    default CodeBlock<B, M> superCall()
    {
        return superCall(List.of());
    }

    default CodeBlock<B, M> superCall(ValueExpression<?, B> val0)
    {
        return superCall(List.of(val0));
    }

    default CodeBlock<B, M> superCall(ValueExpression<?, B> val0, ValueExpression<?, B> val1)
    {
        return superCall(List.of(val0, val1));
    }

    default CodeBlock<B, M> superCall(ValueExpression<?, B> val0, ValueExpression<?, B> val1, ValueExpression<?, B> val2)
    {
        return superCall(List.of(val0, val1, val2));
    }

    default CodeBlock<B, M> superCall(ValueExpression<?, B> val0, ValueExpression<?, B> val1, ValueExpression<?, B> val2, ValueExpression<?, B> val3)
    {
        return superCall(List.of(val0, val1, val2, val3));
    }

    @SuppressWarnings("unchecked")
    CodeBlock<B, M> superCall(List<ValueExpression<?, B>> values);

    @SuppressWarnings("UnusedReturnValue")
    CodeBlock<B, M> ifElse(BooleanExpression<?> condition, Consumer<CodeBlock<B, M>> trueBranch, Consumer<CodeBlock<B, M>> falseBranch);

    /** @noinspection unchecked*/
    <T> CodeBlock<B, M> forLoop(@Nullable Consumer<CodeBlock<T, M>> init, @Nullable BooleanExpression<?> condition, @Nullable Consumer<CodeBlock<T, M>> step, Consumer<CodeBlock<T, M>> body);

    <V, S extends V> CodeBlock<B, M> forEach(String localName, TypeToken<V> varType, ValueExpression<S, B> collection, Consumer<CodeBlock<B, M>> body);

    default <V, S extends V> CodeBlock<B, M> forEach(String localName, Class<V> varType, ValueExpression<S, B> collection, Consumer<CodeBlock<B, M>> body)
    {
        return forEach(localName, TypeToken.of(varType), collection, body);
    }

    default <V, S extends V> CodeBlock<B, M> forEach(VarToken<V> varToken, ValueExpression<S, B> collection, Consumer<CodeBlock<B, M>> body)
    {
        return forEach(varToken.name(), varToken.type(), collection, body);
    }

    <V, S extends V> CodeBlock<B, M> whileLoop(BooleanExpression<?> condition, Consumer<CodeBlock<B, M>> body);

    <V, S extends V> CodeBlock<B, M> doWhile(Consumer<CodeBlock<B, M>> body, BooleanExpression<?> condition);

    <T extends Number> CodeBlock<B, M> switchNumber(ValueExpression<T, B> value, Consumer<CaseBuilder<T, B, M>> cb);
    CodeBlock<B, M> switchString(ValueExpression<String, B> value, Consumer<CaseBuilder<String, B, M>> cb);
    <T extends Enum<T>> CodeBlock<B, M> switchEnum(ValueExpression<T, B> value, Consumer<CaseBuilder<T, B, M>> cb);

    MethodLookup<?> method(String name);

    MethodImplementation<M> owner();
}
