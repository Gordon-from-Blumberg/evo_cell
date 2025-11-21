package com.gordonfromblumberg.games.core.evocell.model;

public record ExpressionDef(
        byte value,
        String name,
        ParameterType parameterType,
        byte[] defaultParameters
) {
    public enum ParameterType {
        number, bool
    }
}
