package com.gordonfromblumberg.games.core.evocell.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

public final class Actions {
    private static final ObjectMap<String, ActionMapping> actionsMap = new ObjectMap<>();
    private static final IntMap<ActionDef> actionDefs = new IntMap<>();

    static {
        actionsMap.put("move", (w, lc, p) -> lc.move(w.getGrid()));
        actionsMap.put("rotateLeft", (w, lc, p) -> lc.rotateLeft());
        actionsMap.put("rotateRight", (w, lc, p) -> lc.rotateRight());
        actionsMap.put("rotate", (w, lc, p) -> {
            if (p % 2 == 0) lc.rotateRight();
            else lc.rotateLeft();
        });

        loadActionDefs();
    }

    private Actions() { }

    private static void loadActionDefs() {
        final JsonReader jsonReader = new JsonReader();
        final JsonValue array = jsonReader.parse(Gdx.files.internal("model/actions.json"));
        for (JsonValue actionDesc = array.child; actionDesc != null; actionDesc = actionDesc.next) {
            byte code = actionDesc.getByte("value");
            ActionDef existing = actionDefs.put(code, new ActionDef(ActionDef.Type.valueOf(actionDesc.getString("type")),
                                               code,
                                               actionDesc.getString("name"),
                                               actionDesc.getString("description"),
                                               parseParameters(actionDesc.get("parameters"))));
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
