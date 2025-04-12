package dev.gigaherz.codegen.codetree;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import dev.gigaherz.codegen.api.codetree.info.ClassInfo;
import dev.gigaherz.codegen.api.codetree.info.FieldInfo;
import dev.gigaherz.codegen.api.codetree.info.MethodInfo;
import dev.gigaherz.codegen.type.TypeProxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ClassData<T> implements ClassInfo<T>
{
    public final TypeProxy<? super T> superClass;
    public final TypeProxy<?> thisType;

    public final List<MethodInfo<Void>> constructors = Lists.newArrayList();
    public final List<MethodInfo<?>> methods = Lists.newArrayList();
    public final List<FieldInfo<?>> fields = Lists.newArrayList();

    private ClassData<? super T> superClassInfo;


    private ClassData(TypeProxy<? super T> superClass, TypeProxy<?> thisType)
    {
        this.superClass = superClass;
        this.thisType = thisType;
    }

    @Override
    public TypeProxy<? super T> superClass()
    {
        return this.superClass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TypeProxy<T> thisType()
    {
        return (TypeProxy<T>) this.thisType;
    }

    @Override
    public List<dev.gigaherz.codegen.api.codetree.info.MethodInfo<Void>> constructors()
    {
        return this.constructors;
    }

    @Override
    public List<dev.gigaherz.codegen.api.codetree.info.MethodInfo<?>> methods()
    {
        return this.methods;
    }

    @Override
    public List<dev.gigaherz.codegen.api.codetree.info.FieldInfo<?>> fields()
    {
        return this.fields;
    }

    @Override
    public ClassInfo<? super T> superClassInfo()
    {
        if (this.superClassInfo == null)
        {
            this.superClassInfo = getSuperClassInfo(this.superClass);
        }
        return this.superClassInfo;
    }

    public Optional<FieldInfo<?>> findField(String fieldName)
    {
        Optional<FieldInfo<?>> first = fields.stream().filter(f -> Objects.equal(f.name(), fieldName)).findFirst();
        if (first.isPresent())
            return first;
        if (superClassInfo == null)
        {
            if (superClass.getRawType() != Object.class)
            {
                superClassInfo = getSuperClassInfo(superClass);
            }
            else
            {
                return Optional.empty();
            }
        }
        return superClassInfo.findField(fieldName);
    }


    private static final Map<Class<?>, ClassData<?>> classInfoCache = new IdentityHashMap<>();

    public static <C> ClassData<? super C> getSuperClassInfo(Class<C> cls)
    {
        return getClassInfo(cls, TypeProxy.of(cls));
    }

    public static <C> ClassData<? super C> getSuperClassInfo(TypeProxy<C> cls)
    {
        return (ClassData<? super C>) getSuperClassInfo(cls.getSuperclass());
    }

    public static <C> ClassData<C> getClassInfo(TypeProxy<C> cls)
    {
        var rawType = java.util.Objects.requireNonNull(cls.getRawType());
        return getClassInfo(rawType, cls);
    }

    public static <C> ClassData<C> getClassInfo(Class<C> cls)
    {
        return getClassInfo(cls, TypeProxy.of(cls));
    }

    public static <C> ClassData<C> getClassInfo(Class<? super C> cls, TypeProxy<C> clsToken)
    {
        TypeProxy<? super C> superToken = clsToken.getSuperclass();
        ClassData<C> ci = new ClassData<>(superToken, clsToken);
        for (Constructor<?> cnt : cls.getDeclaredConstructors())
        {
            var mi = new MethodData<>(ci, "<init>", TypeProxy.of(void.class), cnt.getModifiers());
            for (Parameter p : cnt.getParameters())
            {
                ParamData<?> pi = new ParamData<>();
                pi.name = p.getName();
                pi.paramType = TypeProxy.of(p.getParameterizedType());
                mi.params.add(pi);
            }
            ci.constructors.add(mi);
        }
        for (Method m : cls.getDeclaredMethods())
        {
            var mi = new MethodData<>(ci, m.getName(), TypeProxy.of(m.getReturnType()), m.getModifiers());
            for (Parameter p : m.getParameters())
            {
                ParamData<?> pi = new ParamData<>();
                pi.name = p.getName();
                pi.paramType = TypeProxy.of(p.getParameterizedType());
                mi.params.add(pi);
            }
            ci.methods.add(mi);
        }
        for (Field f : cls.getDeclaredFields())
        {
            FieldData<?> fi = new FieldData<>(ci, f.getName(), TypeProxy.of(f.getType()), f.getModifiers());
            ci.fields.add(fi);
        }
        return ci;
    }
}
