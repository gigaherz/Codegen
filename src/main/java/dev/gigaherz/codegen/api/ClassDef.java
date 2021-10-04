package dev.gigaherz.codegen.api;

import dev.gigaherz.codegen.api.codetree.info.ClassInfo;
import dev.gigaherz.codegen.type.TypeProxy;

public interface ClassDef<C> extends DefineClass<C>, ClassInfo<C>, TypeProxy<C>
{
}
