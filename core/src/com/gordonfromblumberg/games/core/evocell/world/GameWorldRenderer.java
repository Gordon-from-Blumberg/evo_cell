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
    private static final Color color = new Color();

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

        final int cellGridWidth = world.cellGrid.getWidth();
        final int cellGridHeight = world.cellGrid.getHeight();
        final int cellSize = world.cellGrid.getCellSize();
        final int livingCellSize = cellSize - 2;

        final float minLight = world.params.minLight;
        final float maxLight = world.params.maxLight;
        final Cell[][] cells = world.cellGrid.cells;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < cellGridWidth; ++i) {
            final Cell[] col = cells[i];
            for (int j = 0; j < cellGridHeight; ++j) {
                Cell cell = col[j];
                float c = MathUtils.map(minLight, maxLight, minLightColor, maxLightColor, cell.getSunLight());
                color.set(Color.WHITE).mul(c);
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
        for (LivingCell livingCell : livingCells) {
            shapeRenderer.rect(cellSize * livingCell.getCell().getX() + 1,
                    cellSize * livingCell.getCell().getY() + 1,
                    livingCellSize, livingCellSize);
        }

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
