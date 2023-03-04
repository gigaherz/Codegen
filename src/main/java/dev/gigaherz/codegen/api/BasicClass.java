package dev.gigaherz.codegen.api;

import com.google.common.reflect.TypeToken;

@SuppressWarnings("UnstableApiUsage")
public interface BasicClass extends ClassDef<Object>, Annotatable<BasicClass>
{
    <T> ClassDefTyped<? extends T> extending(TypeToken<T> baseClass);

    default <T> ClassDefTyped<? extends T> extending(Class<T> baseClass)
    {
        return extending(TypeToken.of(baseClass));
    }

    <I> ClassDefTyped<? extends I> implementing(TypeToken<I> interfaceClass);

    default <T> ClassDefTyped<? extends T> implementing(Class<T> baseClass)
    {
        return implementing(TypeToken.of(baseClass));
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
