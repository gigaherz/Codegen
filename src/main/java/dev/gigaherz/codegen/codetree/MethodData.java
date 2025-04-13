package dev.gigaherz.codegen.codetree;

import com.google.common.collect.Lists;
import dev.gigaherz.codegen.api.codetree.info.ClassInfo;
import dev.gigaherz.codegen.api.codetree.info.MethodInfo;
import dev.gigaherz.codegen.api.codetree.info.ParamInfo;
import dev.gigaherz.codegen.type.TypeProxy;

import java.util.List;

public class MethodData<R> implements MethodInfo<R>
{
    public List<ParamInfo<?>> params = Lists.newArrayList();
    public TypeProxy<R> returnType;
    public ClassData<?> owner;
    public String name;
    public int modifiers;

    public MethodData(ClassData<?> owner, String name, TypeProxy<R> returnType, int modifiers)
    {
        this.owner = owner;
        this.name = name;
        this.returnType = returnType;
        this.modifiers = modifiers;
    }

    @Override
    public List<ParamInfo<?>> params()
    {
        return this.params;
    }

    @Override
    public TypeProxy<R> returnType()
    {
        return this.returnType;
    }

    @Override
    public ClassInfo<?> owner()
    {
        return this.owner;
    }

    @Override
    public String name()
    {
        return this.name;
    }

    @Override
    public int modifiers()
    {
        return this.modifiers;
    }
}
