package dev.gigaherz.codegen.api;

import dev.gigaherz.codegen.codetree.MethodData;

public interface DefineParam<C, R, P, T extends DefineParam<C, R, P, T>> extends Implementable<C, R>, Annotatable<T>
{
    T withName(String name);
}
