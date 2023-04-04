package dev.gigaherz.codegen.codetree.expr;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import javax.annotation.Nullable;
import java.util.function.ToIntFunction;

public interface BooleanExpression<B> extends ValueExpression<Boolean, B>
{
    void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, @Nullable Label jumpTrue, @Nullable Label jumpFalse);
}
