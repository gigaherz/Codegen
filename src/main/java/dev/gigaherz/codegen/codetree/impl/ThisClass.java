package dev.gigaherz.codegen.codetree.impl;

public class ThisClass
{
    private static final ThisClass INSTANCE = new ThisClass();

    private ThisClass() {}

    public static ThisClass instance()
    {
        return INSTANCE;
    }
}
