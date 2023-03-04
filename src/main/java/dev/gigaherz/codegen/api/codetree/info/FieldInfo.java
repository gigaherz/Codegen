package dev.gigaherz.codegen.api.codetree.info;

import com.google.common.reflect.TypeToken;

@SuppressWarnings("UnstableApiUsage")
public interface FieldInfo<T>
{
    String name();
    int modifiers();
    TypeToken<T> type();
}
