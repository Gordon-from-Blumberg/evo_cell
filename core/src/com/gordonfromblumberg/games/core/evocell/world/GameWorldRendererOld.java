package com.gordonfromblumberg.games.core.evocell.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.screens.AbstractRenderer;
import com.gordonfromblumberg.games.core.common.ui.ClickPoint;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.evocell.model.Cell;

import java.util.Iterator;

public class GameWorldRendererOld extends AbstractRenderer {
    private static final Logger log = LogManager.create(GameWorldRendererOld.class);

    private static final Color LIGHT_COLOR = new Color(0.4f, 0.8f, 1f, 1f);
    private static final Color DARK_COLOR = new Color(0f, 0.12f, 0.07f, 1f);
    private static final Color MID_COLOR = new Color(0.9f, 1f, 0.9f, 0);
    private static final Color LIGHT_SOURCE_COLOR = new Color(1f, 1f, 0.3f, 1f);
    private static final float MAX_SUN_LIGHT;
    private static final Color SEED_COLOR = new Color(0.66f, 0.41f, 0.19f, 1f);

    private static final int TREE_PART_MIN_ABSORPTION;
    private static final int TREE_PART_MAX_ABSORPTION;

    private static final Color MIN_ABS_COLOR = new Color(0.5f, 1f, 0.7f, 1f);
    private static final Color MAX_ABS_COLOR = new Color(0f, 0.6f, 0.1f, 1f);
    private static final float MAX_ABSORPTION;

    private static final Vector3 tempVec3 = new Vector3();

    static {
        ConfigManager configManager = AbstractFactory.getInstance().configManager();
        configManager.getColor("world.lightColor", LIGHT_COLOR);
        configManager.getColor("world.darkColor", DARK_COLOR);
        configManager.getColor("world.lightSource.color", LIGHT_SOURCE_COLOR);
//        MID_COLOR.set(DARK_COLOR).lerp(LIGHT_COLOR, configManager.getFloat("world.midColor"));

        TREE_PART_MIN_ABSORPTION = configManager.getInteger("treePart.minAbsorption")
                + configManager.getInteger("treePart.absorptionShift");
        TREE_PART_MAX_ABSORPTION = configManager.getInteger("treePart.maxAbsorption")
                + configManager.getInteger("treePart.absorptionShift");

        MAX_SUN_LIGHT = 400;
        configManager.getColor("seed.color", SEED_COLOR);

        // lighting test
        configManager.getColor("world.lightingTest.minAbsColor", MIN_ABS_COLOR);
        configManager.getColor("world.lightingTest.maxAbsColor", MAX_ABS_COLOR);
        MAX_ABSORPTION = configManager.getInteger("world.lightingTest.maxAbsorption");
    }

    private final GameWorld world;
    private FrameBuffer fbo;
    private final Batch batch;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Matrix3 viewToWorld = new Matrix3();
    private final Matrix3 worldToView = new Matrix3();

    final Array<ClickPoint> clickPoints = new Array<>();

    private final Color pauseColor = Color.GRAY;

    private int lastTurnRendered = -1;

    public GameWorldRendererOld(GameWorld world, Batch batch) {
        super();
        log.info("GameWorldRenderer constructor");
        this.batch = batch;
        this.world = world;
    }

    public void initialize() {
        log.info("GameWorldRenderer init");
        final AssetManager assets = Main.getInstance().assets();

//        int cellSize = world.cellGrid.getCellSize();
//        int worldHeight = world.cellGrid.getHeight() * cellSize;
//        if (worldHeight > viewport.getWorldHeight()) {
//            float ration = viewport.getWorldWidth() / viewport.getWorldHeight();
//            viewport.setWorldSize(ration * worldHeight, worldHeight);
//            viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
//        }

//        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, world.cellGrid.getWidth() * cellSize, worldHeight, false);
        Texture texture = fbo.getColorBufferTexture();
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
    }

    @Override
    public void render(float dt) {
        updateCamera();

        if (world.isPaused()) {
            batch.setColor(pauseColor);
        }

        final ShapeRenderer shapeRenderer = this.shapeRenderer;
        if (false) {
            fbo.begin();
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

//            shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
            shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, fbo.getWidth(), fbo.getHeight());
            shapeRenderer.identity();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//            int cellSize = world.cellGrid.getCellSize();
//            Cell[][] cells = world.cellGrid.cells;
            final float nightR = DARK_COLOR.r, nightG = DARK_COLOR.g, nightB = DARK_COLOR.b;
            final float diffR = LIGHT_COLOR.r - DARK_COLOR.r,
                    diffG = LIGHT_COLOR.g - DARK_COLOR.g,
                    diffB = LIGHT_COLOR.b - DARK_COLOR.b;
            final float absR = MAX_ABS_COLOR.r, absG = MAX_ABS_COLOR.g, absB = MAX_ABS_COLOR.b;
            final float daR = MIN_ABS_COLOR.r - MAX_ABS_COLOR.r,
                    daG = MIN_ABS_COLOR.g - MAX_ABS_COLOR.g,
                    daB = MIN_ABS_COLOR.b - MAX_ABS_COLOR.b;
//            for (int i = 0, w = world.cellGrid.getWidth(); i < w; ++i) {
//                for (int j = 0, h = world.cellGrid.getHeight(); j < h; ++j) {
//                    final Cell cell = cells[i][j];
//                    final CellObject cellObject = cell.getObject();
//                    if (cellObject == null) {
//                        float k = cell.getSunLight() / MAX_SUN_LIGHT;
//                        shapeRenderer.setColor(
//                                nightR + diffR * k,
//                                nightG + diffG * k,
//                                nightB + diffB * k,
//                                1);
//                    } else {
//                        if (cellObject instanceof Seed) {
//                            shapeRenderer.setColor(SEED_COLOR);
//
//                        } else if (cellObject instanceof TreePart) {
//                            TreePart part = (TreePart) cellObject;
//                            Color treeColor = part.getTree().getColor();
//
//                            float progress = (part.getLightAbsorption() - TREE_PART_MIN_ABSORPTION)
//                                    / (float) (TREE_PART_MAX_ABSORPTION - TREE_PART_MIN_ABSORPTION);
//                            float o = MathUtils.lerp(0.5f, 1f, progress);
//                            if (o > 1f) o = 1f;
//                            float k = 1;
//                            if (part.getType() == TreePartType.SHOOT) k = 1.3f;
//                            else if (part.getType() == TreePartType.DEAD) {
//                                k = 0.5f;
//                                o *= 1.2f;
//                            }
//                            shapeRenderer.setColor(
//                                    (k * treeColor.r - MID_COLOR.r) * o + MID_COLOR.r,
//                                    (k * treeColor.g - MID_COLOR.g) * o + MID_COLOR.g,
//                                    (k * treeColor.b - MID_COLOR.b) * o + MID_COLOR.b,
//                                    1f);
//
//                        } else if (cellObject instanceof LightSource) {
//                            shapeRenderer.setColor(LIGHT_SOURCE_COLOR);
//                        } else {
//                            float k = 1 - cellObject.getLightAbsorption() / MAX_ABSORPTION;
//                            shapeRenderer.setColor(
//                                    absR + daR * k,
//                                    absG + daG * k,
//                                    absB + daB * k,
//                                    1);
//                        }
//                    }
//                    shapeRenderer.rect(i * cellSize, j * cellSize, cellSize, cellSize);
//                }
//            }
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
//        Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl20.glLineWidth(1f / ((OrthographicCamera) viewport.getCamera()).zoom);
//            for (int i = 0, w = world.cellGrid.getWidth(); i < w; ++i) {
//                Cell[] col = cells[i];
//                for (int j = 0, h = world.cellGrid.getHeight(); j < h; ++j) {
//                    CellObject treePart = col[j].getObject();
//                    if (treePart != null) {
//                        if (treePart instanceof TreePart && ((TreePart) treePart).getType() == TreePartType.SHOOT) {
//                            shapeRenderer.setColor(0.4f, 0.3f, 0f, 1f);
//                        } else {
//                            shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
//                        }
//                        shapeRenderer.rect(i * cellSize + 1, j * cellSize + 1, cellSize - 2, cellSize - 2);
//                    }
//                }
//            }
            shapeRenderer.end();

            fbo.end();
//            lastTurnRendered = world.getTurn();
        }

        Texture texture = fbo.getColorBufferTexture();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.draw(texture, -texture.getWidth(), 0, 3 * texture.getWidth(), texture.getHeight(), -1, 0, 2, 1);
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        final Iterator<ClickPoint> it = clickPoints.iterator();
//        while (it.hasNext()) {
//            ClickPoint cp = it.next();
//            worldToView(tempVec3.set(cp.x, cp.y, 1));
//            cp.animation.update(dt);
//            final float circleMul = cp.getCircle();
//            log.debug("Render click at " + cp.x + ", " + cp.y + ", mul = " + circleMul);
//            final float clickWidth = circleMul * ClickPoint.WIDTH;
//            final float clickHeight = circleMul * ClickPoint.HEIGHT;
//            shapeRenderer.ellipse(cp.x - clickWidth / 2, cp.y - clickHeight / 2, clickWidth, clickHeight);
//
//            if (cp.animation.isFinished()) {
//                it.remove();
//                cp.release();
//            }
//        }
        shapeRenderer.end();

//        batch.begin();
//
//        if (world.paused) {
//            world.pauseText.draw(batch);
//            batch.setColor(origColor);
//        }
//
//        batch.end();
    }

    /**
     * Transforms viewport coordinates to logical world
     */
    public void viewToWorld(float x, float y, Vector3 out) {
        out.set(x, y, 1f).mul(viewToWorld);
    }

    /**
     * Transforms logical world to viewport coordinates
     */
    public void worldToView(Vector3 coords) {
        coords.z = 1.0f;
        coords.mul(worldToView);
    }

    private void updateCamera() {
        float cameraSpeed = 8 * camera.zoom;
        float textureWidth = fbo.getWidth();
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.translate(-cameraSpeed, 0);
            if (camera.position.x < -textureWidth / 2) {
                camera.position.x += textureWidth;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.translate(cameraSpeed, 0);
            if (camera.position.x > 1.5f * textureWidth) {
                camera.position.x -= textureWidth;
            }
        }
//        if (Gdx.input.isKeyPressed(Input.Keys.UP))
//            camera.translate(0, cameraSpeed);
//        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
//            camera.translate(0, -cameraSpeed);

        camera.update();
    }
}
