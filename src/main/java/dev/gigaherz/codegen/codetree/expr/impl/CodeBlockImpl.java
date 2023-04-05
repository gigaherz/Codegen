package dev.gigaherz.codegen.codetree.expr.impl;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import dev.gigaherz.codegen.api.FieldToken;
import dev.gigaherz.codegen.api.VarToken;
import dev.gigaherz.codegen.api.codetree.info.FieldInfo;
import dev.gigaherz.codegen.api.codetree.info.MethodInfo;
import dev.gigaherz.codegen.api.codetree.info.ParamInfo;
import dev.gigaherz.codegen.codetree.MethodLookup;
import dev.gigaherz.codegen.codetree.expr.*;
import dev.gigaherz.codegen.codetree.impl.*;
import dev.gigaherz.codegen.type.TypeProxy;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class CodeBlockImpl<B, P, M> implements CodeBlockInternal<B, M>
{
    @Nullable
    private final CodeBlock<P, M> parentBlock;
    private TypeToken<?> returnType;
    private final MethodImplementation<M> owner;
    private final List<InstructionSource> instructions = Lists.newArrayList();
    private final Map<String, LocalVariable<?>> locals = new HashMap<>();

    public CodeBlockImpl(MethodImplementation<M> owner, @Nullable CodeBlock<P, M> parentBlock)
    {
        this.owner = owner;
        this.parentBlock = parentBlock;
    }

    public CodeBlockImpl(MethodImplementation<M> owner, @Nullable CodeBlock<P, M> parentBlock, TypeToken<B> returnType)
    {
        this(owner, parentBlock);
        this.returnType = returnType;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public TypeToken<B> returnType()
    {
        return (TypeToken) returnType;
    }

    public boolean compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, @Nullable Label jumpEnd)
    {
        int last = instructions.size() - 1;
        for (int i = 0; i <= last; i++)
        {
            InstructionSource insn = instructions.get(i);
            if (insn.compile(defineConstant, mv, i == last ? jumpEnd : null, false))
                return false;
        }
        return true;
    }

    public void compile(ToIntFunction<Object> defineConstant, MethodVisitor mv, boolean needsResult)
    {
        var jumpEnd = new Label();
        int last = instructions.size() - 1;
        for (int i = 0; i <= last; i++)
        {
            InstructionSource insn = instructions.get(i);
            if (insn.compile(defineConstant, mv, i == last ? jumpEnd : null, needsResult))
                break;
        }
        mv.visitLabel(jumpEnd);
    }

    public boolean isEmpty()
    {
        return instructions.size() == 0;
    }

    private Stack<Integer> beforeStack = new Stack<>();
    @Override
    public void beforeExpressionCompile()
    {
        beforeStack.push(owner.peekStackDepth());
    }

    @Override
    public void afterExpressionCompile(boolean needsResult)
    {
        var expectedDiff = (needsResult ? 1 : 0);
        var actualDiff = (owner.peekStackDepth() - beforeStack.pop());
        if (actualDiff != expectedDiff)
            throw new IllegalStateException("Stack at the end of an expression must be "+expectedDiff+" more than it was at the start, but it was " + actualDiff);
    }

    @Override
    public List<InstructionSource> instructions()
    {
        return instructions;
    }

    @Override
    public CodeBlock<B, M> local(String name, TypeToken<?> varType)
    {
        if (locals.containsKey(name))
            throw new IllegalStateException("A local with name '" + name + "' has already been declared.");
        var index = owner.defineLocal(name, TypeProxy.of(varType));
        locals.put(name, owner.getLocalVariable(index));
        return this;
    }

    @Override
    public CodeBlock<B, M> local(String name, TypeToken<?> varType, ValueExpression<?, B> initializer)
    {
        if (locals.containsKey(name))
            throw new IllegalStateException("A local with name '" + name + "' has already been declared.");
        var index = owner.defineLocal(name, TypeProxy.of(varType));
        var local = owner.getLocalVariable(index);
        locals.put(name, local);
        assign(new VarRef<>(this, local), initializer);
        return this;
    }

    public void pushStack(TypeToken<?> type)
    {
        owner.pushStack(type);
    }

    public void pushStack(TypeProxy<?> type)
    {
        owner.pushStack(type);
    }

    public void pushStack(int slots)
    {
        owner.pushStack(slots);
    }

    public void popStack()
    {
        owner.popStack();
    }

    public CodeBlockInternal<B, M> getThis()
    {
        instructions.add(new LocalLoad(owner, 0));
        return this;
    }

    public CodeBlockInternal<B, M> getLocal(String localName)
    {
        instructions.add(new LocalLoad(owner, localName));
        return this;
    }

    public CodeBlockInternal<B, M> setLocal(String localName)
    {
        instructions.add(new LocalStore(owner, localName));
        return this;
    }

    public CodeBlockInternal<B, M> getField(String fieldName)
    {
        instructions.add(new FieldLoad(owner, null, fieldName));
        return this;
    }

    public CodeBlockInternal<B, M> setField(String fieldName)
    {
        instructions.add(new FieldStore(owner, null, fieldName));
        return this;
    }

    @Override
    public void returnVoid()
    {
        instructions.add(new Return(this, TypeToken.of(void.class)));
    }

    public void returnInt()
    {
        instructions.add(new Return(this, TypeToken.of(int.class)));
    }

    public void returnType(TypeToken<?> type)
    {
        instructions.add(new Return(this, type));
    }

    @Override
    public <T> void returnVal(ValueExpression<T, M> value)
    {
        ValueExpression<?, M> nValue = owner.applyAutomaticCasting(owner.methodInfo().returnType(), value);
        if (owner.methodInfo().returnType().isSupertypeOf(value.effectiveType()))
        {
            instructions.add(new ExprReturn(this, owner.methodInfo().returnType(), nValue));
        }
    }

    @Override
    public CodeBlock<B, M> breakLoop()
    {
        instructions.add(new SkipLoop(true));
        return this;
    }

    @Override
    public CodeBlock<B, M> continueLoop()
    {
        instructions.add(new SkipLoop(false));
        return this;
    }

    @Override
    public <T> void breakVal(ValueExpression<?, M> value)
    {
        ValueExpression<?, M> nValue = value;
        if (returnType == null)
        {
            returnType = nValue.effectiveType();
        }
        else
        {
            nValue = owner.applyAutomaticCasting(returnType, nValue);
        }
        if (returnType.isSupertypeOf(value.effectiveType()))
        {
            instructions.add(new ExprBreak(this, nValue));
        }
    }

    @Override
    public <T, S> ValueExpression<T, B> set(LRef<T> target, ValueExpression<S, B> value)
    {
        var value1 = owner.applyAutomaticCasting(target.targetType(), value);
        if (target.targetType().isSupertypeOf(value1.effectiveType()))
        {
            return new AssignExpression<>(this, target, value1);
        }
        throw new IllegalStateException("Cannot assign field of type " + target.targetType() + " from expression of type " + value.effectiveType());
    }

    @Override
    public CodeBlock<B, M> compute(ValueExpression<?, B> value)
    {
        instructions.add(new Do(owner, value));
        return this;
    }

    @Override
    public LRef<?> localRef(String localName)
    {
        var local = locals.get(localName);
        if (local == null)
        {
            if (parentBlock != null)
            {
                return parentBlock.localRef(localName);
            }

            throw new IllegalStateException("Undefined local " + localName);
        }
        return new VarRef<>(this, local);
    }

    @Override
    public LRef<?> fieldRef(String fieldName)
    {
        return new FieldRef<>(this, thisVar(), owner.methodInfo().owner().getField(fieldName));
    }

    @Override
    public LRef<?> fieldRef(ValueExpression<?, B> objRef, String fieldName)
    {
        return new FieldRef<>(this, objRef, owner.methodInfo().owner().getField(fieldName));
    }

    @Override
    public LRef<?> fieldRef(ValueExpression<?, B> objRef, FieldInfo<?> fieldInfo)
    {
        return new FieldRef<>(this, objRef, fieldInfo);
    }

    @Override
    public <T> ValueExpression<T, B> field(String fieldName)
    {
        return field(thisVar(), owner.methodInfo().owner().getField(fieldName));
    }

    @Override
    public <T> ValueExpression<T, B> field(FieldToken<T> fieldToken)
    {
        throw new IllegalStateException("TODO - Not implemented");
    }

    @Override
    public <T> ValueExpression<T, B> field(ValueExpression<?, B> objRef, FieldInfo<?> field)
    {
        return new FieldExpression<>(this, objRef, field);
    }

    @Override
    public ValueExpression<?, B> fieldOf(ValueExpression<?, B> objRef, String fieldName)
    {
        return new FieldExpression<>(this, objRef, owner.methodInfo().owner().getField(fieldName));
    }

    @Override
    public ValueExpression<?, B> staticField(TypeToken<?> type, String fieldName)
    {
        throw new IllegalStateException("TODO - Not implemented");
    }

    @Override
    public ValueExpression<?, B> thisVar()
    {
        return new VarExpression<>(this, owner.getLocalVariable(0));
    }

    @Override
    public ValueExpression<?, B> superVar()
    {
        return new NoopConversion<>(this, owner.methodInfo().owner().superClass(), new VarExpression<>(this, owner.getLocalVariable(0)));
    }

    @Override
    public <T> ValueExpression<T, B> localVar(VarToken<T> varToken)
    {
        throw new IllegalStateException("TODO - Not implemented");
    }

    @Override
    public VarExpression<?, B> localVar(String varName)
    {
        return new VarExpression<>(this, owner.getLocalVariable(varName));
    }

    @Override
    public CodeBlock<B, M> exec(ValueExpression<?, B> value)
    {
        instructions.add(new ExecuteExpression(owner, value));
        return this;
    }

    @Override
    public CodeBlock<B, M> autoSuperCall()
    {
        List<ValueExpression<?, B>> locals = owner().methodInfo().params().stream()
                .map(p -> localVar(p.name()))
                .collect(Collectors.toList()); // doesn't work if you replace collect with toList()
        return superCall(locals);
    }

    @Override
    public CodeBlock<B, M> superCall(List<ValueExpression<?, B>> values)
    {
        return superCall(ml -> ml, values);
    }

    @Override
    public final CodeBlock<B, M> superCall(Function<MethodLookup<?>, MethodLookup<?>> methodLookup, List<ValueExpression<?, B>> values)
    {
        var ml = new MethodLookup<>(owner.methodInfo().owner().superClass(), "<init>");
        ml = methodLookup.apply(ml);
        return superCall(ml.result(), values);
    }

    @Override
    public final CodeBlock<B, M> superCall(MethodInfo<?> method, List<ValueExpression<?, B>> values)
    {
        if (!method.owner().thisType().actualType().equals(owner.methodInfo().owner().superClass()))
            throw new IllegalStateException("Super call must be a method or constructor of the immediate super class of this class.");
        instructions.add(new SuperCall(owner, methodCall(superVar(), method, values)));
        return this;
    }

    @Override
    public final <R, T> ValueExpression<R, B> methodCall(ValueExpression<T, B> objRef, String methodName, Function<MethodLookup<T>, MethodLookup<T>> methodLookup, List<ValueExpression<?, B>> values)
    {
        var ml = new MethodLookup<>(objRef.effectiveType(), methodName);
        ml = methodLookup.apply(ml);
        return methodCall(objRef, ml.result(), values);
    }

    @Override
    public <R> ValueExpression<R, B> staticCall(TypeToken<?> classToken, String methodName, List<ValueExpression<?, B>> values)
    {
        throw new IllegalStateException("TODO - Not implemented");
    }

    @Override
    public final <R, T> ValueExpression<R, B> thisCall(String methodName, List<ValueExpression<?, B>> values)
    {
        var ml = new MethodLookup<>(owner().methodInfo().owner(), methodName);
        for (var expr : values)
        {ml = ml.withParam(expr.effectiveType());}
        return methodCall(thisVar(), ml.result(), values);
    }

    @Override
    public final <R, T> ValueExpression<R, B> methodCall(ValueExpression<T, B> objRef, String methodName, List<ValueExpression<?, B>> values)
    {
        var ml = new MethodLookup<>(objRef.effectiveType(), methodName);
        for (var expr : values)
        {ml = ml.withParam(expr.effectiveType());}
        return methodCall(objRef, ml.result(), values);
    }

    @Override
    public <R, T> ValueExpression<R, B> staticCall(TypeToken<T> classToken, String methodName, Function<MethodLookup<T>, MethodLookup<T>> methodLookup, List<ValueExpression<?, B>> values)
    {
        var ml = new MethodLookup<>(classToken, methodName);
        ml = methodLookup.apply(ml);
        return methodCall(null, ml.result(), values);
    }

    @Override
    public final <R> ValueExpression<R, B> methodCall(@Nullable ValueExpression<?, B> objRef, MethodInfo<R> method, List<ValueExpression<?, B>> values)
    {
        List<? extends ParamInfo<?>> params = method.params();
        var lValues = new ArrayList<>(values);
        if (params.size() != lValues.size())
            throw new IllegalStateException("Mismatched set of values. Expected: " + params.stream().map(ParamInfo::paramType).toList()
                    + "; Received: " + lValues.stream().map(ValueExpression::effectiveType).toList());
        for (int i = 0; i < params.size(); i++)
        {
            var param = params.get(i);
            var val = lValues.get(i);
            var lVal = owner.applyAutomaticCasting(param.paramType().actualType(), val);
            if (!param.paramType().actualType().isSupertypeOf(lVal.effectiveType()))
                throw new IllegalStateException("Param " + i + " cannot be converted from " + lVal.effectiveType() + " to " + param.paramType().actualType());
            if (lVal != val)
                lValues.set(i, lVal);
        }
        return new MethodCallExpression<>(this, objRef, method, lValues);
    }

    @Override
    public MethodLookup<?> method(String name)
    {
        return new MethodLookup<>(owner.methodInfo().owner(), name);
    }

    @Override
    public BooleanExpression<B> gt(ValueExpression<?, B> x, ValueExpression<?, B> y)
    {
        if (!x.effectiveType().equals(y.effectiveType()))
        {
            y = owner.applyAutomaticCasting(x.effectiveType(), y);
            if (!x.effectiveType().equals(y.effectiveType()))
            {
                x = owner.applyAutomaticCasting(y.effectiveType(), x);
                if (!x.effectiveType().equals(y.effectiveType()))
                    throw new IllegalStateException("Cannot compare " + x.effectiveType() + " to " + y.effectiveType());
            }
        }

        return new LogicExpression<>(this, ComparisonType.GT, x, y);
    }

    @Override
    public BooleanExpression<B> ge(ValueExpression<?, B> x, ValueExpression<?, B> y)
    {
        if (!x.effectiveType().equals(y.effectiveType()))
        {
            y = owner.applyAutomaticCasting(x.effectiveType(), y);
            if (!x.effectiveType().equals(y.effectiveType()))
            {
                x = owner.applyAutomaticCasting(y.effectiveType(), x);
                if (!x.effectiveType().equals(y.effectiveType()))
                    throw new IllegalStateException("Cannot compare " + x.effectiveType() + " to " + y.effectiveType());
            }
        }

        return new LogicExpression<>(this, ComparisonType.GE, x, y);
    }

    @Override
    public BooleanExpression<B> lt(ValueExpression<?, B> x, ValueExpression<?, B> y)
    {
        if (!x.effectiveType().equals(y.effectiveType()))
        {
            y = owner.applyAutomaticCasting(x.effectiveType(), y);
            if (!x.effectiveType().equals(y.effectiveType()))
            {
                x = owner.applyAutomaticCasting(y.effectiveType(), x);
                if (!x.effectiveType().equals(y.effectiveType()))
                    throw new IllegalStateException("Cannot compare " + x.effectiveType() + " to " + y.effectiveType());
            }
        }

        return new LogicExpression<>(this, ComparisonType.LT, x, y);
    }

    @Override
    public BooleanExpression<B> le(ValueExpression<?, B> x, ValueExpression<?, B> y)
    {
        if (!x.effectiveType().equals(y.effectiveType()))
        {
            y = owner.applyAutomaticCasting(x.effectiveType(), y);
            if (!x.effectiveType().equals(y.effectiveType()))
            {
                x = owner.applyAutomaticCasting(y.effectiveType(), x);
                if (!x.effectiveType().equals(y.effectiveType()))
                    throw new IllegalStateException("Cannot compare " + x.effectiveType() + " to " + y.effectiveType());
            }
        }

        return new LogicExpression<>(this, ComparisonType.LE, x, y);
    }

    @Override
    public BooleanExpression<B> eq(ValueExpression<?, B> x, ValueExpression<?, B> y)
    {
        if (!x.effectiveType().equals(y.effectiveType()))
        {
            y = owner.applyAutomaticCasting(x.effectiveType(), y);
            if (!x.effectiveType().equals(y.effectiveType()))
            {
                x = owner.applyAutomaticCasting(y.effectiveType(), x);
                if (!x.effectiveType().equals(y.effectiveType()))
                    throw new IllegalStateException("Cannot compare " + x.effectiveType() + " to " + y.effectiveType());
            }
        }

        return new LogicExpression<>(this, ComparisonType.EQ, x, y);
    }

    @Override
    public BooleanExpression<B> ne(ValueExpression<?, B> x, ValueExpression<?, B> y)
    {
        if (!x.effectiveType().equals(y.effectiveType()))
        {
            y = owner.applyAutomaticCasting(x.effectiveType(), y);
            if (!x.effectiveType().equals(y.effectiveType()))
            {
                x = owner.applyAutomaticCasting(y.effectiveType(), x);
                if (!x.effectiveType().equals(y.effectiveType()))
                    throw new IllegalStateException("Cannot compare " + x.effectiveType() + " to " + y.effectiveType());
            }
        }

        return new LogicExpression<>(this, ComparisonType.NE, x, y);
    }

    @Override
    public BooleanExpression<B> and(ValueExpression<?, B> a, ValueExpression<?, B> b)
    {
        if (!BooleanExpressionImpl.BOOLEAN_TYPE_TOKEN.equals(a.effectiveType())
                || !BooleanExpressionImpl.BOOLEAN_TYPE_TOKEN.equals(b.effectiveType()))
            throw new IllegalStateException("Operator AND requires two boolean parameters, found " + a.effectiveType() + " and " + b.effectiveType());

        return new LogicExpression<>(this, ComparisonType.AND, a, b);
    }

    @Override
    public BooleanExpression<B> or(ValueExpression<?, B> a, ValueExpression<?, B> b)
    {
        if (!BooleanExpressionImpl.BOOLEAN_TYPE_TOKEN.equals(a.effectiveType())
                || !BooleanExpressionImpl.BOOLEAN_TYPE_TOKEN.equals(b.effectiveType()))
            throw new IllegalStateException("Operator OR requires two boolean parameters, found " + a.effectiveType() + " and " + b.effectiveType());

        return new LogicExpression<>(this, ComparisonType.OR, a, b);
    }

    @Override
    public BooleanExpression<B> not(ValueExpression<?, B> a)
    {
        if (!BooleanExpressionImpl.BOOLEAN_TYPE_TOKEN.equals(a.effectiveType()))
            throw new IllegalStateException("Operator NOT requires a boolean parameters, found " + a.effectiveType());

        return new NotExpression<>(this, a);
    }

    @Override
    public <C> ValueExpression<C, B> iif(BooleanExpression<B> condition, ValueExpression<C, B> trueBranch, ValueExpression<C, B> falseBranch)
    {
        return new ConditionalExpression<>(this, condition, trueBranch, falseBranch);
    }

    @Override
    public <T> ValueExpression<T, B> iif(BooleanExpression<B> condition, Consumer<CodeBlock<T, M>> trueBranch, Consumer<CodeBlock<T, M>> falseBranch)
    {
        var tb = this.<T>childBlock();
        var fb = this.<T>childBlock();

        trueBranch.accept(tb);
        falseBranch.accept(fb);

        return new ConditionalExpression<>(this, condition, new CodeBlockExpression<>(this, tb), new CodeBlockExpression<>(this, fb));
    }

    public ValueExpression<?, B> add(ValueExpression<?, B> a, ValueExpression<?, B> b)
    {
        return binaryArithmeticOperator(a, b, Opcodes.IADD, Opcodes.LADD, Opcodes.FADD, Opcodes.DADD, "+");
    }
    public ValueExpression<?, B> sub(ValueExpression<?, B> a, ValueExpression<?, B> b)
    {
        return binaryArithmeticOperator(a, b, Opcodes.ISUB, Opcodes.LSUB, Opcodes.FSUB, Opcodes.DSUB, "-");
    }
    public ValueExpression<?, B> mul(ValueExpression<?, B> a, ValueExpression<?, B> b)
    {
        return binaryArithmeticOperator(a, b, Opcodes.IMUL, Opcodes.LMUL, Opcodes.FMUL, Opcodes.DMUL, "*");
    }
    public ValueExpression<?, B> div(ValueExpression<?, B> a, ValueExpression<?, B> b)
    {
        return binaryArithmeticOperator(a, b, Opcodes.IDIV, Opcodes.LDIV, Opcodes.FDIV, Opcodes.DDIV, "/");
    }
    public ValueExpression<?, B> mod(ValueExpression<?, B> a, ValueExpression<?, B> b)
    {
        return binaryArithmeticOperator(a, b, Opcodes.IREM, Opcodes.LREM, Opcodes.FREM, Opcodes.DREM, "%");
    }
    public ValueExpression<?, B> bitAnd(ValueExpression<?, B> a, ValueExpression<?, B> b)
    {
        return binaryBitwiseOperator(a, b, Opcodes.IAND, Opcodes.LAND, "&");
    }
    public ValueExpression<?, B> bitOr(ValueExpression<?, B> a, ValueExpression<?, B> b)
    {
        return binaryBitwiseOperator(a, b, Opcodes.IOR, Opcodes.LOR, "|");
    }
    public ValueExpression<?, B> bitXor(ValueExpression<?, B> a, ValueExpression<?, B> b)
    {
        return binaryBitwiseOperator(a, b, Opcodes.IXOR, Opcodes.LXOR, "^");
    }
    public ValueExpression<?, B> shiftLeft(ValueExpression<?, B> a, ValueExpression<?, B> b)
    {
        return shiftOperator(a, b, Opcodes.ISHL, Opcodes.LSHL, "<<");
    }
    public ValueExpression<?, B> shiftRight(ValueExpression<?, B> a, ValueExpression<?, B> b)
    {
        return shiftOperator(a, b, Opcodes.ISHR, Opcodes.LSHR, "&");
    }
    public ValueExpression<?, B> shiftRightUnsigned(ValueExpression<?, B> a, ValueExpression<?, B> b)
    {
        return shiftOperator(a, b, Opcodes.IUSHR, Opcodes.LUSHR, "&");
    }
    public ValueExpression<?, B> index(ValueExpression<?, B> array, ValueExpression<?, B> index)
    {
        throw new IllegalStateException("TODO -- NOT IMPLEMENTED!");
    }
    public ValueExpression<?, B> neg(ValueExpression<?, B> a)
    {
        return unaryArithmeticOperator(a, Opcodes.INEG, Opcodes.LNEG, Opcodes.FNEG, Opcodes.DNEG, "&");
    }
    /** @noinspection unchecked, rawtypes */
    public ValueExpression<?, B> bitNot(ValueExpression<?, B> a)
    {
        if (a.effectiveType().getRawType() == int.class)
            return new BinaryOperator(this, Opcodes.IXOR, a, literal(-1), a.effectiveType());
        if (a.effectiveType().getRawType() == long.class)
            return new BinaryOperator(this, Opcodes.LXOR, a, literal(-1L), a.effectiveType());

        throw new IllegalStateException("Cannot apply '~' operator to value of type " + a.effectiveType());
    }

    /** @noinspection unchecked, rawtypes */
    private ValueExpression<?, B> binaryArithmeticOperator(ValueExpression<?, B> a, ValueExpression<?, B> b, int intOp, int longOp, int floatOp, int doubleOp, String opname)
    {
        TypeToken<?> resultType = owner.computeArithmeticResultType(a.effectiveType(), b.effectiveType());
        if (resultType == null)
            throw new IllegalStateException("Cannot find result type for an arithmetic operation between " + a.effectiveType() + " and " + b.effectiveType());
        var a1 = owner.applyAutomaticCasting(resultType, a);
        var b1 = owner.applyAutomaticCasting(resultType, b);
        if (resultType.getRawType() == int.class)
            return new BinaryOperator(this, intOp, a1, b1, resultType);
        if (resultType.getRawType() == long.class)
            return new BinaryOperator(this, longOp, a1, b1, resultType);
        if (resultType.getRawType() == float.class)
            return new BinaryOperator(this, floatOp, a1, b1, resultType);
        if (resultType.getRawType() == double.class)
            return new BinaryOperator(this, doubleOp, a1, b1, resultType);

        throw new IllegalStateException("Cannot apply '"+opname+"' operator between values of types " + a.effectiveType() + " and " + b.effectiveType());
    }

    /** @noinspection unchecked, rawtypes */
    private ValueExpression<?, B> binaryBitwiseOperator(ValueExpression<?, B> a, ValueExpression<?, B> b, int intOp, int longOp, String opname)
    {
        TypeToken<?> resultType = owner.computeArithmeticResultType(a.effectiveType(), b.effectiveType());
        if (resultType == null)
            throw new IllegalStateException("Cannot find result type for an arithmetic operation between " + a.effectiveType() + " and " + b.effectiveType());
        var a1 = owner.applyAutomaticCasting(resultType, a);
        var b1 = owner.applyAutomaticCasting(resultType, b);
        if (resultType.getRawType() == int.class)
            return new BinaryOperator(this, intOp, a1, b1, resultType);
        if (resultType.getRawType() == long.class)
            return new BinaryOperator(this, longOp, a1, b1, resultType);

        throw new IllegalStateException("Cannot apply '"+opname+"' operator between values of types " + a.effectiveType() + " and " + b.effectiveType());
    }

    /** @noinspection unchecked, rawtypes */
    private ValueExpression<?, B> shiftOperator(ValueExpression<?, B> a, ValueExpression<?, B> b, int intOp, int longOp, String opname)
    {
        if (a.effectiveType().getRawType() == int.class)
            return new BinaryOperator(this, intOp, a, b, a.effectiveType());
        if (a.effectiveType().getRawType() == long.class)
            return new BinaryOperator(this, longOp, a, b, a.effectiveType());

        throw new IllegalStateException("Cannot apply '"+opname+"' operator between values of types " + a.effectiveType() + " and " + b.effectiveType());
    }

    /** @noinspection SameParameterValue */
    private ValueExpression<?, B> unaryArithmeticOperator(ValueExpression<?, B> a, int intOp, int longOp, int floatOp, int doubleOp, String opname)
    {
        if (a.effectiveType().getRawType() == int.class)
            return new UnaryOperator<>(this, intOp, a);
        if (a.effectiveType().getRawType() == long.class)
            return new UnaryOperator<>(this, longOp, a);
        if (a.effectiveType().getRawType() == float.class)
            return new UnaryOperator<>(this, floatOp, a);
        if (a.effectiveType().getRawType() == double.class)
            return new UnaryOperator<>(this, doubleOp, a);

        throw new IllegalStateException("Cannot apply '"+opname+"' operator to value of type " + a.effectiveType());
    }

    public BooleanExpression<B> literal(boolean val) { return new Literal.Bool<>(this, val); }
    public ValueExpression<Byte, B> literal(byte val) { return new Literal.Number<>(this, val, TypeToken.of(byte.class)); }
    public ValueExpression<Short, B> literal(short val) { return new Literal.Number<>(this, val, TypeToken.of(short.class)); }
    public ValueExpression<Integer, B> literal(int val) { return new Literal.Number<>(this, val, TypeToken.of(int.class)); }
    public ValueExpression<Long, B> literal(long val) { return new Literal.Number<>(this, val, TypeToken.of(long.class)); }
    public ValueExpression<Float, B> literal(float val) { return new Literal.Number<>(this, val, TypeToken.of(float.class)); }
    public ValueExpression<Double, B> literal(double val) { return new Literal.Number<>(this, val, TypeToken.of(double.class)); }
    public ValueExpression<String, B> literal(String val) { return new Literal.String<>(this, val); }

    public <X> CodeBlockInternal<X, M> childBlock()
    {
        return new CodeBlockImpl<>(owner, this);
    }

    @Override
    public CodeBlock<B, M> ifElse(BooleanExpression<?> condition, Consumer<CodeBlock<B, M>> trueBranch, Consumer<CodeBlock<B, M>> falseBranch)
    {
        instructions.add(new IfBlock<>(this, condition, trueBranch, falseBranch));
        return this;
    }

    @Override
    public CodeBlock<B, M> forLoop(String localName, TypeToken<?> varType, BooleanExpression<?> condition, ValueExpression<?, B> step, Consumer<CodeBlock<B, M>> body)
    {
        throw new IllegalStateException("TODO - Not implemented");
    }

    @Override
    public <V, S extends V> CodeBlock<B, M> forEach(String localName, TypeToken<V> varType, ValueExpression<S, B> collection, Consumer<CodeBlock<B, M>> body)
    {
        throw new IllegalStateException("TODO - Not implemented");
    }

    @Override
    public <V, S extends V> CodeBlock<B, M> whileLoop(BooleanExpression<?> condition, Consumer<CodeBlock<B, M>> body)
    {
        throw new IllegalStateException("TODO - Not implemented");
    }

    @Override
    public <V, S extends V> CodeBlock<B, M> doWhile(Consumer<CodeBlock<B, M>> body, BooleanExpression<?> condition)
    {
        throw new IllegalStateException("TODO - Not implemented");
    }

    public MethodImplementation<M> owner()
    {
        return owner;
    }
}

