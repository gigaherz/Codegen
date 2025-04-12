package dev.gigaherz.codegen.codetree.expr;

import dev.gigaherz.codegen.api.FieldToken;
import dev.gigaherz.codegen.api.VarToken;
import dev.gigaherz.codegen.codetree.MethodLookup;
import dev.gigaherz.codegen.type.TypeProxy;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public interface ExpressionBuilder<B, M>
{
    <T> ValueExpression<T, B> field(String fieldName);
    <T> ValueExpression<T, B> field(FieldToken<T> fieldToken);

    ValueExpression<?, B> fieldOf(ValueExpression<?, B> objRef, String fieldName);

    ValueExpression<?, B> staticField(TypeProxy<?> type, String fieldName);

    default ValueExpression<?, B> staticField(Class<?> type, String fieldName)
    {
        return staticField(TypeProxy.of(type), fieldName);
    }

    ValueExpression<?, B> thisVar();

    ValueExpression<?, B> superVar();

    <T> ValueExpression<T, B> localVar(String varName);
    <T> ValueExpression<T, B> localVar(VarToken<T> varToken);

    default <R, T> ValueExpression<R, B> thisCall(String methodName)
    {
        return thisCall(methodName, List.of());
    }

    default <R, T> ValueExpression<R, B> thisCall(String methodName, ValueExpression<?, B> val0)
    {
        return thisCall(methodName, List.of(val0));
    }

    default <R, T> ValueExpression<R, B> thisCall(String methodName, ValueExpression<?, B> val0, ValueExpression<?, B> val1)
    {
        return thisCall(methodName, List.of(val0, val1));
    }

    default <R, T> ValueExpression<R, B> thisCall(String methodName, ValueExpression<?, B> val0, ValueExpression<?, B> val1, ValueExpression<?, B> val2)
    {
        return thisCall(methodName, List.of(val0, val1, val2));
    }

    default <R, T> ValueExpression<R, B> thisCall(String methodName, ValueExpression<?, B> val0, ValueExpression<?, B> val1, ValueExpression<?, B> val2, ValueExpression<?, B> val3)
    {
        return thisCall(methodName, List.of(val0, val1, val2, val3));
    }

    <R, T> ValueExpression<R, B> thisCall(String methodName, List<ValueExpression<?, B>> values);

    default <R, T> ValueExpression<R, B> methodCall(ValueExpression<T, B> objRef, String methodName)
    {
        return methodCall(objRef, methodName, List.of());
    }

    default <R, T> ValueExpression<R, B> methodCall(ValueExpression<T, B> objRef, String methodName, ValueExpression<?, B> val0)
    {
        return methodCall(objRef, methodName, List.of(val0));
    }

    default <R, T> ValueExpression<R, B> methodCall(ValueExpression<T, B> objRef, String methodName, ValueExpression<?, B> val0, ValueExpression<?, B> val1)
    {
        return methodCall(objRef, methodName, List.of(val0, val1));
    }

    default <R, T> ValueExpression<R, B> methodCall(ValueExpression<T, B> objRef, String methodName, ValueExpression<?, B> val0, ValueExpression<?, B> val1, ValueExpression<?, B> val2)
    {
        return methodCall(objRef, methodName, List.of(val0, val1, val2));
    }

    default <R, T> ValueExpression<R, B> methodCall(ValueExpression<T, B> objRef, String methodName, ValueExpression<?, B> val0, ValueExpression<?, B> val1, ValueExpression<?, B> val2, ValueExpression<?, B> val3)
    {
        return methodCall(objRef, methodName, List.of(val0, val1, val2, val3));
    }

    <R, T> ValueExpression<R, B> methodCall(ValueExpression<T, B> objRef, String methodName, List<ValueExpression<?, B>> values);

    default <R, T> ValueExpression<R, B> methodCall(ValueExpression<T, B> objRef, String methodName, Function<MethodLookup<T>, MethodLookup<T>> methodLookup)
    {
        return methodCall(objRef, methodName, methodLookup, List.of());
    }

    default  <R, T> ValueExpression<R, B> methodCall(ValueExpression<T, B> objRef, String methodName, Function<MethodLookup<T>, MethodLookup<T>> methodLookup, ValueExpression<?, B> val0)
    {
        return methodCall(objRef, methodName, methodLookup, List.of(val0));
    }

    default <R, T> ValueExpression<R, B> methodCall(ValueExpression<T, B> objRef, String methodName, Function<MethodLookup<T>, MethodLookup<T>> methodLookup, ValueExpression<?, B> val0, ValueExpression<?, B> val1)
    {
        return methodCall(objRef, methodName, methodLookup, List.of(val0, val1));
    }

    default <R, T> ValueExpression<R, B> methodCall(ValueExpression<T, B> objRef, String methodName, Function<MethodLookup<T>, MethodLookup<T>> methodLookup, ValueExpression<?, B> val0, ValueExpression<?, B> val1, ValueExpression<?, B> val2)
    {
        return methodCall(objRef, methodName, methodLookup, List.of(val0, val1, val2));
    }

    default <R, T> ValueExpression<R, B> methodCall(ValueExpression<T, B> objRef, String methodName, Function<MethodLookup<T>, MethodLookup<T>> methodLookup, ValueExpression<?, B> val0, ValueExpression<?, B> val1, ValueExpression<?, B> val2, ValueExpression<?, B> val3)
    {
        return methodCall(objRef, methodName, methodLookup, List.of(val0, val1, val2, val3));
    }

    <R, T> ValueExpression<R, B> methodCall(ValueExpression<T, B> objRef, String methodName, Function<MethodLookup<T>, MethodLookup<T>> methodLookup, List<ValueExpression<?, B>> values);

    default <R> ValueExpression<R, B> staticCall(TypeProxy<?> classToken, String methodName)
    {
        return staticCall(classToken, methodName, List.of());
    }

    default <R> ValueExpression<R, B> staticCall(TypeProxy<?> classToken, String methodName, ValueExpression<?, B> val0)
    {
        return staticCall(classToken, methodName, List.of(val0));
    }

    default <R> ValueExpression<R, B> staticCall(TypeProxy<?> classToken, String methodName, ValueExpression<?, B> val0, ValueExpression<?, B> val1)
    {
        return staticCall(classToken, methodName, List.of(val0, val1));
    }

    default <R> ValueExpression<R, B> staticCall(TypeProxy<?> classToken, String methodName, ValueExpression<?, B> val0, ValueExpression<?, B> val1, ValueExpression<?, B> val2)
    {
        return staticCall(classToken, methodName, List.of(val0, val1, val2));
    }

    default <R> ValueExpression<R, B> staticCall(TypeProxy<?> classToken, String methodName, ValueExpression<?, B> val0, ValueExpression<?, B> val1, ValueExpression<?, B> val2, ValueExpression<?, B> val3)
    {
        return staticCall(classToken, methodName, List.of(val0, val1, val2, val3));
    }

    <R> ValueExpression<R, B> staticCall(TypeProxy<?> classToken, String methodName, List<ValueExpression<?, B>> values);

    default <T> ValueExpression<?, B> staticCall(TypeProxy<T> classToken, String methodName, Function<MethodLookup<T>, MethodLookup<T>> methodLookup)
    {
        return staticCall(classToken, methodName, methodLookup, List.of());
    }

    default <R, T> ValueExpression<R, B> staticCall(TypeProxy<T> classToken, String methodName, Function<MethodLookup<T>, MethodLookup<T>> methodLookup, ValueExpression<?, B> val0)
    {
        return staticCall(classToken, methodName, methodLookup, List.of(val0));
    }

    default <R, T> ValueExpression<R, B> staticCall(TypeProxy<T> classToken, String methodName, Function<MethodLookup<T>, MethodLookup<T>> methodLookup, ValueExpression<?, B> val0, ValueExpression<?, B> val1)
    {
        return staticCall(classToken, methodName, methodLookup, List.of(val0, val1));
    }

    default <R, T> ValueExpression<R, B> staticCall(TypeProxy<T> classToken, String methodName, Function<MethodLookup<T>, MethodLookup<T>> methodLookup, ValueExpression<?, B> val0, ValueExpression<?, B> val1, ValueExpression<?, B> val2)
    {
        return staticCall(classToken, methodName, methodLookup, List.of(val0, val1, val2));
    }

    default <R, T> ValueExpression<R, B> staticCall(TypeProxy<T> classToken, String methodName, Function<MethodLookup<T>, MethodLookup<T>> methodLookup, ValueExpression<?, B> val0, ValueExpression<?, B> val1, ValueExpression<?, B> val2, ValueExpression<?, B> val3)
    {
        return staticCall(classToken, methodName, methodLookup, List.of(val0, val1, val2, val3));
    }

    <R, T> ValueExpression<R, B> staticCall(TypeProxy<T> classToken, String methodName, Function<MethodLookup<T>, MethodLookup<T>> methodLookup, List<ValueExpression<?, B>> values);

    default <R> ValueExpression<R, B> staticCall(Class<?> classToken, String methodName)
    {
        return staticCall(classToken, methodName, List.of());
    }

    default <R> ValueExpression<R, B> staticCall(Class<?> classToken, String methodName, ValueExpression<?, B> val0)
    {
        return staticCall(classToken, methodName, List.of(val0));
    }

    default <R> ValueExpression<R, B> staticCall(Class<?> classToken, String methodName, ValueExpression<?, B> val0, ValueExpression<?, B> val1)
    {
        return staticCall(classToken, methodName, List.of(val0, val1));
    }

    default <R> ValueExpression<R, B> staticCall(Class<?> classToken, String methodName, ValueExpression<?, B> val0, ValueExpression<?, B> val1, ValueExpression<?, B> val2)
    {
        return staticCall(classToken, methodName, List.of(val0, val1, val2));
    }

    default <R> ValueExpression<R, B> staticCall(Class<?> classToken, String methodName, ValueExpression<?, B> val0, ValueExpression<?, B> val1, ValueExpression<?, B> val2, ValueExpression<?, B> val3)
    {
        return staticCall(classToken, methodName, List.of(val0, val1, val2, val3));
    }

    default <R> ValueExpression<R, B> staticCall(Class<?> classToken, String methodName, List<ValueExpression<?, B>> values)
    {
        return this.staticCall(TypeProxy.of(classToken), methodName, values);
    }

    default <R, T> ValueExpression<R, B> staticCall(Class<T> classToken, String methodName, Function<MethodLookup<T>, MethodLookup<T>> methodLookup)
    {
        return staticCall(classToken, methodName, methodLookup, List.of());
    }

    default <R, T> ValueExpression<R, B> staticCall(Class<T> classToken, String methodName, Function<MethodLookup<T>, MethodLookup<T>> methodLookup, ValueExpression<?, B> val0)
    {
        return staticCall(classToken, methodName, methodLookup, List.of(val0));
    }

    default <R, T> ValueExpression<R, B> staticCall(Class<T> classToken, String methodName, Function<MethodLookup<T>, MethodLookup<T>> methodLookup, ValueExpression<?, B> val0, ValueExpression<?, B> val1)
    {
        return staticCall(classToken, methodName, methodLookup, List.of(val0, val1));
    }

    default <R, T> ValueExpression<R, B> staticCall(Class<T> classToken, String methodName, Function<MethodLookup<T>, MethodLookup<T>> methodLookup, ValueExpression<?, B> val0, ValueExpression<?, B> val1, ValueExpression<?, B> val2)
    {
        return staticCall(classToken, methodName, methodLookup, List.of(val0, val1, val2));
    }

    default <R, T> ValueExpression<R, B> staticCall(Class<T> classToken, String methodName, Function<MethodLookup<T>, MethodLookup<T>> methodLookup, ValueExpression<?, B> val0, ValueExpression<?, B> val1, ValueExpression<?, B> val2, ValueExpression<?, B> val3)
    {
        return staticCall(classToken, methodName, methodLookup, List.of(val0, val1, val2, val3));
    }

    default <R, T> ValueExpression<R, B> staticCall(Class<T> classToken, String methodName, Function<MethodLookup<T>, MethodLookup<T>> methodLookup, List<ValueExpression<?, B>> values)
    {
        return this.staticCall(TypeProxy.of(classToken), methodName, methodLookup, values);
    }

    <C> ValueExpression<C, B> iif(BooleanExpression<B> condition, ValueExpression<C, B> trueBranch, ValueExpression<C, B> falseBranch);

    <T> ValueExpression<T, B> iif(BooleanExpression<B> condition, Consumer<CodeBlock<T, M>> trueBranch, Consumer<CodeBlock<T, M>> falseBranch);

    BooleanExpression<B> gt(ValueExpression<?, B> x, ValueExpression<?, B> y);

    BooleanExpression<B> ge(ValueExpression<?, B> x, ValueExpression<?, B> y);

    BooleanExpression<B> lt(ValueExpression<?, B> x, ValueExpression<?, B> y);

    BooleanExpression<B> le(ValueExpression<?, B> x, ValueExpression<?, B> y);

    BooleanExpression<B> eq(ValueExpression<?, B> x, ValueExpression<?, B> y);

    BooleanExpression<B> ne(ValueExpression<?, B> x, ValueExpression<?, B> y);

    BooleanExpression<B> and(ValueExpression<?, B> a, ValueExpression<?, B> b);

    BooleanExpression<B> or(ValueExpression<?, B> a, ValueExpression<?, B> b);

    BooleanExpression<B> not(ValueExpression<?, B> a);

    ValueExpression<?, B> add(ValueExpression<?, B> a, ValueExpression<?, B> b);
    ValueExpression<?, B> sub(ValueExpression<?, B> a, ValueExpression<?, B> b);
    ValueExpression<?, B> mul(ValueExpression<?, B> a, ValueExpression<?, B> b);
    ValueExpression<?, B> div(ValueExpression<?, B> a, ValueExpression<?, B> b);
    ValueExpression<?, B> mod(ValueExpression<?, B> a, ValueExpression<?, B> b);
    ValueExpression<?, B> neg(ValueExpression<?, B> b);
    ValueExpression<?, B> bitAnd(ValueExpression<?, B> a, ValueExpression<?, B> b);
    ValueExpression<?, B> bitOr(ValueExpression<?, B> a, ValueExpression<?, B> b);
    ValueExpression<?, B> bitXor(ValueExpression<?, B> a, ValueExpression<?, B> b);
    ValueExpression<?, B> bitNot(ValueExpression<?, B> b);
    ValueExpression<?, B> shiftLeft(ValueExpression<?, B> a, ValueExpression<?, B> b);
    ValueExpression<?, B> shiftRight(ValueExpression<?, B> a, ValueExpression<?, B> b);
    ValueExpression<?, B> shiftRightUnsigned(ValueExpression<?, B> a, ValueExpression<?, B> b);

    ValueExpression<?, B> index(ValueExpression<?, B> array, ValueExpression<?, B> index);

    BooleanExpression<B> literal(boolean val);
    ValueExpression<Byte, B> literal(byte val);
    ValueExpression<Short, B> literal(short val);
    ValueExpression<Integer, B> literal(int val);
    ValueExpression<Long, B> literal(long val);
    ValueExpression<Float, B> literal(float val);
    ValueExpression<Double, B> literal(double val);
    ValueExpression<String, B> literal(String val);
}
