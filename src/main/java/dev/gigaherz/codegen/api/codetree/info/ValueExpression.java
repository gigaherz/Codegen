package dev.gigaherz.codegen.api.codetree.info;

import org.objectweb.asm.MethodVisitor;

public interface ValueExpression<F>
{
    void compile(MethodVisitor mv);
}
