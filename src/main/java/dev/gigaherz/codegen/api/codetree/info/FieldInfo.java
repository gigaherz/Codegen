package dev.gigaherz.codegen.api.codetree.info;

import dev.gigaherz.codegen.type.TypeProxy;

@SuppressWarnings("UnstableApiUsage")
public interface FieldInfo<T>
{
    String name();

    int modifiers();

    TypeProxy<T> type();

    ClassInfo<?> owner();
}
