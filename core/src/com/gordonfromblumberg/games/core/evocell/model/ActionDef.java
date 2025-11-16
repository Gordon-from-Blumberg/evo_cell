package com.gordonfromblumberg.games.core.evocell.model;

import com.gordonfromblumberg.games.core.common.utils.RandomGen;

public record ActionDef(
        Type type,
        byte value,
        String name,
        String description,
        ActionParameterDef[] parameters
) {

    public enum Type {
        action, spec
    }

    record ActionParameterDef(
            String name,
            DefaultParameterValue defaultValue
    ) { }

    interface DefaultParameterValue {
        byte get();
    }

    record ConstantParameterValue(byte value) implements DefaultParameterValue {
        @Override
        public byte get() {
            return value;
        }
    }

    record RandomParameterValue(byte min,
                                byte max) implements DefaultParameterValue {
        @Override
        public byte get() {
            return RandomGen.INSTANCE.nextByte(min, max);
        }
    }
}
