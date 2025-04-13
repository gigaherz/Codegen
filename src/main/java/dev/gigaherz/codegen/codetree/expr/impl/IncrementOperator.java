package dev.gigaherz.codegen.codetree.expr.impl;

import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.expr.LRef;
import dev.gigaherz.codegen.codetree.impl.MethodImplementation;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.ToIntFunction;

public class IncrementOperator<X, B> extends ValueExpressionImpl<X, B>
{
    private final LRef<X> target;
    private final boolean pre;
    private final boolean inc;

    public IncrementOperator(CodeBlockInternal<B, ?> cb, LRef<X> target, boolean pre, boolean inc)
    {
        super(cb);
        this.target = target;
        this.pre = pre;
        this.inc = inc;
    }

    @Override
    public TypeProxy<X> effectiveType()
    {
        return target.targetType();
    }

    @Override
    public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult, TypeProxy<?> returnInsnType)
    {
        cb.beforeExpressionCompile();

        var isLocal = target.isLocal();
        var rawType = target.targetType().getSafeRawType();
        var skipStore = isLocal && MethodImplementation.isInteger(rawType);

        if (!skipStore)
        {
            target.compileBefore(defineConstant, mv);
        }


        if (!skipStore || needsResult) target.value().compile(defineConstant, mv, true, null);

        if (!pre && needsResult)
        {
            if (rawType == long.class || rawType == double.class)
            {
                mv.visitInsn(Opcodes.DUP2_X1);
                cb.dupStackSkip(2, 1);
            }
            else
            {
                mv.visitInsn(Opcodes.DUP_X1);
                cb.dupStackSkip(1, 1);
            }
        }

        if (inc)
        {
            if (isLocal && MethodImplementation.isInteger(rawType))
            {
                mv.visitIincInsn(target.getLocalIndex(), 1);
            }
            else if (MethodImplementation.isInteger(rawType))
            {
                cb.pushStack(1);
                mv.visitInsn(Opcodes.ICONST_1);
                cb.popStack(1);
                cb.popStack(1);
                mv.visitInsn(Opcodes.IADD);
                cb.pushStack(1);
            }
            else if (rawType == long.class)
            {
                cb.pushStack(2);
                mv.visitInsn(Opcodes.LCONST_1);
                cb.popStack(2);
                cb.popStack(2);
                mv.visitInsn(Opcodes.LADD);
                cb.pushStack(2);
            }
            else if (rawType == float.class)
            {
                cb.pushStack(1);
                mv.visitInsn(Opcodes.FCONST_1);
                cb.popStack(1);
                cb.popStack(1);
                mv.visitInsn(Opcodes.FADD);
                cb.pushStack(1);
            }
            else if (rawType == double.class)
            {
                cb.pushStack(2);
                mv.visitInsn(Opcodes.DCONST_1);
                cb.popStack(2);
                cb.popStack(2);
                mv.visitInsn(Opcodes.DADD);
                cb.pushStack(2);
            }
        }
        else
        {
            if (MethodImplementation.isInteger(rawType))
            {
                cb.pushStack(1);
                mv.visitInsn(Opcodes.ICONST_1);
                cb.popStack(1);
                cb.popStack(1);
                mv.visitInsn(Opcodes.ISUB);
                cb.pushStack(1);
            }
            else if (rawType == long.class)
            {
                cb.pushStack(2);
                mv.visitInsn(Opcodes.LCONST_1);
                cb.popStack(2);
                cb.popStack(2);
                mv.visitInsn(Opcodes.LSUB);
                cb.pushStack(2);
            }
            else if (rawType == float.class)
            {
                cb.pushStack(1);
                mv.visitInsn(Opcodes.FCONST_1);
                cb.popStack(1);
                cb.popStack(1);
                mv.visitInsn(Opcodes.FSUB);
                cb.pushStack(1);
            }
            else if (rawType == double.class)
            {
                cb.pushStack(2);
                mv.visitInsn(Opcodes.DCONST_1);
                cb.popStack(2);
                cb.popStack(2);
                mv.visitInsn(Opcodes.DSUB);
                cb.pushStack(2);
            }
        }

        if (pre && needsResult)
        {
            if (rawType == long.class || rawType == double.class)
            {
                mv.visitInsn(Opcodes.DUP2_X1);
                cb.dupStackSkip(2, 1);
            }
            else
            {
                mv.visitInsn(Opcodes.DUP_X1);
                cb.dupStackSkip(1, 1);
            }
        }

        if (!skipStore) target.compileAfter(defineConstant, mv);

        cb.afterExpressionCompile(needsResult);
    }
}
