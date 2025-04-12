package dev.gigaherz.codegen.codetree.impl;

import dev.gigaherz.codegen.api.codetree.info.ClassInfo;
import dev.gigaherz.codegen.api.codetree.info.FieldInfo;
import dev.gigaherz.codegen.codetree.ClassData;
import dev.gigaherz.codegen.codetree.CompileTerminationMode;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nullable;
import java.util.function.ToIntFunction;

public class FieldStore extends InstructionSource
{
    private final MethodImplementation<?> mi;
    private final String fieldName;
    private FieldInfo<?> fieldInfo;
    private final ClassInfo<?> owner;

    public FieldStore(MethodImplementation<?> mi, @Nullable ClassData<?> owner, String fieldName)
    {
        this.mi = mi;
        this.fieldName = fieldName;
        this.owner = owner != null ? owner : mi.methodInfo().owner();
    }

    @Override
    public CompileTerminationMode compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, Label jumpEnd, boolean needsResult)
    {
        mv.visitLabel(mi.makeLabel());

        if (fieldInfo == null)
        {
            fieldInfo = owner.getField(fieldName);
        }

        compile(fieldInfo, mv);

        return CompileTerminationMode.NORMAL;
    }

    public static void compile(FieldInfo<?> fieldInfo, MethodVisitor mv)
    {
        if ((fieldInfo.modifiers() & Opcodes.ACC_STATIC) == 0)
            mv.visitFieldInsn(Opcodes.PUTFIELD, fieldInfo.owner().thisType().getInternalName(), fieldInfo.name(), fieldInfo.type().getDescriptor());
        else
            mv.visitFieldInsn(Opcodes.PUTSTATIC, fieldInfo.owner().thisType().getInternalName(), fieldInfo.name(), fieldInfo.type().getDescriptor());
    }
}
