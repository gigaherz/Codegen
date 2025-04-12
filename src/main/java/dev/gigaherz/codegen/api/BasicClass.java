package dev.gigaherz.codegen.api;

import dev.gigaherz.codegen.type.TypeProxy;

@SuppressWarnings("UnstableApiUsage")
public interface BasicClass extends ClassDef<Object>, Annotatable<BasicClass>
{
    <T> ClassDefTyped<? extends T> extending(TypeProxy<T> baseClass);

    default <T> ClassDefTyped<? extends T> extending(Class<T> baseClass)
    {
        return extending(TypeProxy.of(baseClass));
    }

    <I> ClassDefTyped<? extends I> implementing(TypeProxy<I> interfaceClass);

    default <T> ClassDefTyped<? extends T> implementing(Class<T> baseClass)
    {
        return implementing(TypeProxy.of(baseClass));
    }

    // default: package-private
    BasicClass setPublic();

    BasicClass setPrivate();

    BasicClass setProtected();

    // default: non-final
    BasicClass setFinal();

    // nested default: inner; top-level: N/A
    BasicClass setStatic();

    // default: not abstract
    BasicClass setAbstract();
}
