package dev.gigaherz.codegen.api;

import dev.gigaherz.codegen.codetree.MethodInfo;

public interface DefineParam<C, P, T extends DefineParam<C, P, T>> extends Implementable<C, MethodInfo>, Annotatable<T>
{
    T withName(String name);
}
