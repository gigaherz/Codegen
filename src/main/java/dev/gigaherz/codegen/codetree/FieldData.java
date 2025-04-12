package dev.gigaherz.codegen.codetree;

import dev.gigaherz.codegen.api.codetree.info.ClassInfo;
import dev.gigaherz.codegen.api.codetree.info.FieldInfo;
import dev.gigaherz.codegen.type.TypeProxy;

@SuppressWarnings("UnstableApiUsage")
public class FieldData<T> implements FieldInfo<T>
{
    public String name;
    public int modifiers;
    public TypeProxy<?> type;
    public ClassInfo<?> owner;

    public FieldData(ClassInfo<?> owner, String name, TypeProxy<?> type, int modifiers)
    {
        this.owner = owner;
        this.name = name;
        this.type = type;
        this.modifiers = modifiers;
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

    @SuppressWarnings("unchecked")
    @Override
    public TypeProxy<T> type()
    {
        return (TypeProxy<T>) this.type;
    }

    @Override
    public ClassInfo<?> owner()
    {
        return owner;
    }
}
