package dev.gigaherz.codegen.api;

import dev.gigaherz.codegen.codetree.expr.CodeBlock;
import dev.gigaherz.codegen.codetree.expr.ExpressionBuilder;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;

import java.util.function.Function;

public interface DefineField<C, F> extends FinishToClass<C>, Annotatable<DefineField<C, F>>
{
    // default package-private
    DefineField<C, F> setPublic();

    DefineField<C, F> setPrivate();

    DefineField<C, F> setProtected();

    // default instance
    DefineField<C, F> setStatic();

    // default non-final
    DefineField<C, F> setFinal();

    // default no initializer
    DefineField<C, F> initializer(Function<ExpressionBuilder<?,?>, ValueExpression<F, ?>> expr);
}
