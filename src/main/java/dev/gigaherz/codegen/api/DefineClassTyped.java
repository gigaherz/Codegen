package dev.gigaherz.codegen.api;

import com.google.common.reflect.TypeToken;

public interface DefineClassTyped<C> extends DefineClass<C>
{
    DefineClass<C> implementing(TypeToken<?> interfaceClass);

    default DefineClass<C> implementing(Class<?> baseClass)
    {
        return implementing(TypeToken.of(baseClass));
    }
}
