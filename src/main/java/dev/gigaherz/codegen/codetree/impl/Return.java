package dev.gigaherz.codegen.codetree.impl;

import com.google.common.reflect.TypeToken;
import dev.gigaherz.codegen.codetree.CompileTerminationMode;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.ToIntFunction;

@SuppressWarnings("UnstableApiUsage")
public class Return extends InstructionSource
{
    private final TypeToken<?> returnType;
    private final CodeBlockInternal<?, ?> cb;

    public Return(CodeBlockInternal<?, ?> cb, TypeToken<?> returnType)
    {
        this.cb = cb;
        this.returnType = returnType;
    }

    @Override
    public CompileTerminationMode compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, Label jumpEnd, boolean needsResult)
    {
        mv.visitLabel(cb.owner().makeLabel());

        compileReturn(returnType, mv);

        return CompileTerminationMode.BREAK;
    }

    public static void compileReturn(TypeToken<?> returnType, MethodVisitor mv)
    {
        Class<?> rawType = returnType.getRawType();
        if (rawType == void.class)
        {
            mv.visitInsn(Opcodes.RETURN);
        }
        else if (!returnType.isPrimitive())
        {
            mv.visitInsn(Opcodes.ARETURN);
        }
        else if (rawType == long.class)
        {
            mv.visitInsn(Opcodes.LRETURN);
        }
        else if (rawType == float.class)
        {
            mv.visitInsn(Opcodes.FRETURN);
        }
        else if (rawType == double.class)
        {
            mv.visitInsn(Opcodes.DRETURN);
        }
        else //if (type.getRawType() == int.class || type.getRawType() == short.class || type.getRawType() == byte.class || type.getRawType() == boolean.class)
        {
            mv.visitInsn(Opcodes.IRETURN);
        }
    }
}
