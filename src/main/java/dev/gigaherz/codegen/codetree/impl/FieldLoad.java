package dev.gigaherz.codegen.codetree.impl;

import dev.gigaherz.codegen.api.codetree.info.ClassInfo;
import dev.gigaherz.codegen.api.codetree.info.FieldInfo;
import dev.gigaherz.codegen.codetree.ClassData;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nullable;

public class FieldLoad extends InstructionSource
{
    private final MethodImplementation<?> mi;
    private final String fieldName;
    private FieldInfo<?> fieldInfo;
    private final ClassInfo<?> owner;

    public FieldLoad(MethodImplementation<?> mi, @Nullable ClassData<?> owner, String fieldName)
    {
        this.mi = mi;
        this.fieldName = fieldName;
        this.owner = owner != null ? owner : mi.methodInfo().owner();
    }

    @Override
    public boolean compile(MethodVisitor mv, Label jumpEnd, boolean needsResult)
    {
        mv.visitLabel(mi.makeLabel());

        if (fieldInfo == null)
        {
            fieldInfo = owner.getField(fieldName);
        }

        compile(fieldInfo, mv);

        return false;
    }

    public static void compile(FieldInfo<?> fieldInfo, MethodVisitor mv)
    {
        if ((fieldInfo.modifiers() & Opcodes.ACC_STATIC) == 0)
            mv.visitFieldInsn(Opcodes.GETFIELD, fieldInfo.owner().thisType().getInternalName(), fieldInfo.name(), TypeProxy.getTypeDescriptor(fieldInfo.type()));
        else
            mv.visitFieldInsn(Opcodes.GETSTATIC, fieldInfo.owner().thisType().getInternalName(), fieldInfo.name(), TypeProxy.getTypeDescriptor(fieldInfo.type()));
    }
}
