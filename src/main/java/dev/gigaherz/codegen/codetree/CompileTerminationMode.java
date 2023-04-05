package dev.gigaherz.codegen.codetree;

public enum CompileTerminationMode
{
    NORMAL(false),
    BREAK(true);

    private final boolean isBreak;

    CompileTerminationMode(boolean isBreak)
    {
        this.isBreak = isBreak;
    }

    public boolean isBreak()
    {
        return isBreak;
    }
}
