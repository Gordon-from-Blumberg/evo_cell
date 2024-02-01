package com.gordonfromblumberg.games.core.evocell.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.world.WorldRenderer;
import com.gordonfromblumberg.games.core.evocell.model.*;

public class GameWorldRenderer extends WorldRenderer<GameWorld> {
    private static final Logger log = LogManager.create(GameWorldRenderer.class);
    private static final Color MINERALS_COLOR = new Color(Color.BLUE);
    private static final Color MIN_TEMPERATURE_COLOR = new Color();
    private static final Color MAX_TEMPERATURE_COLOR = new Color();
    private static final float MAX_MINERALS = 100f;
    private static final Color color = new Color();
    private static final Color tempColor = new Color();

    static {
        ConfigManager config = AbstractFactory.getInstance().configManager();
        config.getColor("render.minTemperatureColor", MIN_TEMPERATURE_COLOR);
        config.getColor("render.maxTemperatureColor", MAX_TEMPERATURE_COLOR);
    }

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final RenderParams renderParams;
    private final Array<LivingCell> livingCells = new Array<>();

    private final Color livingCellColor = new Color(Color.OLIVE);

    private final float maxLightColor = 1f;
    private final float minLightColor;

    public GameWorldRenderer(GameWorld world, RenderParams renderParams) {
        super(world);

        final ConfigManager config = AbstractFactory.getInstance().configManager();
        this.renderParams = renderParams;
        this.minLightColor = config.getFloat("render.minLightColor");
    }

    @Override
    public void render(float dt) {
        updateCamera();

        final ShapeRenderer shapeRenderer = this.shapeRenderer;
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);

        final GameWorld world = this.world;
        final int cellGridWidth = world.cellGrid.getWidth();
        final int cellGridHeight = world.cellGrid.getHeight();
        final int cellSize = world.cellGrid.getCellSize();
        final int livingCellSize = cellSize - 2;

        final float minLight = world.params.minLight;
        final float maxLight = world.params.maxLight;
        final float minTemperature = world.params.minTemperature;
        final float maxTemperature = world.params.maxTemperature;
        final Cell[][] cells = world.cellGrid.cells;

        final boolean renderLight = renderParams.renderLight;
        final boolean renderMinerals = renderParams.renderMinerals;
        final boolean renderTemperature = renderParams.renderTemperature;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < cellGridWidth; ++i) {
            final Cell[] col = cells[i];
            for (int j = 0; j < cellGridHeight; ++j) {
                Cell cell = col[j];
                color.set(Color.WHITE);
                if (renderMinerals) {
                    color.lerp(MINERALS_COLOR, cell.getMinerals() / MAX_MINERALS);
                }
                if (renderTemperature) {
                    tempColor.set(Color.WHITE);
                    int temperature = cell.getTemperature();
                    if (temperature > 15) {
                        tempColor.lerp(MAX_TEMPERATURE_COLOR, (temperature - 15) / (maxTemperature - 15));
                    } else if (temperature < 15) {
                        tempColor.lerp(MIN_TEMPERATURE_COLOR, (15 - temperature) / (15 - minTemperature));
                    }
                    color.mul(tempColor);
                }
                if (renderLight) {
                    color.mul(MathUtils.map(minLight, maxLight, minLightColor, maxLightColor, cell.getSunLight()));
                }
                shapeRenderer.setColor(color);
                shapeRenderer.rect(i * cellSize, j * cellSize, cellSize, cellSize);
                if (cell.getObject() != null) {
                    livingCells.add(cell.getObject());
                }
            }
        }

        shapeRenderer.setColor(livingCellColor);
        for (LivingCell livingCell : livingCells) {
            shapeRenderer.rect(cellSize * livingCell.getCell().getX() + 1,
                    cellSize * livingCell.getCell().getY() + 1,
                    livingCellSize, livingCellSize);
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        setLineWidth(1f);
        final int quarterCell = cellSize / 4;
        final int threeQuarters = 3 * cellSize / 4;
        for (LivingCell livingCell : livingCells) {
            int x = cellSize * livingCell.getCell().getX();
            int y = cellSize * livingCell.getCell().getY();
            shapeRenderer.rect(x + 1,y + 1, livingCellSize, livingCellSize);
            switch (livingCell.getDir()) {
                case up -> shapeRenderer.line(x + quarterCell, y + threeQuarters,
                        x + threeQuarters, y + threeQuarters);
                case right -> shapeRenderer.line(x + threeQuarters, y + quarterCell,
                        x + threeQuarters, y + threeQuarters);
                case down -> shapeRenderer.line(x + quarterCell, y + quarterCell,
                        x + threeQuarters, y + quarterCell);
                case left -> shapeRenderer.line(x + quarterCell, y + quarterCell,
                        x + quarterCell, y + threeQuarters);
            }
        }
        livingCells.clear();

        shapeRenderer.setColor(Color.YELLOW);
        setLineWidth(2f);
        shapeRenderer.rect(0, 0, cellSize * cellGridWidth, cellSize * cellGridHeight);
        shapeRenderer.end();
    }

    private void setLineWidth(float width) {
        Gdx.gl20.glLineWidth(width / getCamera().zoom);
    }

    private void updateCamera() {
        float cameraSpeed = 8 * camera.zoom;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.translate(-cameraSpeed, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.translate(cameraSpeed, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.translate(0, cameraSpeed);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.translate(0, -cameraSpeed);
        }

        camera.update();
    }
}
