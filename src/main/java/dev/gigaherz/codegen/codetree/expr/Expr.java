package dev.gigaherz.codegen.codetree.expr;

public interface Expr<B>
{
    CodeBlockInternal<B, ?> block();
}
