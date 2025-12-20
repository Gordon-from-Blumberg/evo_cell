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
        actionsMap.put("stop", (w, b, c, p) -> {});
        actionsMap.put("nothing", (w, b, c, p) -> {});
        actionsMap.put("setActiveGene", (w, b, c, p) -> b.setActiveGeneIndex(p));
        actionsMap.put("move", (w, b, c, p) -> b.move(w.getGrid(), c));
        actionsMap.put("rotateLeft", (w, b, c, p) -> b.rotateLeft(c));
        actionsMap.put("rotateRight", (w, b, c, p) -> b.rotateRight(c));
        actionsMap.put("rotate", (w, b, c, p) -> {
            if (p % 2 == 0) b.rotateRight(c);
            else b.rotateLeft(c);
        });
        actionsMap.put("increaseParameter", (w, b, c, p) -> b.increaseParameter(p, c));
        actionsMap.put("decreaseParameter", (w, b, c, p) -> b.decreaseParameter(p, c));
        actionsMap.put("produceOffspring", (w, b, c, p) -> b.produceOffspring(w, c));
        actionsMap.put("produceOrganics", (w, b, c, p) -> b.produceOrganics(c));
        actionsMap.put("eatOrganics", (w, b, c, p) -> b.eatOrganics(c));
        actionsMap.put("digestOrganics", (w, b, c, p) -> b.digestOrganics(c));
        actionsMap.put("bite", (w, b, c, p) -> b.bite(w.getGrid(), c));
        actionsMap.put("eatMinerals", (w, b, c, p) -> b.eatMinerals(c));
        actionsMap.put("chemosynthesis", (w, b, c, p) -> b.chemosynthesis(c));
        actionsMap.put("transformMineralsToOrganics", (w, b, c, p) -> b.transformMineralsToOrganics(c));
        actionsMap.put("transformOrganicsToMinerals", (w, b, c, p) -> b.transformOrganicsToMinerals(c));
        actionsMap.put("regenerate", (w, b, c, p) -> b.regenerate(c));

        actionsMap.put("increaseParameterEmbryo", (w, b, c, p) -> b.increaseParameterEmbryo(p));
        actionsMap.put("decreaseParameterEmbryo", (w, b, c, p) -> b.decreaseParameterEmbryo(p));

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
            if (actionDef.type() == ActionDef.Type.action && !actionsMap.containsKey(actionDef.name())) {
                throw new IllegalStateException("Action " + actionDef.name() + " is not mapped");
            }
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
            JsonValue codeValue = actionDesc.get("value");
            ActionDef.Type type = ActionDef.Type.valueOf(actionDesc.getString("type"));
            String name = actionDesc.getString("name");
            String description = actionDesc.getString("description");
            JsonValue tagValue = actionDesc.get("tag");
            String tag = tagValue == null ? actionDesc.getString("name") : tagValue.asString();
            ActionDef.ActionParameterDef[] parameters = parseParameters(actionDesc.get("parameters"));
            if (codeValue != null) {
                byte code = codeValue.asByte();
                ActionDef actionDef = new ActionDef(type,
                                                    code,
                                                    name,
                                                    description,
                                                    tag,
                                                    parameters);
                ActionDef existing = embryoActionDefs.put(code, actionDef);
                if (existing != null) {
                    throw new IllegalStateException("Duplicated action code " + code);
                }
            } else {
                putEmbryoActionAsRange(actionDesc.getByte("codeFrom"), actionDesc.getByte("codeTo"),
                                       type, name, description, tag, parameters);
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

    private static void putEmbryoActionAsRange(byte codeFrom, byte codeTo, ActionDef.Type type, String name,
                                               String description, String tag, ActionDef.ActionParameterDef[] parameters) {
        for (byte code = codeFrom; code <= codeTo; ++code) {
            ActionDef actionDef = new ActionDef(type,
                                                code,
                                                name,
                                                description,
                                                tag,
                                                parameters);
            ActionDef existing = embryoActionDefs.put(code, actionDef);
            if (existing != null) {
                throw new IllegalStateException("Duplicated action code " + code);
            }
        }
    }
}
