package com.gordonfromblumberg.games.core.evocell.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

public final class Expressions {
    static final ObjectMap<String, ExpressionMapping> expressionsMap = new ObjectMap<>();
    static final IntMap<ExpressionDef> expressionDefs = new IntMap<>();

    static {
        expressionsMap.put("equals", (p1, p2) -> p1.number() == p2.number() ? 1 : 0);
        expressionsMap.put("not", (p1, p2) -> p1.bool() ? 0 : 1);
        expressionsMap.put("sum", (p1, p2) -> p1.number() + p2.number());

        loadExpressionDefs();
    }

    private Expressions() {
        throw new UnsupportedOperationException("Expressions should not be instantiated");
    }

    private static void loadExpressionDefs() {
        final JsonReader jsonReader = new JsonReader();
        final JsonValue array = jsonReader.parse(Gdx.files.internal("model/expressions.json"));
        for (JsonValue exprDesc = array.child; exprDesc != null; exprDesc = exprDesc.next) {
            byte code = exprDesc.getByte("value");
            ExpressionDef expressionDef = new ExpressionDef(
                    code,
                    exprDesc.getString("name"),
                    ExpressionDef.ParameterType.valueOf(exprDesc.getString("parameterType")),
                    exprDesc.get("defaultParameters").asByteArray());
            ExpressionDef existing = expressionDefs.put(code, expressionDef);
            if (existing != null) {
                throw new IllegalStateException("Duplicated action code " + code);
            }
        }
    }
}


