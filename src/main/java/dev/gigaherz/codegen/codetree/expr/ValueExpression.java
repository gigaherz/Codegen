package dev.gigaherz.codegen.codetree.expr;

import com.google.common.reflect.TypeToken;
import dev.gigaherz.codegen.api.codetree.info.MethodInfo;
import dev.gigaherz.codegen.codetree.ClassData;
import dev.gigaherz.codegen.codetree.CodeBlock;
import dev.gigaherz.codegen.codetree.MethodLookup;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.MethodVisitor;

import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public interface ValueExpression<R>
{
    TypeToken<R> effectiveType();
    default TypeProxy<R> proxyType() {
        return TypeProxy.of(effectiveType());
    }

    void compile(MethodVisitor mv, boolean needsResult);

    default LRef<?> fieldRef(CodeBlock<?> cb, String fieldName)
    {
        return CodeBlock.fieldRef(this, ClassData.getClassInfo(effectiveType()).getField(fieldName));
    }

    default MethodCallExpression<?> methodCall(String name)
    {
        return methodCall(name, ml -> ml);
    }

    default MethodCallExpression<?> methodCall(String name, ValueExpression<?>... values)
    {
        return methodCall(name, ml -> {
            for(var val : values)
            {
                ml.withParam(val.effectiveType());
            }
            return ml;
        }, values);

    }

    default MethodCallExpression<?> methodCall(String name, Function<MethodLookup<R>, MethodLookup<R>> lookup, ValueExpression<?>... values)
    {
        var ml = new MethodLookup<>(proxyType().classInfo(), name);
        ml = lookup.apply(ml);
        return CodeBlock.methodCall(this, ml.result(), values);
    }
}
