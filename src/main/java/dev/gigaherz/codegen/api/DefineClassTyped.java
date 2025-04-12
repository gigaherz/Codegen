package dev.gigaherz.codegen.api;

import dev.gigaherz.codegen.type.TypeProxy;

public interface DefineClassTyped<C> extends DefineClass<C>
{
    DefineClass<C> implementing(TypeProxy<?> interfaceClass);

    default DefineClass<C> implementing(Class<?> baseClass)
    {
        return implementing(TypeProxy.of(baseClass));
    }
}
