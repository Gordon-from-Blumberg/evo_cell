package com.gordonfromblumberg.games.core.evocell.model;

public record ExpressionDef(
        byte value,
        String name,
        ParameterType parameterType,
        byte[] defaultValues
) {
    public enum ParameterType {
        number, bool
    }
}
