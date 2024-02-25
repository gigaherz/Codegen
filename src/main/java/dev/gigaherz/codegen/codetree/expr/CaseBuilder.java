package dev.gigaherz.codegen.codetree.expr;

import java.util.List;

public interface CaseBuilder<T, R, B>
{
    CaseBuilder<T, R, B> caseWhen(T value, CodeBlock<R, B> body);
    CaseBuilder<T, R, B> caseWhen(List<T> values, CodeBlock<R, B> body);
    CaseBuilder<T, R, B> caseDefault(List<T> values, CodeBlock<R, B> body);
}
