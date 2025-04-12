package dev.gigaherz.codegen.api.codetree.info;

import dev.gigaherz.codegen.type.TypeProxy;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public interface ClassInfo<T>
{
    TypeProxy<? super T> superClass();

    TypeProxy<T> thisType();

    List<? extends MethodInfo<?>> constructors();

    List<? extends MethodInfo<?>> methods();

    List<? extends FieldInfo<?>> fields();

    ClassInfo<? super T> superClassInfo();

    default FieldInfo<?> getField(String fieldName)
    {
        return findField(fieldName).orElseThrow(() -> new IllegalStateException("No field found with name " + fieldName));
    }

    Optional<FieldInfo<?>> findField(String fieldName);
}
