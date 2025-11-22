package com.gordonfromblumberg.games.core.evocell.world;

import com.badlogic.gdx.utils.PooledLinkedList;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.utils.RandomGen;
import com.gordonfromblumberg.games.core.common.world.World;
import com.gordonfromblumberg.games.core.evocell.model.*;
import com.gordonfromblumberg.games.core.game_template.TemplateWorld;

public class GameWorld extends World {
    private static final Logger log = LogManager.create(TemplateWorld.class);

    final WorldParams params;
    final CellGrid cellGrid;
    final PooledLinkedList<LivingCell> livingCells = new PooledLinkedList<>(2048);
    private final LightDistribution lightDistribution;
    private final TemperatureDistribution temperatureDistribution;
    private final HumidityDistribution humidityDistribution;
    final WorldStatistic statistic = new WorldStatistic();
    private final Interpreter interpreter;
    Cell selectedCell;

    private int turn = 0;
    private float time = 0f;
    private float updateDelay;

    public GameWorld(WorldParams params) {
        this.params = params;

        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        this.cellGrid = new CellGrid(params.width, params.height, configManager.getInteger("world.cellSize"));
        this.lightDistribution = new StaticLightDistribution(params);
        this.temperatureDistribution = new StaticTemperatureDistribution(params);
        this.humidityDistribution = new StaticHumidityDistribution(params);

        this.interpreter = new Interpreter();
        log.debug("GameWorld was constructed");
    }

    @Override
    public void initialize() {
        super.initialize();

        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        updateDelay = 1f / configManager.getInteger("world.turnsPerSecond");

        setInitialMinerals(0.01f, 1, 10);
        initDebug();
    }

    private void initDebug() {
        int x = params.getWidth() / 2;
        int y = params.getHeight() * 3 / 4;
//        for (Direction d : Direction.ALL) {
            LivingCell livingCell = SimpleLivingCell.getInstance();
            livingCell.setCell(cellGrid.cells[x][y]);
            livingCell.setEnergy(50);
            livingCell.setOrganics(20);
            livingCell.setDir(Direction.random());
            livingCell.setTemperature(17);
            livingCell.setWater(10);
            livingCell.init();
//            livingCell.setDir(d);
//            if (d.ordinal() % 2 == 0) ++x; else ++y;
//        }

        EvoLivingCell evoCell = EvoLivingCell.getInstance();
        evoCell.setRandomDna();
        evoCell.setCell(cellGrid.cells[x - 2][y - 2]);
        evoCell.setEnergy(1000);
        evoCell.setOrganics(100);
        evoCell.setDir(Direction.random());
        evoCell.setTemperature(17);
        evoCell.setWishedTemperature(17);
        evoCell.setWater(10);
        evoCell.setGene(0, -100, -5, 2, -103, 5, -127, -2, -103, 5);
        evoCell.init();
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
            final int turn = this.turn;
            for (int i = 0; i < cellGridWidth; ++i) {
                final Cell[] cellCol = cells[i];
                for (int j = 0; j < cellGridHeight; ++j) {
                    final Cell cell = cellCol[j];
                    cell.setSunLight(lightDistribution.getLight(i, j, turn));
                    cell.setTemperature(temperatureDistribution.getTemperature(i, j, turn));
                    cell.setHumidity(humidityDistribution.getHumidity(i, j, turn));
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

    public Interpreter interpreter() {
        return interpreter;
    }

    private void setInitialMinerals(float probability, int min, int max) {
        final Cell[][] cells = cellGrid.cells;
        for (int i = 0, w = cellGrid.getWidth(), h = cellGrid.getHeight(); i < w; ++i) {
            final Cell[] col = cells[i];
            for (int j = 0; j < h; ++j) {
                final Cell cell = col[j];
                if (RandomGen.INSTANCE.nextBool(probability)) {
                    cell.setMinerals(RandomGen.INSTANCE.nextInt(min, max));
                }
            }
        }
    }
}
