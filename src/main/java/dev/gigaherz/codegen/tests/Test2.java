package dev.gigaherz.codegen.tests;

public class Test2 extends Test
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
        return super.getX();
    }

    public int fib(int val)
    {
        return switch (val)
        {
            case 0, 1 -> 1;
            default -> fib(val - 1) + fib(val - 2);
        };
    }
}
