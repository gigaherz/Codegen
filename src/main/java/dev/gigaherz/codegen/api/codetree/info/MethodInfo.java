package dev.gigaherz.codegen.api.codetree.info;

import com.google.common.reflect.TypeToken;
import dev.gigaherz.codegen.type.TypeProxy;

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public interface MethodInfo<R>
{
    List<? extends ParamInfo<?>> params();

    TypeToken<R> returnType();

    ClassInfo<?> owner();

    String name();

    int modifiers();

    default String getDescriptor()
    {
        var sb = new StringBuilder();

        sb.append("(");

        for (var param : params())
        {
            sb.append(param.paramType().getDescriptor());
        }

        sb.append(")");

        sb.append(TypeProxy.getTypeDescriptor(returnType()));

        return sb.toString();
    }

    @Nullable
    default String getSignature()
    {
        return null;
    }

    default boolean isStatic()
    {
        return Modifier.isStatic(modifiers());
    }

    default boolean isPublic()
    {
        return Modifier.isPublic(modifiers());
    }

    default boolean isPrivate()
    {
        return Modifier.isPrivate(modifiers());
    }

    default boolean isProtected()
    {
        return Modifier.isProtected(modifiers());
    }
}
