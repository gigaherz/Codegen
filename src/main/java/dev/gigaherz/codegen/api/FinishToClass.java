package dev.gigaherz.codegen.api;

import dev.gigaherz.codegen.api.codetree.info.ClassInfo;
import dev.gigaherz.codegen.api.codetree.info.MethodInfo;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.type.TypeProxy;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface FinishToClass<T> extends Finishable<ClassDef<T>>, DefineClass<T>
{
    @Override
    default <F> DefineField<T, F> field(String name, TypeProxy<F> fieldType)
    {
        return finish().field(name, fieldType);
    }

    @Override
    default <F> DefineMethod<T, F> method(String name, TypeProxy<F> returnType)
    {
        return finish().method(name, returnType);
    }

    @Override
    default DefineMethod<T, Void> constructor()
    {
        return finish().constructor();
    }

    @Override
    default ClassInfo<? extends T> make()
    {
        return finish().make();
    }

    @Override
    default byte[] makeClass()
    {
        return finish().makeClass();
    }

    @Override
    default DefineClass<T> replicateParentConstructors(Predicate<MethodInfo<Void>> filter, Consumer<CodeBlockInternal<Void, Void>> cb)
    {
        return finish().replicateParentConstructors(filter, cb);
    }
}
