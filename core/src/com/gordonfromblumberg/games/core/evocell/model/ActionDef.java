package com.gordonfromblumberg.games.core.evocell.model;

public class ActionDef {
    private Type type;
    private byte value;
    private String name;
    private String description;

    public Type getType() {
        return type;
    }

    public byte getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public enum Type {
        action, spec
    }
}
