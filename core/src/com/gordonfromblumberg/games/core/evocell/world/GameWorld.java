package com.gordonfromblumberg.games.core.evocell.world;

import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.world.World;
import com.gordonfromblumberg.games.core.evocell.model.*;
import com.gordonfromblumberg.games.core.game_template.TemplateWorld;

public class GameWorld extends World {
    private static final Logger log = LogManager.create(TemplateWorld.class);

    final WorldParams params;
    final CellGrid cellGrid;
    private final LightDistribution lightDistribution;

    private int turn = 0;
    private float time = 0f;
    private float updateDelay;

    public GameWorld(WorldParams params) {
        this.params = params;

        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        this.cellGrid = new CellGrid(params.width, params.height, configManager.getInteger("world.cellSize"));
        this.lightDistribution = new StaticLightDistribution(params.height, params.minLight, params.maxLight);
        log.debug("GameWorld was constructed");
    }

    @Override
    public void initialize() {
        super.initialize();

        final Cell[][] cells = cellGrid.cells;
        for (int i = 0, w = cells.length; i < w; ++i) {
            final Cell[] cellCol = cells[i];
            for (int j = 0, h = cellCol.length; j < h; ++j) {
                cellCol[j].setSunLight(lightDistribution.getLight(i, j, 0));
            }
        }

        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        updateDelay = 1f / configManager.getInteger("world.turnsPerSecond");

        initDebug();
    }

    private void initDebug() {
        LivingCell livingCell = SimpleLivingCell.getInstance();
        livingCell.setCell(cellGrid.cells[50][50]);
        livingCell.setEnergy(50);
        livingCell.setOrganics(20);
    }

    @Override
    public void update(float delta, float mouseX, float mouseY) {
        super.update(delta, mouseX, mouseY);

        if (!paused) {
            time += delta;
            if (time < updateDelay) {
                return;
            }

            time = 0;
            ++turn;

            final Cell[][] cells = cellGrid.cells;
            final int cellGridWidth = cellGrid.getWidth();
            final int cellGridHeight = cellGrid.getHeight();
            for (int i = 0; i < cellGridWidth; ++i) {
                final Cell[] cellCol = cells[i];
                for (int j = 0; j < cellGridHeight; ++j) {
                    cellCol[j].update(this);
                }
            }
        }
    }

    public WorldParams getParams() {
        return params;
    }

    public int getTurn() {
        return turn;
    }
}
