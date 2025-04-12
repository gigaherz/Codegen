package dev.gigaherz.codegen.api;

import dev.gigaherz.codegen.type.TypeProxy;

@SuppressWarnings("UnstableApiUsage")
public interface VarToken<T>
{
    String name();
    TypeProxy<T> type();
}
