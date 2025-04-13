package dev.gigaherz.codegen.codetree.expr.impl;

import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.expr.ComparisonType;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import dev.gigaherz.codegen.codetree.impl.MethodImplementation;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nullable;
import java.util.function.ToIntFunction;

public class LogicExpression<B> extends BooleanExpressionImpl<B>
{
    private final ComparisonType comparisonType;
    private final ValueExpression<?, B> first;
    private final ValueExpression<?, B> second;

    public LogicExpression(CodeBlockInternal<B, ?> cb, ComparisonType comparisonType, ValueExpression<?, B> first, ValueExpression<?, B> second)
    {
        super(cb);
        this.comparisonType = comparisonType;
        this.first = first;
        this.second = second;
    }

    @Override
    public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult, TypeProxy<?> returnInsnType)
    {
        cb.beforeExpressionCompile();
        if (needsResult)
        {
            var jumpFalse = new Label();
            var jumpEnd = new Label();

            compile(defineConstant, mv, null, jumpFalse);

            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitJumpInsn(Opcodes.GOTO, jumpEnd);
            mv.visitLabel(jumpFalse);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitLabel(jumpEnd);
            cb.pushStack(1);
        }
        cb.afterExpressionCompile(needsResult);
    }

    @Override
    public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, @Nullable Label jumpTrue, @Nullable Label jumpFalse)
    {
        if (jumpFalse == null && jumpTrue == null)
            throw new IllegalStateException("Comparison compile called with both labels null");

        cb.beforeExpressionCompile();
        if (first instanceof BooleanExpressionImpl b1 && second instanceof BooleanExpressionImpl b2)
        {
            switch (comparisonType)
            {
                case AND -> {
                    boolean b = jumpFalse == null;
                    if (b) jumpFalse = new Label();
                    b1.compile(defineConstant, mv, null, jumpFalse);
                    b2.compile(defineConstant, mv, jumpTrue, jumpFalse);
                    if (b) mv.visitLabel(jumpFalse);
                }
                case OR -> {
                    boolean b = jumpTrue == null;
                    if (b) jumpTrue = new Label();
                    b1.compile(defineConstant, mv, jumpTrue, null);
                    b2.compile(defineConstant, mv, jumpTrue, jumpFalse);
                    if (b) mv.visitLabel(jumpTrue);
                }
                default -> throw new IllegalStateException("Cannot use GT/LT/GE/LE with non-numeric data types.");
            }
        }
        else if (MethodImplementation.isBoolean(first.effectiveType()))
        {
            switch (comparisonType)
            {
                case AND -> {
                    boolean b = jumpFalse == null;
                    if (b) jumpFalse = new Label();

                    first.compile(defineConstant, mv, true, null);
                    mv.visitJumpInsn(Opcodes.IFEQ, jumpFalse);

                    second.compile(defineConstant, mv, true, null);
                    mv.visitJumpInsn(Opcodes.IFEQ, jumpFalse);

                    if (jumpTrue != null)
                        mv.visitJumpInsn(Opcodes.GOTO, jumpTrue);

                    if (b) mv.visitLabel(jumpFalse);
                }
                case OR -> {
                    boolean b = jumpTrue == null;
                    if (b) jumpTrue = new Label();

                    first.compile(defineConstant, mv, true, null);
                    mv.visitJumpInsn(Opcodes.IFNE, jumpTrue);

                    second.compile(defineConstant, mv, true, null);
                    mv.visitJumpInsn(Opcodes.IFNE, jumpTrue);

                    if (jumpFalse != null)
                        mv.visitJumpInsn(Opcodes.GOTO, jumpFalse);

                    if (b) mv.visitLabel(jumpTrue);
                }
                default -> throw new IllegalStateException("Cannot use GT/LT/GE/LE with non-numeric data types.");
            }
            mv.visitJumpInsn(Opcodes.GOTO, jumpFalse);
        }
        else
        {
            first.compile(defineConstant, mv, true, null);
            second.compile(defineConstant, mv, true, null);

            if (MethodImplementation.isInteger(first.effectiveType()))
            {
                cb.popStack(1);
                cb.popStack(1);
                if (jumpTrue == null)
                {
                    switch (comparisonType)
                    {
                        case GT -> mv.visitJumpInsn(Opcodes.IF_ICMPLE, jumpFalse);
                        case GE -> mv.visitJumpInsn(Opcodes.IF_ICMPLT, jumpFalse);
                        case LT -> mv.visitJumpInsn(Opcodes.IF_ICMPGE, jumpFalse);
                        case LE -> mv.visitJumpInsn(Opcodes.IF_ICMPGT, jumpFalse);
                        case EQ -> mv.visitJumpInsn(Opcodes.IF_ICMPNE, jumpFalse);
                        case NE -> mv.visitJumpInsn(Opcodes.IF_ICMPEQ, jumpFalse);
                        default -> throw new IllegalStateException("Cannot use boolean AND/OR with non-boolean data types.");
                    }
                }
                else
                {
                    switch (comparisonType)
                    {
                        case GT -> mv.visitJumpInsn(Opcodes.IF_ICMPGT, jumpTrue);
                        case GE -> mv.visitJumpInsn(Opcodes.IF_ICMPGE, jumpTrue);
                        case LT -> mv.visitJumpInsn(Opcodes.IF_ICMPLT, jumpTrue);
                        case LE -> mv.visitJumpInsn(Opcodes.IF_ICMPLE, jumpTrue);
                        case EQ -> mv.visitJumpInsn(Opcodes.IF_ICMPEQ, jumpTrue);
                        case NE -> mv.visitJumpInsn(Opcodes.IF_ICMPNE, jumpTrue);
                        default -> throw new IllegalStateException("Cannot use boolean AND/OR with non-boolean data types.");
                    }
                    if (jumpFalse != null)
                        mv.visitJumpInsn(Opcodes.GOTO, jumpFalse);
                }
            }
            else if (MethodImplementation.isLong(first.effectiveType()))
            {
                cb.popStack(2);
                cb.popStack(2);
                mv.visitInsn(Opcodes.LCMP);
                cb.pushStack(1);
                if (jumpTrue == null)
                {
                    switch (comparisonType)
                    {
                        case GT -> mv.visitJumpInsn(Opcodes.IFLE, jumpFalse);
                        case GE -> mv.visitJumpInsn(Opcodes.IFLT, jumpFalse);
                        case LT -> mv.visitJumpInsn(Opcodes.IFGE, jumpFalse);
                        case LE -> mv.visitJumpInsn(Opcodes.IFGT, jumpFalse);
                        case EQ -> mv.visitJumpInsn(Opcodes.IFNE, jumpFalse);
                        case NE -> mv.visitJumpInsn(Opcodes.IFEQ, jumpFalse);
                        default -> throw new IllegalStateException("Cannot use boolean AND/OR with non-boolean data types.");
                    }
                }
                else
                {
                    switch (comparisonType)
                    {
                        case GT -> mv.visitJumpInsn(Opcodes.IFGT, jumpTrue);
                        case GE -> mv.visitJumpInsn(Opcodes.IFGE, jumpTrue);
                        case LT -> mv.visitJumpInsn(Opcodes.IFLT, jumpTrue);
                        case LE -> mv.visitJumpInsn(Opcodes.IFLE, jumpTrue);
                        case EQ -> mv.visitJumpInsn(Opcodes.IFEQ, jumpTrue);
                        case NE -> mv.visitJumpInsn(Opcodes.IFNE, jumpTrue);
                        default -> throw new IllegalStateException("Cannot use boolean AND/OR with non-boolean data types.");
                    }
                    if (jumpFalse != null)
                        mv.visitJumpInsn(Opcodes.GOTO, jumpFalse);
                }
                cb.popStack(1);
            }
            else if (MethodImplementation.isFloat(first.effectiveType()))
            {
                cb.popStack(1);
                cb.popStack(1);
                mv.visitInsn(Opcodes.FCMPL);
                cb.pushStack(1);
                if (jumpTrue == null)
                {
                    switch (comparisonType)
                    {
                        case GT -> mv.visitJumpInsn(Opcodes.IFLE, jumpFalse);
                        case GE -> mv.visitJumpInsn(Opcodes.IFLT, jumpFalse);
                        case LT -> mv.visitJumpInsn(Opcodes.IFGE, jumpFalse);
                        case LE -> mv.visitJumpInsn(Opcodes.IFGT, jumpFalse);
                        case EQ -> mv.visitJumpInsn(Opcodes.IFNE, jumpFalse);
                        case NE -> mv.visitJumpInsn(Opcodes.IFEQ, jumpFalse);
                        default -> throw new IllegalStateException("Cannot use boolean AND/OR with non-boolean data types.");
                    }
                }
                else
                {
                    switch (comparisonType)
                    {
                        case GT -> mv.visitJumpInsn(Opcodes.IFGT, jumpTrue);
                        case GE -> mv.visitJumpInsn(Opcodes.IFGE, jumpTrue);
                        case LT -> mv.visitJumpInsn(Opcodes.IFLT, jumpTrue);
                        case LE -> mv.visitJumpInsn(Opcodes.IFLE, jumpTrue);
                        case EQ -> mv.visitJumpInsn(Opcodes.IFEQ, jumpTrue);
                        case NE -> mv.visitJumpInsn(Opcodes.IFNE, jumpTrue);
                        default -> throw new IllegalStateException("Cannot use boolean AND/OR with non-boolean data types.");
                    }
                    if (jumpFalse != null)
                        mv.visitJumpInsn(Opcodes.GOTO, jumpFalse);
                }
                cb.popStack(1);
            }
            else if (MethodImplementation.isDouble(first.effectiveType()))
            {
                cb.popStack(2);
                cb.popStack(2);
                mv.visitInsn(Opcodes.DCMPL);
                cb.pushStack(1);
                if (jumpTrue == null)
                {
                    switch (comparisonType)
                    {
                        case GT -> mv.visitJumpInsn(Opcodes.IFLE, jumpFalse);
                        case GE -> mv.visitJumpInsn(Opcodes.IFLT, jumpFalse);
                        case LT -> mv.visitJumpInsn(Opcodes.IFGE, jumpFalse);
                        case LE -> mv.visitJumpInsn(Opcodes.IFGT, jumpFalse);
                        case EQ -> mv.visitJumpInsn(Opcodes.IFNE, jumpFalse);
                        case NE -> mv.visitJumpInsn(Opcodes.IFEQ, jumpFalse);
                        default -> throw new IllegalStateException("Cannot use boolean AND/OR with non-boolean data types.");
                    }
                }
                else
                {
                    switch (comparisonType)
                    {
                        case GT -> mv.visitJumpInsn(Opcodes.IFGT, jumpTrue);
                        case GE -> mv.visitJumpInsn(Opcodes.IFGE, jumpTrue);
                        case LT -> mv.visitJumpInsn(Opcodes.IFLT, jumpTrue);
                        case LE -> mv.visitJumpInsn(Opcodes.IFLE, jumpTrue);
                        case EQ -> mv.visitJumpInsn(Opcodes.IFEQ, jumpTrue);
                        case NE -> mv.visitJumpInsn(Opcodes.IFNE, jumpTrue);
                        default -> throw new IllegalStateException("Cannot use boolean AND/OR with non-boolean data types.");
                    }
                    if (jumpFalse != null)
                        mv.visitJumpInsn(Opcodes.GOTO, jumpFalse);
                }
                cb.popStack(1);
            }
            else
            {
                cb.popStack(1);
                cb.popStack(1);
                if (jumpTrue == null)
                {
                    switch (comparisonType)
                    {
                        case EQ -> mv.visitJumpInsn(Opcodes.IF_ACMPNE, jumpFalse);
                        case NE -> mv.visitJumpInsn(Opcodes.IF_ACMPEQ, jumpFalse);
                        default -> throw new IllegalStateException("Cannot use GT/LT/GE/LE/AND/OR with non-numeric data types.");
                    }
                }
                else
                {
                    switch (comparisonType)
                    {
                        case EQ -> mv.visitJumpInsn(Opcodes.IF_ACMPEQ, jumpTrue);
                        case NE -> mv.visitJumpInsn(Opcodes.IF_ACMPNE, jumpTrue);
                        default -> throw new IllegalStateException("Cannot use GT/LT/GE/LE/AND/OR with non-numeric data types.");
                    }
                    if (jumpFalse != null)
                        mv.visitJumpInsn(Opcodes.GOTO, jumpFalse);
                }
            }
        }
        cb.afterExpressionCompile(false);
    }
}


