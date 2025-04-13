package dev.gigaherz.codegen.api;

import dev.gigaherz.codegen.type.TypeProxy;

public interface FieldToken<T>
{
    String name();
    TypeProxy<T> type();
}
