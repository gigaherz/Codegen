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

    TypeToken<T> actualType();

    String getInternalName();

    String getName();

    String getClassNameWithoutPackage();

    ClassInfo<T> classInfo();

    default String getDescriptor()
    {
        var rt = getRawType();
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

    @SuppressWarnings({"UnstableApiUsage"})
    class Token<T> implements TypeProxy<T>
    {
        private final TypeToken<T> type;

        private Token(TypeToken<T> type)
        {
            this.type = type;
        }

        @Override
        public TypeToken<T> actualType()
        {
            if (type == null)
                throw new IllegalStateException("Type has not been calculated yet.");
            return this.type;
        }

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
            return type.getRawType().getTypeName();
        }

        @Override
        public ClassInfo<T> classInfo()
        {
            return ClassData.getClassInfo(this);
        }

        @Override
        public boolean isPrimitive()
        {
            return type.isPrimitive();
        }

        @Override
        public boolean isArray()
        {
            return type.isArray();
        }

        @Override
        public boolean isInterface()
        {
            return type.getRawType().isInterface();
        }

        @Override
        public boolean isVoid()
        {
            return type.getRawType() == void.class;
        }

        @Override
        public Class<? super T> getRawType()
        {
            return type.getRawType();
        }

        @Override
        public String toString()
        {
            return type.toString();
        }

        @Override
        public Constructor<T> getConstructor(Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException
        {
            //noinspection unchecked
            return (Constructor<T>) type.getRawType().getConstructor(parameterTypes);
        }

        @Override
        public <T1> boolean isSupertypeOf(TypeProxy<T1> subclass)
        {
            return type.isSupertypeOf(subclass.actualType());
        }

        @Override
        public TypeProxy<? super T> getSuperclass()
        {
            var superClass = type.getRawType().getSuperclass();
            if (superClass == null)
                return null;
            //noinspection unchecked,rawtypes
            return TypeProxy.of((TypeToken)type.getSupertype(superClass));
        }
    }
}
