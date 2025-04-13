package dev.gigaherz.codegen.tests;

public class Test implements Vector3I
{
    private final int x;
    private final int y;
    private final int z;

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

    public int f(int a, int b)
    {
        return a + b;
    }

    public boolean test()
    {
        int a = x;
        int b = f(a, a = y);
        return (a = z) > b;
    }

    private float q;
    public Test test2()
    {
        var t = this;
        for(int i=0;i<10;i++)
        {
            var x = y+z;
            t = new Test(x,y,z);
        }

        q++;

        return t;
    }
}
