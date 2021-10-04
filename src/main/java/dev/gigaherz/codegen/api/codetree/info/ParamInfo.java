package dev.gigaherz.codegen.api.codetree.info;

import com.google.common.reflect.TypeToken;
import dev.gigaherz.codegen.type.TypeProxy;

@SuppressWarnings("UnstableApiUsage")
public interface ParamInfo<T>
{
    TypeProxy<T> paramType();
    String name();
}
