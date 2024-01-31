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
    final WorldStatistic statistic = new WorldStatistic();
    Cell selectedCell;

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
        int x = params.getWidth() / 2;
        int y = params.getHeight() / 2;
//        for (Direction d : Direction.ALL) {
            LivingCell livingCell = SimpleLivingCell.getInstance();
            livingCell.setCell(cellGrid.cells[x][y]);
            livingCell.setEnergy(50);
            livingCell.setOrganics(20);
            livingCell.setDir(Direction.random());
            livingCell.init();
//            livingCell.setDir(d);
//            if (d.ordinal() % 2 == 0) ++x; else ++y;
//        }
    }

    @Override
    public void update(float delta, float mouseX, float mouseY) {
        super.update(delta, mouseX, mouseY);
        selectedCell = cellGrid.findCell((int) mouseX, (int) mouseY);

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
            statistic.resetForNewTurn();
            int worldEnergy = 0;
            int worldOrganics = 0;
            int worldMinerals = 0;
            for (int i = 0; i < cellGridWidth; ++i) {
                final Cell[] cellCol = cells[i];
                for (int j = 0; j < cellGridHeight; ++j) {
                    final Cell cell = cellCol[j];
                    cell.update(this);
                    worldEnergy += cell.getEnergy();
                    worldOrganics += cell.getOrganics();
                    worldMinerals += cell.getMinerals();
                }
            }
            statistic.worldEnergy = worldEnergy;
            statistic.worldOrganics = worldOrganics;
            statistic.worldMinerals = worldMinerals;
            statistic.updateMaximums();
        }
    }

    public void updateCellStatistic(LivingCell cell) {
        ++statistic.cellCount;
        statistic.totalCellEnergy += cell.getEnergy();
        statistic.totalCellOrganics += cell.getOrganics();
        statistic.totalCellMinerals += cell.getMinerals();
        if (cell.getAge() > statistic.currentMaxCellAge)
            statistic.currentMaxCellAge = cell.getAge();
        if (cell.getOrganics() > statistic.maxCellOrganics)
            statistic.maxCellOrganics = cell.getOrganics();
        if (cell.getMinerals() > statistic.maxCellMinerals)
            statistic.maxCellMinerals = cell.getMinerals();
    }

    public WorldParams getParams() {
        return params;
    }

    public CellGrid getGrid() {
        return cellGrid;
    }

    public int getTurn() {
        return turn;
    }
}
