package dev.gigaherz.codegen.codetree.expr;

import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.MethodVisitor;

import java.util.function.ToIntFunction;

@SuppressWarnings("UnstableApiUsage")
public interface LRef<T>
{
    TypeProxy<T> targetType();

    void compileBefore(ToIntFunction<Object> defineConstant, MethodVisitor mv);

    void compileAfter(ToIntFunction<Object> defineConstant, MethodVisitor mv);
}
