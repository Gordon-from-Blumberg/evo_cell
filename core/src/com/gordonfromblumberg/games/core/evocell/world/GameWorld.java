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
    final PooledLinkedList<Bot> livingCells = new PooledLinkedList<>(2048);
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

        setInitialMinerals(0.1f);
        initDebug();
    }

    private void initDebug() {
        int x = params.getWidth() / 2;
        int y = params.getHeight() * 3 / 4;
//        for (Direction d : Direction.ALL) {
            Bot bot = SimpleBot.getInstance();
            bot.setCell(cellGrid.cells[x][y - 30]);
            bot.setEnergy(50);
            bot.setOrganics(20);
            bot.setDir(Direction.random());
            bot.setTemperature(17);
            bot.setWater(10);
            bot.init();
//            livingCell.setDir(d);
//            if (d.ordinal() % 2 == 0) ++x; else ++y;
//        }

        EvoBot evoBot = EvoBot.getInstance();
        evoBot.setRandomDna();
        evoBot.setCell(cellGrid.cells[x - 2][y - 2]);
        cellGrid.cells[x - 2][y - 2].setMinerals(10);
        evoBot.setEnergy(1000);
        evoBot.setOrganics(100);
        evoBot.setDir(Direction.random());
        evoBot.setTemperature(17);
        evoBot.setWater(10);
        evoBot.setGene(0, -1, 1, 0, 0, 0, 0, 0, 0, 0, 14, 0, 0, -101, 7, -103);
        evoBot.setGene(1, 0, -99, -125, 3, -121, -20, 3, 0, 0, 30, 0, -102, 2);
        evoBot.setGene(2, 0, -99, -125, 3, -121, -21, 1, -121, 12, 2, 120, -102, 3);
        evoBot.setGene(3, 0, -100, -125, 3, -121, -21, 2, 30, 16, 17, 120, -102, 3);
        evoBot.init();
        interpreter.runEmbryo(this, evoBot);

        System.out.println(interpreter.print(evoBot));
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

    public void updateCellStatistic(Bot bot) {
        ++statistic.cellCount;
        statistic.totalCellEnergy += bot.getEnergy();
        statistic.totalCellOrganics += bot.getOrganics();
        statistic.totalCellMinerals += bot.getMinerals();
        if (bot.getAge() > statistic.currentMaxCellAge)
            statistic.currentMaxCellAge = bot.getAge();
        if (bot.getOrganics() > statistic.maxCellOrganics)
            statistic.maxCellOrganics = bot.getOrganics();
        if (bot.getMinerals() > statistic.maxCellMinerals)
            statistic.maxCellMinerals = bot.getMinerals();
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

    private void setInitialMinerals(float probability) {
        final LightDistribution lightDist = this.lightDistribution;
        final Cell[][] cells = cellGrid.cells;
        for (int i = 0, w = cellGrid.getWidth(), h = cellGrid.getHeight(); i < w; ++i) {
            final Cell[] col = cells[i];
            for (int j = 0; j < h; ++j) {
                final int light = lightDist.getLight(i, j, 0);
                final Cell cell = col[j];
                if (RandomGen.INSTANCE.nextBool(light > 0 ? probability / light : probability)) {
                    int min = 10 - light, max = 50 - 3 * light;
                    if (min < 0) min = 0;
                    if (max < 0) max = 0;
                    cell.setMinerals(RandomGen.INSTANCE.nextInt(min, max));
                }
            }
        }
    }
}
