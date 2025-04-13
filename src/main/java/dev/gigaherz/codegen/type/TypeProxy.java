package dev.gigaherz.codegen.type;

import com.google.common.reflect.TypeToken;
import dev.gigaherz.codegen.api.codetree.info.ClassInfo;
import dev.gigaherz.codegen.codetree.ClassData;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public interface TypeProxy<T>
{
    static Token<?> of(Type type)
    {
        return new Token<>(TypeToken.of(type));
    }

    static <P> Token<P> of(Class<P> type)
    {
        return new Token<>(TypeToken.of(type));
    }

    static <P> Token<P> of(TypeToken<P> type)
    {
        return new Token<>(type);
    }

    String getInternalName();

    String getName();

    String getClassNameWithoutPackage();

    ClassInfo<T> classInfo();

    default String getDescriptor()
    {
        var rt = getSafeRawType();
        if (isPrimitive())
        {
            if (rt == int.class)
            {
                return "I";
            }
            else if (rt == boolean.class)
            {
                return "Z";
            }
            else if (rt == byte.class)
            {
                return "B";
            }
            else if (rt == char.class)
            {
                return "C";
            }
            else if (rt == short.class)
            {
                return "S";
            }
            else if (rt == long.class)
            {
                return "J";
            }
            else if (rt == float.class)
            {
                return "F";
            }
            else if (rt == double.class)
            {
                return "D";
            }
            else if (rt == void.class)
            {
                return "V";
            }
            else
            {
                throw new Error("unrecognized primitive type: " + rt);
            }
        }
        else if (isArray())
        {
            return Objects.requireNonNull(rt).getCanonicalName().replace('.', '/');
        }
        else
        {
            return "L" + getInternalName() + ";";
        }
    }

    @Nullable
    default String getSignature()
    {
        return null;
    }

    default boolean isDynamic() {
        return false;
    }

    boolean isPrimitive();

    boolean isArray();

    boolean isInterface();

    boolean isVoid();

    @Nullable
    Class<? super T> getRawType();

    Constructor<T> getConstructor(Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException;

    <S> boolean isSupertypeOf(TypeProxy<S> subclass);

    TypeProxy<? super T> getSuperclass();

    Class<? super T> getSafeRawType();

    default boolean isAssignableFrom(TypeProxy<?> typeProxy)
    {
        return typeProxy.isSupertypeOf(this);
    }

    @SuppressWarnings({"UnstableApiUsage"})
    record Token<T>(TypeToken<T> actualType) implements TypeProxy<T>
    {
        @Override
        public String getInternalName()
        {
            return getName().replace(".", "/");
        }

        @Override
        public String getName()
        {
            return actualType().getRawType().getName();
        }

        @Override
        public String getClassNameWithoutPackage()
        {
            return actualType.getRawType().getTypeName();
        }

        @Override
        public ClassInfo<T> classInfo()
        {
            return ClassData.getClassInfo(this);
        }

        @Override
        public boolean isPrimitive()
        {
            return actualType.isPrimitive();
        }

        @Override
        public boolean isArray()
        {
            return actualType.isArray();
        }

        @Override
        public boolean isInterface()
        {
            return actualType.getRawType().isInterface();
        }

        @Override
        public boolean isVoid()
        {
            return actualType.getRawType() == void.class;
        }

        @Override
        public Class<? super T> getRawType()
        {
            return actualType.getRawType();
        }

        @Override
        public String toString()
        {
            return actualType.toString();
        }

        @Override
        public Constructor<T> getConstructor(Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException
        {
            //noinspection unchecked
            return (Constructor<T>) actualType.getRawType().getConstructor(parameterTypes);
        }

        @Override
        public <T1> boolean isSupertypeOf(TypeProxy<T1> subclass)
        {
            if (subclass instanceof Token(TypeToken type))
                return actualType.isSupertypeOf(type);
            return isSupertypeOf(subclass.getSuperclass());
        }

        @Override
        public TypeProxy<? super T> getSuperclass()
        {
            var superClass = actualType.getRawType().getSuperclass();
            if (superClass == null)
                return null;
            //noinspection unchecked,rawtypes
            return TypeProxy.of((TypeToken) actualType.getSupertype(superClass));
        }

        @Override
        public Class<? super T> getSafeRawType()
        {
            return getRawType();
        }

        @Override
        public boolean isAssignableFrom(TypeProxy<?> typeProxy)
        {
            return actualType.getRawType().isAssignableFrom(typeProxy.getRawType());
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;
            //noinspection rawtypes
            if (!(obj instanceof Token token)) return false;
            return actualType.equals(token.actualType);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(actualType);
        }
    }
}
