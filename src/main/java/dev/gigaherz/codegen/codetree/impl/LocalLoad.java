package dev.gigaherz.codegen.codetree.impl;

import dev.gigaherz.codegen.codetree.CompileTerminationMode;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.ToIntFunction;

public class LocalLoad extends InstructionSource
{
    private final MethodImplementation<?> mi;
    String localName;
    int localNumber;

    public LocalLoad(MethodImplementation<?> mi, String localName)
    {
        this.mi = mi;
        this.localName = localName;
    }

    public LocalLoad(MethodImplementation<?> mi, int localNumber)
    {
        this.mi = mi;
        this.localNumber = localNumber;
    }

    @Override
    public CompileTerminationMode compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, Label jumpEnd, boolean needsResult)
    {
        mv.visitLabel(mi.makeLabel());

        LocalVariable<?> localVariable;

        if (localName != null)
        {
            localVariable = mi.getLocalVariable(localName);
            localNumber = localVariable.index;
        }
        else
        {
            int localNumber = this.localNumber;
            localVariable = mi.getLocalVariable(localNumber);
        }

        compile(localVariable, mv);

        return CompileTerminationMode.NORMAL;
    }

    public static void compile(LocalVariable<?> localVariable, MethodVisitor mv)
    {
        if (!localVariable.variableType.isPrimitive())
        {
            mv.visitVarInsn(Opcodes.ALOAD, localVariable.index);
        }
        else
        {
            Class<?> rawType = localVariable.variableType.getSafeRawType();
            if (rawType == long.class)
            {
                mv.visitVarInsn(Opcodes.LLOAD, localVariable.index);
            }
            else if (rawType == float.class)
            {
                mv.visitVarInsn(Opcodes.FLOAD, localVariable.index);
            }
            else if (rawType == double.class)
            {
                mv.visitVarInsn(Opcodes.DLOAD, localVariable.index);
            }
            else //if (type.getRawType() == int.class || type.getRawType() == short.class || type.getRawType() == byte.class || type.getRawType() == boolean.class)
            {
                mv.visitVarInsn(Opcodes.ILOAD, localVariable.index);
            }
        }
    }
}
