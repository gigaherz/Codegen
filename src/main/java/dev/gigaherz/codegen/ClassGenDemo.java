package dev.gigaherz.codegen;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClassGenDemo
{
    public static class Test implements Vector3I
    {
        protected final int x;
        protected final int y;
        protected final int z;

        public Test(int x, int y, int z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public int getX()
        {
            return x;
        }

        public int getY()
        {
            return getX();
        }

        public int getZ()
        {
            return z;
        }

        public int maxCoord()
        {
            if (x > y && x > z)
            {
                return x;
            }
            else if (y > z)
            {
                return y;
            }
            else
            {
                return z;
            }
        }
    }

    public static class Test2 extends Test
    {
        public int x;

        public Test2(int x, int y, int z)
        {
            super(x, y, z);
            this.x = x;
        }

        public int getX()
        {
            return x;
        }

        public int getSuperX()
        {
            return super.x;
        }
    }

    public interface Vector3I
    {
        int getX();
        int getY();
        int getZ();
    }

    public static void main(String[] args)
    {
        var builder = new ClassMaker(Thread.currentThread().getContextClassLoader()).begin()
                .setPublic().setFinal()
                .implementing(Vector3I.class)
                .field("x", int.class).setPrivate().setFinal()
                .field("y", int.class).setPrivate().setFinal()
                .field("z", int.class).setPrivate().setFinal()
                .constructor().setPublic().setInstance()
                .param(int.class).withName("x")
                .param(int.class).withName("y")
                .param(int.class).withName("z")
                .implementation(cb -> cb
                        .superCall()
                        .assign(cb.fieldRef("x"), cb.localVar("x"))
                        .assign(cb.fieldRef("y"), cb.localVar("y"))
                        .assign(cb.fieldRef("z"), cb.localVar("z"))
                        .returnVoid())
                .method("getX", int.class)
                .setPublic().setInstance().implementation(cb -> cb.returnVal(cb.field("x")))
                .method("getY", int.class)
                .setPublic().setInstance().implementation(cb -> cb.returnVal(cb.thisVar().methodCall("getX")))
                .method("getZ", int.class)
                .setPublic().setInstance().implementation(cb -> cb.returnVal(cb.thisVar().field("z")))
                .method("maxCoord", int.class)
                .setPublic().setInstance().implementation(cb -> cb
                        .ifElse(
                                cb.and(cb.gt(cb.field("x"), cb.field("y")), cb.gt(cb.field("x"), cb.field("z"))),
                                ct -> ct.returnVal(ct.field("x")),
                                cf -> cf.returnVal(cf.iif(cf.gt(cf.field("y"), cf.field("z")), cf.field("y"), cf.field("z")))
                        ));

        try
        {
            var filename = "F:/" + builder.finish().getName() + ".class";
            System.out.println("Saving class to file " + filename);
            Files.write(Path.of(filename), builder.makeClass());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        var ci = builder.make();
        try
        {
            var cn = ci.thisType().getConstructor(int.class, int.class, int.class);
            cn.setAccessible(true);
            var instance = cn.newInstance(1, 2, 3);
            var x = instance.getX();
            System.out.println("getX() returned " + x);
        }
        catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }
}
