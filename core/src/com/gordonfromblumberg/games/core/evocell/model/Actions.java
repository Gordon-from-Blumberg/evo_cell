package com.gordonfromblumberg.games.core.evocell.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

public final class Actions {
    static final ObjectMap<String, ActionMapping> actionsMap = new ObjectMap<>();
    static final IntMap<ActionDef> actionDefs = new IntMap<>();
    static final IntMap<ActionDef> embryoActionDefs = new IntMap<>();

    static {
        actionsMap.put("stop", (w, lc, c, p) -> {});
        actionsMap.put("move", (w, lc, c, p) -> lc.move(w.getGrid()));
        actionsMap.put("rotateLeft", (w, lc, c, p) -> lc.rotateLeft());
        actionsMap.put("rotateRight", (w, lc, c, p) -> lc.rotateRight());
        actionsMap.put("rotate", (w, lc, c, p) -> {
            if (p % 2 == 0) lc.rotateRight();
            else lc.rotateLeft();
        });
        actionsMap.put("produceOffspring", (w, lc, c, p) -> lc.produceOffspring(w, c));

        loadActionDefs();
        loadEmbryoActionDefs();
    }

    private Actions() {
        throw new UnsupportedOperationException("Actions should not be instantiated");
    }

    private static void loadActionDefs() {
        final JsonReader jsonReader = new JsonReader();
        final JsonValue array = jsonReader.parse(Gdx.files.internal("model/actions.json"));
        for (JsonValue actionDesc = array.child; actionDesc != null; actionDesc = actionDesc.next) {
            byte code = actionDesc.getByte("value");
            JsonValue tagValue = actionDesc.get("tag");
            ActionDef actionDef = new ActionDef(ActionDef.Type.valueOf(actionDesc.getString("type")),
                                                code,
                                                actionDesc.getString("name"),
                                                actionDesc.getString("description"),
                                                tagValue == null ? actionDesc.getString("name") : tagValue.asString(),
                                                parseParameters(actionDesc.get("parameters")));
            ActionDef existing = actionDefs.put(code, actionDef);
            if (existing != null) {
                throw new IllegalStateException("Duplicated action code " + code);
            }
            JsonValue embryo = actionDesc.get("embryo");
            if (embryo != null && embryo.asBoolean()) {
                embryoActionDefs.put(code, actionDef);
            }
        }
    }

    private static void loadEmbryoActionDefs() {
        final JsonReader jsonReader = new JsonReader();
        final JsonValue array = jsonReader.parse(Gdx.files.internal("model/embryoActions.json"));
        for (JsonValue actionDesc = array.child; actionDesc != null; actionDesc = actionDesc.next) {
            byte code = actionDesc.getByte("value");
            JsonValue tagValue = actionDesc.get("tag");
            ActionDef actionDef = new ActionDef(ActionDef.Type.valueOf(actionDesc.getString("type")),
                                                code,
                                                actionDesc.getString("name"),
                                                actionDesc.getString("description"),
                                                tagValue == null ? actionDesc.getString("name") : tagValue.asString(),
                                                parseParameters(actionDesc.get("parameters")));
            ActionDef existing = embryoActionDefs.put(code, actionDef);
            if (existing != null) {
                throw new IllegalStateException("Duplicated action code " + code);
            }
        }
    }

    private static ActionDef.ActionParameterDef[] parseParameters(JsonValue parameters) {
        if (parameters == null)
            return null;

        ActionDef.ActionParameterDef[] result = new ActionDef.ActionParameterDef[parameters.size];
        JsonValue parDesc = parameters.child;
        for (int i = 0; parDesc != null; parDesc = parDesc.next, ++i) {
            JsonValue defaultDesc = parDesc.get("default");
            String parameterType = defaultDesc.getString("type");
            ActionDef.DefaultParameterValue defaultParameterValue = switch (parameterType) {
                case "constant" -> new ActionDef.ConstantParameterValue(defaultDesc.getByte("value"));
                case "random" -> new ActionDef.RandomParameterValue(defaultDesc.getByte("min"), defaultDesc.getByte("max"));
                default -> throw new IllegalStateException("Unknown parameter type " + parameterType);
            };
            result[i] = new ActionDef.ActionParameterDef(parDesc.getString("name"), defaultParameterValue);
        }
        return result;
    }
}
