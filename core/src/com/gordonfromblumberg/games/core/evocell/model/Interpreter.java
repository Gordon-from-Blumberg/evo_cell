package com.gordonfromblumberg.games.core.evocell.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.evocell.world.GameWorld;

public class Interpreter {
    private static final Logger log = LogManager.create(Interpreter.class);

    private final IntMap<ActionDef> actions = new IntMap<>();

    public Interpreter() {
        log.info("Start loading...");
        load();
        log.info("Loading finished");
    }

    public void run(GameWorld world, EvoLivingCell cell) {

    }

    @SuppressWarnings("unchecked")
    private void load() {
        final Json json = new Json();
        Array<ActionDef> actionList = json.fromJson(Array.class, ActionDef.class, Gdx.files.classpath("model/actions.json"));
        for (ActionDef a : actionList) {
            if (actions.containsKey(a.getValue())) {
                throw new IllegalStateException("Duplicate ActionDef key " + a.getValue());
            }

            actions.put(a.getValue(), a);
        }
    }
}
