package com.gordonfromblumberg.games.core.game_template;

import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.world.WorldUIInfo;
import com.gordonfromblumberg.games.core.common.world.WorldUIRenderer;

public class TemplateUIRenderer extends WorldUIRenderer<TemplateWorld> {
    private static final Logger log = LogManager.create(TemplateUIRenderer.class);
    private static final String DEFAULT_SAVE_DIR = "saves";
    private static final String SAVE_EXTENSION = "dat";

    public TemplateUIRenderer(WorldUIInfo<TemplateWorld> info) {
        super(info);
        log.info("TemplateUIRenderer constructor");

        init();
    }

    private void init() {
        addSaveLoadWindow(1 << 10, DEFAULT_SAVE_DIR, SAVE_EXTENSION);
    }
}
