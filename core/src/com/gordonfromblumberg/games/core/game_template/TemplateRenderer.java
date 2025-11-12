package com.gordonfromblumberg.games.core.game_template;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.gordonfromblumberg.games.core.common.animation.GbAnimation;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.model.GameObject;
import com.gordonfromblumberg.games.core.common.ui.ClickPoint;
import com.gordonfromblumberg.games.core.common.utils.Assets;
import com.gordonfromblumberg.games.core.common.world.WorldRenderer;

import java.util.Iterator;

public class TemplateRenderer extends WorldRenderer<TemplateWorld> {
    private static final Logger log = LogManager.create(TemplateRenderer.class);
    private static final Color TEMP_COLOR = new Color();

    private final Batch batch;
    private final Rectangle worldArea = new Rectangle();
    private IsometricTiledMapRenderer mapRenderer;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    final Array<ClickPoint> clickPoints = new Array<>();

    TextureRegion background;
    private final Color pauseColor = Color.GRAY;
    final BitmapFontCache pauseText;

    public TemplateRenderer(TemplateWorld world, Batch batch) {
        super(world);
        log.info("TemplateRenderer constructor");

        this.batch = batch;

        pauseText = new BitmapFontCache(Assets.manager()
                .get("ui/uiskin.json", Skin.class)
                .getFont("default-font"));
        pauseText.setText("PAUSE", 100, 100);
    }

    public void initialize() {
        log.info("TemplateRenderer init");
        final AssetManager assets = Assets.manager();

        background = assets
                .get("image/texture_pack.atlas", TextureAtlas.class)
                .findRegion("background");

        this.mapRenderer = new IsometricTiledMapRenderer(world.getMap(), batch);
        TiledMapTileLayer l = (TiledMapTileLayer) world.getMap().getLayers().get("map");
        viewport.getCamera().position.set(l.getWidth() * l.getTileWidth() / 2f, 0, 0);
        viewToWorld.set(new float[] {
                1.0f / l.getTileWidth(),  1.0f / l.getTileWidth(),  0.0f,
                -1.0f / l.getTileHeight(), 1.0f / l.getTileHeight(), 0.0f,
                0.5f,                    -0.5f,                     1.0f
        });
        worldToView.set(viewToWorld).inv();
    }

    @Override
    public void render(float dt) {
        updateCamera(8 * camera.zoom);

        batch.begin();
        final Color origColor = TEMP_COLOR.set(batch.getColor());
        if (world.isPaused()) {
            batch.setColor(pauseColor);
        }

//        batch.draw(background, 0, 0);

        mapRenderer.setView((OrthographicCamera) viewport.getCamera());
        mapRenderer.renderTileLayer((TiledMapTileLayer) world.getMap().getLayers().get(0));

        if (world.isPaused()) {
            for (GameObject gameObject : world.getGameObjects()) {
                gameObject.getSprite().setColor(pauseColor);
                gameObject.render(batch);
                gameObject.getSprite().setColor(Color.WHITE);
            }
        } else {
            for (GameObject gameObject : world.getGameObjects()) {
                gameObject.render(batch);
            }
            batch.end();

            shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0f, 1f, 0f, 1f);
            final Iterator<ClickPoint> it = clickPoints.iterator();
            final Vector3 vec3 = Pools.obtain(Vector3.class);
            while (it.hasNext()) {
                ClickPoint cp = it.next();
                float x = cp.getX();
                float y = cp.getY();
                worldToView(vec3.set(x, y, 1));
                GbAnimation animation = cp.getAnimation();
                animation.update(dt);
                final float circleMul = cp.getCircle();
                log.debug("Render click at " + x + ", " + y + ", mul = " + circleMul);
                final float clickWidth = circleMul * ClickPoint.WIDTH;
                final float clickHeight = circleMul * ClickPoint.HEIGHT;
                shapeRenderer.ellipse(x - clickWidth / 2, y - clickHeight / 2, clickWidth, clickHeight);

                if (animation.isFinished()) {
                    it.remove();
                    cp.release();
                }
            }
            Pools.free(vec3);
            shapeRenderer.end();

            batch.begin();
        }

        if (world.isPaused()) {
            pauseText.draw(batch);
            batch.setColor(origColor);
        }

        batch.end();
    }

    public void click(float x, float y) {
        Vector3 tempVec3 = Pools.obtain(Vector3.class);
        worldToView(tempVec3.set(x, y, 1));
        ClickPoint cp = ClickPoint.getInstance();
        clickPoints.add(cp.init(tempVec3.x, tempVec3.y));
        Pools.free(tempVec3);
    }
}
