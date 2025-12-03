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
        expressionsMap.put("equals", (g, b, p1, p2) -> p1.number(g, b) == p2.number(g, b) ? 1 : 0);
        expressionsMap.put("not", (g, b, p1, p2) -> p1.bool(g, b) ? 0 : 1);
        expressionsMap.put("gt", (g, b, p1, p2) -> p1.number(g, b) > p2.number(g, b) ? 1 : 0);
        expressionsMap.put("lt", (g, b, p1, p2) -> p1.number(g, b) < p2.number(g, b) ? 1 : 0);
        expressionsMap.put("and", (g, b, p1, p2) -> p1.bool(g, b) && p2.bool(g, b) ? 1 : 0);
        expressionsMap.put("or", (g, b, p1, p2) -> p1.bool(g, b) || p2.bool(g, b) ? 1 : 0);
        expressionsMap.put("sum", (g, b, p1, p2) -> p1.number(g, b) + p2.number(g, b));
        expressionsMap.put("get my cell", (g, b, p1, p2) -> b.getMyCellInfo(p1.number(g, b)));
        expressionsMap.put("get my property", (g, b, p1, p2) -> b.getMyInfo(p1.number(g, b)));

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
            if (!expressionsMap.containsKey(expressionDef.name())) {
                throw new IllegalStateException("Expression " + expressionDef.name() + " is not mapped");
            }
            ExpressionDef existing = expressionDefs.put(code, expressionDef);
            if (existing != null) {
                throw new IllegalStateException("Duplicated expression code " + code);
            }
        }
    }
}


