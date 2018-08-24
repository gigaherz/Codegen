package gigaherz.codegen.api;

import java.lang.annotation.Annotation;

public interface Annotatable<T>
{
    <A extends Annotation> T annotate(A a);
}
