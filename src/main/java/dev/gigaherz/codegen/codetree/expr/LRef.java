package dev.gigaherz.codegen.codetree.expr;

import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.MethodVisitor;

import java.util.function.ToIntFunction;

public interface LRef<T>
{
    TypeProxy<T> targetType();

    ValueExpression<T, ?> value();

    void compileBefore(ToIntFunction<Object> defineConstant, MethodVisitor mv);

    void compileAfter(ToIntFunction<Object> defineConstant, MethodVisitor mv);

    boolean isLocal();

    int getLocalIndex();
}
