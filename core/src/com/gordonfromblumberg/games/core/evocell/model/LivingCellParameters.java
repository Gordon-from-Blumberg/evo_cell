package com.gordonfromblumberg.games.core.evocell.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class LivingCellParameters {
    private static final Array<ParameterType> parameterTypes = new Array<>();

    private final Array<Parameter> parameters = new Array<>();

    static {
        loadParameters();
    }

    LivingCellParameters() {
        for (ParameterType type : parameterTypes) {
            parameters.add(new Parameter(type));
        }
    }

    int get(ParameterName parameterName) {
        return parameters.get(parameterName.ordinal()).value;
    }

    void set(ParameterName parameterName, int value) {
        parameters.get(parameterName.ordinal()).value = value;
    }

    int get(int index) {
        return parameters.get(index).value;
    }

    int energyConsumption() {
        float sum = 0;
        for (Parameter p : parameters) {
            sum += p.energyConsumption();
        }
        return (int) sum;
    }

    public enum ParameterName {
        chlorophyll,
        moving
    }

    static class Parameter {
        private ParameterType type;
        private int value;

        Parameter(ParameterType type) {
            this.type = type;
        }

        float energyConsumption() {
            return type.energyConsumption(value);
        }
    }

    static record ParameterType(ParameterName name,
                                float baseCost,
                                float costStep,
                                float energyConsumption
    ) {
        int increaseCost(int value) {
            return (int) (baseCost + value * costStep);
        }

        float energyConsumption(int value) {
            return value * energyConsumption;
        }
    }

    private static void loadParameters() {
        final JsonReader jsonReader = new JsonReader();
        final JsonValue array = jsonReader.parse(Gdx.files.internal("model/livingCellParameters.json"));
        JsonValue parameterDesc = array.child;
        final ParameterName[] names = ParameterName.values();
        for (int i = 0, n = names.length; i < n; ++i, parameterDesc = parameterDesc.next) {
            ParameterName nameEnum = names[i];
            String name = parameterDesc.getString("name");
            if (!nameEnum.name().equals(name)) {
                throw new IllegalStateException("In livingCellParameter#" + i + " expected " + nameEnum + ", " +
                                                        "but is " + name);
            }
            parameterTypes.add(new ParameterType(nameEnum,
                                                 parameterDesc.getFloat("baseCost"),
                                                 parameterDesc.getFloat("costStep"),
                                                 parameterDesc.getFloat("energyConsumption")));
        }

        if (parameterDesc != null) {
            throw new IllegalStateException("More than " + names.length + " parameters found in livingCellParameters.json");
        }
    }
}
