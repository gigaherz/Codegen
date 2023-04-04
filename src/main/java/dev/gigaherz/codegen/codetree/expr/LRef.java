package dev.gigaherz.codegen.codetree.expr;

import com.google.common.reflect.TypeToken;
import org.objectweb.asm.MethodVisitor;

import java.util.function.ToIntFunction;

@SuppressWarnings("UnstableApiUsage")
public interface LRef<T>
{
    TypeToken<T> targetType();

    void compileBefore(ToIntFunction<Object> defineConstant, MethodVisitor mv);

    void compileAfter(ToIntFunction<Object> defineConstant, MethodVisitor mv);
}
