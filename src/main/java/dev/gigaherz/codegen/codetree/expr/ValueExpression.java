package dev.gigaherz.codegen.codetree.expr;

import com.google.common.reflect.TypeToken;
import dev.gigaherz.codegen.codetree.MethodLookup;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.MethodVisitor;

import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public interface ValueExpression<T, B> extends Expr<B>
{
    TypeToken<T> effectiveType();

    TypeProxy<T> proxyType();

    LRef<?> fieldRef(String fieldName);

    ValueExpression<?, B> field(String fieldName);

    ValueExpression<?, B> methodCall(String name);

    ValueExpression<?, B> methodCall(String name, ValueExpression<?, B>... values);

    ValueExpression<?, B> methodCall(String name, Function<MethodLookup<T>, MethodLookup<T>> lookup, ValueExpression<?, B>... values);

    void compile(MethodVisitor mv, boolean needsResult);
}
