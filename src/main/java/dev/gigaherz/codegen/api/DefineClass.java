package dev.gigaherz.codegen.api;

import dev.gigaherz.codegen.api.codetree.info.ClassInfo;
import dev.gigaherz.codegen.api.codetree.info.MethodInfo;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.type.TypeProxy;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface DefineClass<C> extends Finishable<ClassDef<C>>
{
    <F> DefineField<C, F> field(String name, TypeProxy<F> fieldType);

    default <F> DefineField<C, F> field(String name, Class<F> fieldType)
    {
        return field(name, TypeProxy.of(fieldType));
    }

    default <F> DefineField<C, F> field(FieldToken<F> token)
    {
        return field(token.name(), token.type());
    }

    <R> DefineMethod<C, R> method(String name, TypeProxy<R> returnType);

    default <F> DefineMethod<C, F> method(String name, Class<F> fieldType)
    {
        return method(name, TypeProxy.of(fieldType));
    }

    DefineMethod<C, Void> constructor();

    default DefineClass<C> replicateParentConstructors(Consumer<CodeBlockInternal<Void, Void>> cb)
    {
        return replicateParentConstructors(discard -> true, cb);
    }

    DefineClass<C> replicateParentConstructors(Predicate<MethodInfo<Void>> filter, Consumer<CodeBlockInternal<Void, Void>> cb);

    byte[] makeClass();

    ClassInfo<? extends C> make();
}

