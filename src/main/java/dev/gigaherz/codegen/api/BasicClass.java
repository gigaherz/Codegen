package dev.gigaherz.codegen.api;

import dev.gigaherz.codegen.type.TypeProxy;

public interface BasicClass<T> extends ClassDef<T>, Annotatable<BasicClass<T>>
{
    DefineClass<T> implementing(TypeProxy<?> interfaceClass);

    default DefineClass<T> implementing(Class<?> baseClass)
    {
        return implementing(TypeProxy.of(baseClass));
    }

    // default: package-private
    BasicClass<T> setPublic();

    BasicClass<T> setPrivate();

    BasicClass<T> setProtected();

    // default: non-final
    BasicClass<T> setFinal();

    // nested default: inner; top-level: N/A
    BasicClass<T> setStatic();

    // default: not abstract
    BasicClass<T> setAbstract();
}
