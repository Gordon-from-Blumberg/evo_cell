package com.gordonfromblumberg.games.core.game_template;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.gordonfromblumberg.games.core.common.event.Event;
import com.gordonfromblumberg.games.core.common.event.EventHandler;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.model.GameObject;
import com.gordonfromblumberg.games.core.common.utils.Assets;
import com.gordonfromblumberg.games.core.common.utils.BSPTree;
import com.gordonfromblumberg.games.core.common.utils.ClickHandler;
import com.gordonfromblumberg.games.core.common.utils.RandomGen;
import com.gordonfromblumberg.games.core.common.world.World;

import java.util.Iterator;

public class TemplateWorld extends World {
    private static final Logger log = LogManager.create(TemplateWorld.class);
    private static int nextId = 1;

    private final Array<GameObject> gameObjects = new Array<>();

    private final BSPTree tree;

    public Rectangle visibleArea;
    private float width, height;

    TiledMap map;

    private int maxCount = 0;

    private float time = 0;
    private int score = 0;

    final Array<ClickHandler> clickHandlers = new Array<>(1);

    public TemplateWorld() {
        log.info("TemplateWorld constructor");

        visibleArea = new Rectangle();
        tree = new BSPTree(0, 0, 0, 0);
    }

    @Override
    public void initialize() {
        log.info("TemplateWorld init");
        final AssetManager assets = Assets.manager();
        RandomGen rand = RandomGen.INSTANCE;
        map = new TiledMap();
        int width = 30;
        int height = 30;
        TiledMapTileLayer layer = new TiledMapTileLayer(width, height, 100, 50);
        layer.setName("map");
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                int randTile = rand.nextInt(1, 3);
                cell.setTile(new StaticTiledMapTile(assets
                        .get("image/texture_pack.atlas", TextureAtlas.class)
                        .findRegion("tile0" + randTile)));
                layer.setCell(i, j, cell);
            }
        }
        map.getLayers().add(layer);
        log.debug("TemplateWorld initialized");
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        visibleArea.setSize(width, height);
    }

    public void addGameObject(GameObject gameObject) {
        gameObjects.add(gameObject);
        gameObject.setActive(true);
        gameObject.setId(nextId++);
        if (gameObjects.size > maxCount) maxCount = gameObjects.size;
    }

    public void removeGameObject(GameObject gameObject) {
        gameObjects.removeValue(gameObject, true);
        gameObject.release();
    }

    public Array<GameObject> getGameObjects() {
        return gameObjects;
    }

    /**
     * @param delta Time after last frame in seconds
     * @param mouseX World mouse x coordinate
     * @param mouseY World mouse y coordinate
     */
    @Override
    public void update(float delta, float mouseX, float mouseY) {
        super.update(delta, mouseX, mouseY);

        if (!paused) {
            time += delta;
//          visibleArea.set(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
//          tree.resetAndMove(0, 0);

            for (GameObject gameObject : gameObjects) {
                gameObject.update(delta);
//                if (gameObject.isActive()) {
//                  tree.addObject(gameObject);
//                }
            }

            detectCollisions();

            eventProcessor.process();

            if (time > 2) {
                time = 0;
                Gdx.app.log("GameWorld", gameObjects.size + " objects in the world of maximum " + maxCount);
            }
        }
    }

    public TiledMap getMap() {
        return map;
    }

    // world coords
    public void click(int button, float x, float y) {
        for (ClickHandler handler : clickHandlers)
            handler.onClick(button, x, y);
    }

    public void addClickHandler(ClickHandler handler) {
        clickHandlers.add(handler);
    }

    //    public float getMinVisibleX() {
//        return visibleArea.x;
//    }
//
//    public float getMaxVisibleX() {
//        return visibleArea.x + visibleArea.width;
//    }
//
//    public float getMinVisibleY() {
//        return visibleArea.y;
//    }
//
//    public float getMaxVisibleY() {
//        return visibleArea.y + visibleArea.height;
//    }

    private void detectCollisions() {
        while (tree.hasNext()) {
            final Iterator<GameObject> iterator = tree.next();
            final Iterator<GameObject> internalIterator = tree.internalIterator();
            while (iterator.hasNext()) {
                final GameObject gameObject = iterator.next();
                if (!gameObject.isActive())
                    continue;

                while (internalIterator.hasNext()) {
                    final GameObject internalGameObject = internalIterator.next();
                    if (!internalGameObject.isActive())
                        continue;

                    if (detectCollision(gameObject, internalGameObject)) {
                        gameObject.collide(internalGameObject);
                    }
                }
            }
        }
    }

    private boolean detectCollision(GameObject obj1, GameObject obj2) {
        return obj1.getBoundingRectangle().overlaps(obj2.getBoundingRectangle())
                && Intersector.intersectPolygons(obj1.getPolygon(), obj2.getPolygon(), null);
    }

    @Override
    public void dispose() {
        super.dispose();
        for (GameObject gameObject : gameObjects) {
            gameObject.dispose();
        }
    }
}
