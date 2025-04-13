package dev.gigaherz.codegen;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import dev.gigaherz.codegen.api.*;
import dev.gigaherz.codegen.api.codetree.info.ClassInfo;
import dev.gigaherz.codegen.api.codetree.info.FieldInfo;
import dev.gigaherz.codegen.api.codetree.info.MethodInfo;
import dev.gigaherz.codegen.api.codetree.info.ParamInfo;
import dev.gigaherz.codegen.codetree.ClassData;
import dev.gigaherz.codegen.codetree.expr.CodeBlockInternal;
import dev.gigaherz.codegen.codetree.expr.ExpressionBuilder;
import dev.gigaherz.codegen.codetree.expr.ValueExpression;
import dev.gigaherz.codegen.codetree.impl.MethodImplementation;
import dev.gigaherz.codegen.codetree.impl.SuperCall;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("UnstableApiUsage")
public class ClassMaker
{
    public static boolean generateMethodParameterTable = false;

    private final ClassLoader parentClassLoader;
    private final RuntimeClassLoader dynamicClassLoader;

    public ClassMaker(ClassLoader contextClassLoader)
    {
        this.parentClassLoader = contextClassLoader;
        this.dynamicClassLoader = new RuntimeClassLoader(parentClassLoader);
    }

    public ClassDef<Object> begin(Function<BasicClass<Object>,ClassDef<Object>> builder)
    {
        return builder.apply(new TypedClassImpl<>(TypeProxy.of(Object.class)));
    }

    public <T> ClassDef<T> begin(Class<T> superclass, Function<BasicClass<T>,ClassDef<T>> builder)
    {
        return begin(TypeProxy.of(superclass), builder);
    }

    public <T> ClassDef<T> begin(TypeToken<T> superclass, Function<BasicClass<T>,ClassDef<T>> builder)
    {
        return begin(TypeProxy.of(superclass), builder);
    }

    public <T> ClassDef<T> begin(TypeProxy<T> superclass, Function<BasicClass<T>,ClassDef<T>> builder)
    {
        return builder.apply(new TypedClassImpl<>(superclass));
    }

    public static <T> FieldToken<T> fieldToken(String name, Class<T> type)
    {
        final TypeProxy<T> typeToken = TypeProxy.of(type);
        return fieldToken(name, typeToken);
    }

    private static <T> FieldToken<T> fieldToken(String name, TypeProxy<T> typeToken)
    {
        return new FieldToken<T>()
        {
            @Override
            public String name()
            {
                return name;
            }

            @Override
            public TypeProxy<T> type()
            {
                return typeToken;
            }
        };
    }

    public static <T> VarToken<T> varToken(String name, Class<T> type)
    {
        final TypeProxy<T> typeToken = TypeProxy.of(type);
        return varToken(name, typeToken);
    }

    private static <T> VarToken<T> varToken(String name, TypeProxy<T> typeToken)
    {
        return new VarToken<T>()
        {
            @Override
            public String name()
            {
                return name;
            }

            @Override
            public TypeProxy<T> type()
            {
                return typeToken;
            }
        };
    }

    public class TypedClassImpl<C, T extends C> extends ClassImpl<C, T> implements BasicClass<T>
    {
        public TypedClassImpl(TypeProxy<C> superclass)
        {
            super(superclass);
        }

        @Override
        public ClassDef<T> implementing(TypeProxy<?> interfaceClass)
        {
            implementingInternal(interfaceClass);
            return this;
        }

        @Override
        public BasicClass<T> setPublic()
        {
            modifiers |= Modifier.PUBLIC;
            return this;
        }

        @Override
        public BasicClass<T> setPrivate()
        {
            modifiers |= Modifier.PRIVATE;
            return this;
        }

        @Override
        public BasicClass<T> setProtected()
        {
            modifiers |= Modifier.PROTECTED;
            return this;
        }

        @Override
        public BasicClass<T> setFinal()
        {
            modifiers |= Modifier.FINAL;
            return this;
        }

        @Override
        public BasicClass<T> setStatic()
        {
            modifiers |= Modifier.STATIC;
            return this;
        }

        @Override
        public BasicClass<T> setAbstract()
        {
            modifiers |= Modifier.ABSTRACT;
            return this;
        }

        @Override
        public <A extends Annotation> BasicClass<T> annotate(A a)
        {
            annotations.add(a);
            return this;
        }
    }

    private abstract class ClassImpl<C, T extends C> implements ClassDef<T>
    {
        protected final List<Annotation> annotations = Lists.newArrayList();
        protected final List<FieldImpl<?>> fields = Lists.newArrayList();
        protected final List<ConstructorImpl<?>> constructors = Lists.newArrayList();
        protected final List<MethodImpl<?>> methods = Lists.newArrayList();
        protected final TypeProxy<C> superClass;
        protected final List<TypeProxy<?>> superInterfaces = Lists.newArrayList();
        protected int modifiers;

        private static int nextClassId = 1;
        private final int classId = (nextClassId++);// + classId
        protected String name = /*this.getClass().getPackageName() + "." + */ "C" + classId;
        protected String fullName = this.getClass().getPackageName() + "." + name;

        public ClassImpl(TypeProxy<C> baseClass)
        {
            this.superClass = baseClass;
        }

        public void implementingInternal(TypeProxy<?> interfaceClass)
        {
            if (!interfaceClass.getSafeRawType().isInterface())
                throw new IllegalStateException("The provided class " + interfaceClass + " is not an interface!");
            superInterfaces.add(interfaceClass);
        }

        @Override
        public <F> DefineField<T, F> field(String name, TypeProxy<F> fieldType)
        {
            FieldImpl<F> field = new FieldImpl<>(name, fieldType);
            fields.add(field);
            return field;
        }

        @Override
        public <R> DefineMethod<T, R> method(String name, TypeProxy<R> returnType)
        {
            var m = new MethodImpl<>(name, returnType);
            methods.add(m);
            return m;
        }

        @Override
        public DefineMethod<T, Void> constructor()
        {
            var m = new ConstructorImpl<>();
            constructors.add(m);
            return m;
        }

        @Override
        public DefineClass<T> replicateParentConstructors(Predicate<MethodInfo<Void>> filter, Consumer<CodeBlockInternal<Void, Void>> cb)
        {
            DefineClass<T> cthis = this;
            var csuper = ClassData.getClassInfo(superClass);
            for(var superConstructor : csuper.constructors())
            {
                if (superConstructor.isStatic()) continue;
                if (!filter.test(superConstructor)) continue;

                var ccon = cthis.constructor();

                if (superConstructor.isPublic()) ccon = ccon.setPublic();
                else if (superConstructor.isProtected()) ccon = ccon.setProtected();
                else if (superConstructor.isPrivate()) ccon = ccon.setPrivate();

                var cargs = ccon.setInstance();
                for(ParamInfo<?> param : superConstructor.params())
                {
                    cargs.param(param.paramType()).withName(param.name());
                }

                cargs.implementation(cb);

                cthis = cargs.finish();
            }
            return cthis;
        }

        @Override
        public String toString()
        {
            return "ClassDefinition[" + fullName + "]";
        }

        public byte[] makeClass()
        {
            var cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

            var interfaces = superInterfaces.stream().map(TypeProxy::getInternalName).toArray(String[]::new);

            cw.visit(Opcodes.V21, modifiers | Opcodes.ACC_SUPER, getInternalName(), getSignature(),
                    superClass.getInternalName(),
                    interfaces);

                /*for(var ann : annotations)
                {
                    cw.visitAnnotation(ann.annotationType())
                }*/

            for (var fi : fields)
            {
                var fname = fi.name;

                var fv = cw.visitField(fi.modifiers, fname, fi.fieldType.getDescriptor(), fi.fieldType.getSignature(), null);

                    /*for(var ann : fi.annotations)
                    {
                        fv.visitAnnotation(ann.annotationType())
                    }*/

                //fv.visitTypeAnnotation()

                //fv.visitAttribute();

                fv.visitEnd();
            }


            for (var mi : constructors)
            {
                mi.makeMethod(cw);
            }

            for (var mi : methods)
            {
                mi.makeMethod(cw);
            }

            return cw.toByteArray();
        }

        @SuppressWarnings("unchecked")
        @Override
        public ClassInfo<T> make()
        {
            return ClassData.getClassInfo((Class<T>) dynamicClassLoader.defineClass(fullName, makeClass()));
        }

        @Override
        public String getInternalName()
        {
            return fullName.replace(".", "/");
        }

        @Override
        public String getName()
        {
            return fullName;
        }

        @Override
        public String getClassNameWithoutPackage()
        {
            return name;
        }

        @Override
        public ClassInfo<T> classInfo()
        {
            return this;
        }

        @Override
        public String getDescriptor()
        {
            return "L" + getInternalName() + ";";
        }

        @Override
        public boolean isPrimitive()
        {
            return false;
        }

        @Override
        public boolean isArray()
        {
            return false;
        }

        @Override
        public boolean isInterface()
        {
            // TODO: maybe in the future
            return false;
        }

        @Override
        public boolean isVoid()
        {
            return false;
        }

        @Override
        public <T1> boolean isSupertypeOf(TypeProxy<T1> subclass)
        {
            if (subclass == this) return true;
            if (!(subclass instanceof ClassImpl)) return false;
            var sup = subclass.getSuperclass();
            while(sup != null)
            {
                if (sup == this) return true;
                if (!(sup instanceof ClassImpl)) return false;
                sup = sup.getSuperclass();
            }
            return false;
        }

        @Override
        public TypeProxy<? super T> getSuperclass()
        {
            return superClass();
        }

        @Override
        public boolean isDynamic()
        {
            return true;
        }

        @Nullable
        @Override
        public Class<? super T> getRawType()
        {
            throw new IllegalStateException("Cannot get the actual type from a ClassMaker, please use make() to generate an actual class first.");
        }

        @Override
        public Class<? super T> getSafeRawType()
        {
            return superClass.getSafeRawType();
        }

        @Override
        public TypeProxy<? super C> superClass()
        {
            return superClass;
        }

        @Override
        public TypeProxy<T> thisType()
        {
            return this;
        }

        @Override
        public List<? extends MethodInfo<Void>> constructors()
        {
            return this.constructors;
        }

        @Override
        public List<? extends MethodInfo<?>> methods()
        {
            return this.methods;
        }

        @Override
        public List<? extends FieldInfo<?>> fields()
        {
            return this.fields;
        }

        @Override
        public ClassInfo<? super C> superClassInfo()
        {
            return ClassData.getSuperClassInfo(superClass);
        }

        @Override
        public Optional<FieldInfo<?>> findField(String fieldName)
        {
            return fields.stream().filter(f -> fieldName.equals(f.name)).findFirst().map(f -> f);
        }

        @Override
        public ClassDef<T> finish()
        {
            return this;
        }

        private class FieldImpl<F> implements DefineField<T, F>, FieldInfo<F>
        {
            protected final List<Annotation> annotations = Lists.newArrayList();
            protected final TypeProxy<F> fieldType;
            protected int modifiers;
            protected Function<ExpressionBuilder<?, ?>, ValueExpression<F, ?>> init;
            protected String name;

            private FieldImpl(String name, TypeProxy<F> fieldType)
            {
                this.name = name;
                this.fieldType = fieldType;
            }

            public FieldImpl(FieldImpl<F> copyFrom)
            {
                this(copyFrom.name, copyFrom.fieldType);

                this.modifiers = copyFrom.modifiers;
                this.init = copyFrom.init;
                annotations.addAll(copyFrom.annotations);
            }

            @Override
            public <A extends Annotation> DefineField<T, F> annotate(A a)
            {
                annotations.add(a);
                return this;
            }

            @Override
            public DefineField<T, F> setPublic()
            {
                modifiers |= Modifier.PUBLIC;
                return this;
            }

            @Override
            public DefineField<T, F> setPrivate()
            {
                modifiers |= Modifier.PRIVATE;
                return this;
            }

            @Override
            public DefineField<T, F> setProtected()
            {
                modifiers |= Modifier.PROTECTED;
                return this;
            }

            @Override
            public DefineField<T, F> setStatic()
            {
                modifiers |= Modifier.STATIC;
                return this;
            }

            @Override
            public DefineField<T, F> setFinal()
            {
                modifiers |= Modifier.FINAL;
                return this;
            }

            @Override
            public DefineField<T, F> initializer(Function<ExpressionBuilder<?, ?>, ValueExpression<F, ?>> expr)
            {
                init = expr;
                return this;
            }

            @Override
            public ClassDef<T> finish()
            {
                return ClassImpl.this;
            }

            @Override
            public String name()
            {
                return this.name;
            }

            @Override
            public int modifiers()
            {
                return this.modifiers;
            }

            @Override
            public TypeProxy<F> type()
            {
                return this.fieldType;
            }

            @Override
            public ClassInfo<?> owner()
            {
                return ClassImpl.this;
            }

        }

        private class ConstructorImpl<WorkaroundBecauseJavaIsStupid> extends MethodImpl<Void>
        {
            public ConstructorImpl(ConstructorImpl<WorkaroundBecauseJavaIsStupid> copyFrom)
            {
                super(copyFrom);
            }

            public ConstructorImpl()
            {
                super("<init>", TypeProxy.of(void.class));
            }

            @Override
            public void makeMethod(ClassWriter cw)
            {
                var mv = cw.visitMethod(this.modifiers, this.name, this.getDescriptor(), this.getSignature(), this.getExceptions());

                    /*for(var ann : this.annotations)
                    {
                        mv.visitAnnotation(ann.annotationType())
                    }*/

                if (generateMethodParameterTable)
                {
                    for (var param : this.params)
                    {
                        mv.visitParameter(param.name, param.modifiers);
                        // mv.visitParameterAnnotation()
                    }
                }

                if ((this.modifiers & Opcodes.ACC_ABSTRACT) == 0)
                {
                    var startLabel = new Label();
                    var endLabel = new Label();

                    MethodImplementation<Void> code = MethodImplementation.begin(this, startLabel);

                    var codeBlock = code.rootBlock();

                    this.impl.accept(codeBlock);

                    var insns = codeBlock.instructions();

                    mv.visitCode();

                    if ((this.modifiers & Opcodes.ACC_STATIC) == 0)
                    {
                        // super

                        if (!insns.isEmpty() && insns.get(0) instanceof SuperCall sc)
                        {
                            insns.removeFirst();

                            sc.compile(cw::newConst, mv, endLabel, false);
                        }
                        else
                        {
                            // default constructor

                            mv.visitLabel(startLabel);
                            mv.visitVarInsn(Opcodes.ALOAD, 0);
                            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superClass.getInternalName(), "<init>", "()V", false);
                        }
                    }

                    // field initializers

                    for (var fi : fields)
                    {
                        var fname = fi.name;

                        if (fi.init != null)
                        {
                            mv.visitLabel(new Label());

                            mv.visitVarInsn(Opcodes.ALOAD, 0); // this

                            var cb = codeBlock.childBlock();

                            var val = fi.init.apply(cb);

                            val.compile(cw::newConst, mv, true, null);

                            mv.visitFieldInsn(Opcodes.PUTFIELD, this.owner().thisType().getInternalName(), fname, fi.fieldType.getDescriptor());
                        }
                    }

                    codeBlock.compile(cw::newConst, mv, endLabel);
                    mv.visitLabel(endLabel);

                    // locals
                    for (var local : code.locals)
                    {
                        var n = local.name == null ? "this" : local.name;
                        mv.visitLocalVariable(n, local.variableType.getDescriptor(), local.variableType.getSignature(), startLabel, endLabel, local.index);
                        // mv.visitLocalVariableAnnotation
                    }

                    mv.visitMaxs(code.maxStack, code.locals.size());
                }
                mv.visitEnd();
            }
        }

        private class MethodImpl<R> implements DefineMethod<T, R>, MethodInfo<R>
        {
            protected final List<Annotation> annotations = Lists.newArrayList();
            protected final List<ParamDefinition<?>> params = Lists.newArrayList();
            protected final List<TypeProxy<? extends Throwable>> exceptions = Lists.newArrayList();
            protected final String name;
            protected final TypeProxy<R> returnType;
            protected int modifiers;
            protected Consumer<CodeBlockInternal<R, R>> impl;


            public MethodImpl(String name, TypeProxy<R> returnType)
            {
                this.name = name;
                this.returnType = returnType;
            }

            public MethodImpl(MethodImpl<R> copyFrom)
            {
                this(copyFrom.name, copyFrom.returnType);
                this.modifiers = copyFrom.modifiers;
                this.impl = copyFrom.impl;
                annotations.addAll(copyFrom.annotations);
            }

            @Override
            public <A extends Annotation> DefineMethod<T, R> annotate(A a)
            {
                annotations.add(a);
                return this;
            }

            @Override
            public DefineMethod<T, R> setPublic()
            {
                modifiers |= Modifier.PUBLIC;
                return this;
            }

            @Override
            public DefineMethod<T, R> setPrivate()
            {
                modifiers |= Modifier.PRIVATE;
                return this;
            }

            @Override
            public DefineMethod<T, R> setProtected()
            {
                modifiers |= Modifier.PROTECTED;
                return this;
            }

            @Override
            public DefineMethod<T, R> setFinal()
            {
                modifiers |= Modifier.FINAL;
                return this;
            }

            @Override
            public DefineArgs0<T, R> setStatic()
            {
                modifiers |= Modifier.STATIC;
                return new DefineArgsImpl0();
            }

            @Override
            public DefineArgs0<T, R> setInstance()
            {
                modifiers &= ~Modifier.STATIC;
                return new DefineArgsImpl0();
            }

            @Override
            public ClassDef<T> finish()
            {
                return ClassImpl.this;
            }

            @Override
            public DefineClass<T> makeAbstract()
            {
                this.modifiers |= Modifier.ABSTRACT;
                return finish();
            }

            protected <P> ParamDefinition<P> addParam(TypeProxy<P> paramType)
            {
                ParamDefinition<P> def = new ParamDefinition<>(paramType);
                params.add(def);
                return def;
            }

            @Override
            public DefineClass<T> implementation(Consumer<CodeBlockInternal<R, R>> code)
            {
                this.impl = code;
                return this;
            }

            @Nullable
            public String[] getExceptions()
            {
                if (!exceptions.isEmpty())
                    return exceptions.stream().map(TypeProxy::getInternalName).toArray(String[]::new);

                return null;
            }

            @Override
            public List<? extends ParamInfo<?>> params()
            {
                return params;
            }

            @Override
            public TypeProxy<R> returnType()
            {
                return returnType;
            }

            @Override
            public ClassInfo<?> owner()
            {
                return ClassImpl.this;
            }

            @Override
            public String name()
            {
                return name;
            }

            @Override
            public int modifiers()
            {
                return 0;
            }

            public void makeMethod(ClassWriter cw)
            {
                var mv = cw.visitMethod(this.modifiers, this.name, this.getDescriptor(), this.getSignature(), this.getExceptions());

                    /*for(var ann : this.annotations)
                    {
                        mv.visitAnnotation(ann.annotationType())
                    }*/

                if (generateMethodParameterTable)
                {
                    for (var param : this.params)
                    {
                        mv.visitParameter(param.name, param.modifiers);

                        // mv.visitParameterAnnotation()
                    }
                }

                if ((this.modifiers & Opcodes.ACC_ABSTRACT) == 0)
                {
                    var startLabel = new Label();
                    var endLabel = new Label();

                    MethodImplementation<R> code = MethodImplementation.begin(this, startLabel);

                    var codeBlock = code.rootBlock();

                    this.impl.accept(codeBlock);

                    mv.visitCode();

                    codeBlock.compile(cw::newConst, mv, endLabel);
                    mv.visitLabel(endLabel);

                    // locals
                    for (var local : code.locals)
                    {
                        var n = local.name == null ? "this" : local.name;
                        mv.visitLocalVariable(n, local.variableType.getDescriptor(), local.variableType.getSignature(), startLabel, endLabel, local.index);
                        // mv.visitLocalVariableAnnotation
                    }

                    mv.visitMaxs(code.maxStack, code.locals.size());
                }

                mv.visitEnd();
            }

            private class Impl implements Implementable<T, R>
            {
                @Override
                public ClassDef<T> finish()
                {
                    return ClassImpl.this;
                }

                @Override
                public DefineClass<T> makeAbstract()
                {
                    modifiers |= Modifier.ABSTRACT;
                    return finish();
                }

                @Override
                public DefineClass<T> implementation(Consumer<CodeBlockInternal<R, R>> code)
                {
                    return MethodImpl.this.implementation(code);
                }
            }

            private class ImplWithParam<P, Z extends DefineParam<T, R, P, Z>> extends Impl
                    implements DefineParam<T, R, P, Z>
            {
                protected final ParamDefinition<P> param;

                private ImplWithParam(ParamDefinition<P> param)
                {
                    this.param = param;
                }

                @Override
                public <A extends Annotation> Z annotate(A a)
                {
                    param.annotations.add(a);
                    return (Z) this;
                }

                @Override
                public Z withName(String name)
                {
                    param.name = name;
                    return (Z) this;
                }
            }

            private class DefineArgsImpl0 extends Impl implements DefineArgs0<T, R>
            {
                @Override
                public <P> DefineArgs1<T, R, P> param(TypeProxy<P> paramClass)
                {
                    return new DefineArgsImpl1<>(addParam(paramClass));
                }
            }

            private class DefineArgsImpl1<P0> extends ImplWithParam<P0, DefineArgs1<T, R, P0>>
                    implements DefineArgs1<T, R, P0>
            {
                private DefineArgsImpl1(ParamDefinition<P0> param)
                {
                    super(param);
                }

                @Override
                public <P> DefineArgs2<T, R, P0, P> param(TypeProxy<P> paramClass)
                {
                    return new DefineArgsImpl2<>(addParam(paramClass));
                }
            }

            private class DefineArgsImpl2<P0, P1> extends ImplWithParam<P1, DefineArgs2<T, R, P0, P1>>
                    implements DefineArgs2<T, R, P0, P1>
            {

                private DefineArgsImpl2(ParamDefinition<P1> param)
                {
                    super(param);
                }

                @Override
                public <P> DefineArgs3<T, R, P0, P1, P> param(TypeProxy<P> paramClass)
                {
                    return new DefineArgsImpl3<>(addParam(paramClass));
                }
            }

            private class DefineArgsImpl3<P0, P1, P2> extends ImplWithParam<P2, DefineArgs3<T, R, P0, P1, P2>>
                    implements DefineArgs3<T, R, P0, P1, P2>
            {

                private DefineArgsImpl3(ParamDefinition<P2> param)
                {
                    super(param);
                }

                @Override
                public <P> DefineArgs4<T, R, P0, P1, P2, P> param(TypeProxy<P> paramClass)
                {
                    return new DefineArgsImpl4<>(addParam(paramClass));
                }
            }

            private class DefineArgsImpl4<P0, P1, P2, P3> extends ImplWithParam<P3, DefineArgs4<T, R, P0, P1, P2, P3>>
                    implements DefineArgs4<T, R, P0, P1, P2, P3>
            {

                private DefineArgsImpl4(ParamDefinition<P3> param)
                {
                    super(param);
                }

                @Override
                public <P> DefineArgs5<T, R, P0, P1, P2, P3, P> param(TypeProxy<P> paramClass)
                {
                    return new DefineArgsImpl5<>(addParam(paramClass));
                }
            }

            private class DefineArgsImpl5<P0, P1, P2, P3, P4>
                    extends ImplWithParam<P4, DefineArgs5<T, R, P0, P1, P2, P3, P4>>
                    implements DefineArgs5<T, R, P0, P1, P2, P3, P4>
            {

                private DefineArgsImpl5(ParamDefinition<P4> param)
                {
                    super(param);
                }

                @Override
                public <P> DefineArgs6<T, R, P0, P1, P2, P3, P4, P> param(TypeProxy<P> paramClass)
                {
                    return new DefineArgsImpl6<>(addParam(paramClass));
                }
            }

            private class DefineArgsImpl6<P0, P1, P2, P3, P4, P5>
                    extends ImplWithParam<P5, DefineArgs6<T, R, P0, P1, P2, P3, P4, P5>>
                    implements DefineArgs6<T, R, P0, P1, P2, P3, P4, P5>
            {

                private DefineArgsImpl6(ParamDefinition<P5> param)
                {
                    super(param);
                }

                @Override
                public <P> DefineArgs7<T, R, P0, P1, P2, P3, P4, P5, P> param(TypeProxy<P> paramClass)
                {
                    return new DefineArgsImpl7<>(addParam(paramClass));
                }
            }

            private class DefineArgsImpl7<P0, P1, P2, P3, P4, P5, P6>
                    extends ImplWithParam<P6, DefineArgs7<T, R, P0, P1, P2, P3, P4, P5, P6>>
                    implements DefineArgs7<T, R, P0, P1, P2, P3, P4, P5, P6>
            {

                private DefineArgsImpl7(ParamDefinition<P6> param)
                {
                    super(param);
                }

                @Override
                public <P> DefineArgs8<T, R, P0, P1, P2, P3, P4, P5, P6, P> param(TypeProxy<P> paramClass)
                {
                    return new DefineArgsImpl8<>(addParam(paramClass));
                }
            }

            private class DefineArgsImpl8<P0, P1, P2, P3, P4, P5, P6, P7>
                    extends ImplWithParam<P7, DefineArgs8<T, R, P0, P1, P2, P3, P4, P5, P6, P7>>
                    implements DefineArgs8<T, R, P0, P1, P2, P3, P4, P5, P6, P7>
            {
                private DefineArgsImpl8(ParamDefinition<P7> param)
                {
                    super(param);
                }
            }

            public class ParamDefinition<P> implements ParamInfo<P>
            {
                protected final List<Annotation> annotations = Lists.newArrayList();
                protected final TypeProxy<P> paramType;
                @Nullable
                protected String name;
                protected int modifiers;

                public ParamDefinition(TypeProxy<P> paramType)
                {
                    this.paramType = paramType;
                }

                @Override
                public TypeProxy<P> paramType()
                {
                    return this.paramType;
                }

                @Nullable
                @Override
                public String name()
                {
                    return this.name;
                }
            }
        }

        @Override
        public Constructor<T> getConstructor(Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException
        {
            return (Constructor<T>) getSafeRawType().getConstructor(parameterTypes);
        }
    }

    private static class RuntimeClassLoader extends ClassLoader
    {
        public RuntimeClassLoader(ClassLoader parent)
        {
            super(parent);
        }

        public Class<?> defineClass(String name, byte[] classBytes)
        {
            return this.defineClass(name, classBytes, 0, classBytes.length);
        }
    }
}
