package dev.gigaherz.codegen.codetree.expr.impl;

import dev.gigaherz.codegen.codetree.expr.BooleanExpression;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nullable;
import java.util.function.ToIntFunction;

/** @noinspection UnstableApiUsage*/
public abstract class Literal<T, B> extends ValueExpressionImpl<T,B>
{
    protected final T value;
    protected final TypeProxy<T> valueType;

    public Literal(CodeBlockInternal<B, ?> cb, T value, TypeProxy<T> valueType)
    {
        super(cb);
        this.value = value;
        this.valueType = valueType;
    }

    @Override
    public TypeProxy<T> effectiveType()
    {
        return valueType;
    }

    public static class Number<T extends java.lang.Number, B> extends Literal<T, B>
    {
        public Number(CodeBlockInternal<B, ?> cb, T value, TypeProxy<T> valueType)
        {
            super(cb, value, valueType);
        }

        @Override
        public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult, TypeProxy<?> returnInsnType)
        {
            var raw = valueType.getRawType();
            if (raw == byte.class ||raw == short.class ||raw == int.class)
            {
                var val = value.intValue();
                if (val == 0)
                    mv.visitInsn(Opcodes.ICONST_0);
                else if(val == 1)
                    mv.visitInsn(Opcodes.ICONST_1);
                else if(val == 2)
                    mv.visitInsn(Opcodes.ICONST_2);
                else if(val == 3)
                    mv.visitInsn(Opcodes.ICONST_3);
                else if(val == 4)
                    mv.visitInsn(Opcodes.ICONST_4);
                else if(val == 5)
                    mv.visitInsn(Opcodes.ICONST_5);
                else if(val == -1)
                    mv.visitInsn(Opcodes.ICONST_M1);
                else
                {
                    if (val < 128 && val >= -128)
                        mv.visitIntInsn(Opcodes.BIPUSH, val);
                    else if (val < 32768 && val >= -32768)
                        mv.visitIntInsn(Opcodes.SIPUSH, val);
                    else
                    {
                        int constantIndex = defineConstant.applyAsInt(val);
                        mv.visitLdcInsn(constantIndex);
                    }
                }
            }
            else if (raw == long.class)
            {
                var val = value.longValue();
                if (val == 0)
                    mv.visitInsn(Opcodes.LCONST_0);
                else if(val == 1)
                    mv.visitInsn(Opcodes.LCONST_1);
                else
                {
                    int constantIndex = defineConstant.applyAsInt(val);
                    mv.visitLdcInsn(constantIndex);
                }
            }
            else if (raw == float.class)
            {
                var val = value.intValue();
                if (val == 0)
                    mv.visitInsn(Opcodes.FCONST_0);
                else if(val == 1)
                    mv.visitInsn(Opcodes.FCONST_1);
                else if(val == 2)
                    mv.visitInsn(Opcodes.FCONST_2);
                else
                {
                    int constantIndex = defineConstant.applyAsInt(val);
                    mv.visitLdcInsn(constantIndex);
                }
            }
            else if (raw == double.class)
            {
                var val = value.intValue();
                if (val == 0)
                    mv.visitInsn(Opcodes.DCONST_0);
                else if(val == 1)
                    mv.visitInsn(Opcodes.DCONST_1);
                else
                {
                    int constantIndex = defineConstant.applyAsInt(val);
                    mv.visitLdcInsn(constantIndex);
                }
            }
        }
    }

    public static class String<B> extends Literal<java.lang.String, B>
    {
        public String(CodeBlockInternal<B, ?> cb, java.lang.String value)
        {
            super(cb, value, TypeProxy.of(java.lang.String.class));
        }

        @Override
        public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult, TypeProxy<?> returnInsnType)
        {
            throw new IllegalStateException("TODO -- NOT IMPLEMENTED");
        }
    }

    public static class Bool<B> extends Literal<Boolean, B> implements BooleanExpression<B>
    {

        public Bool(CodeBlockInternal<B, ?> cb, Boolean value)
        {
            super(cb, value, TypeProxy.of(boolean.class));
        }

        @Override
        public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult, TypeProxy<?> returnInsnType)
        {
            throw new IllegalStateException("TODO -- NOT IMPLEMENTED");
        }

        @Override
        public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, @Nullable Label jumpTrue, @Nullable Label jumpFalse)
        {
            throw new IllegalStateException("TODO -- NOT IMPLEMENTED");
        }
    }
}
