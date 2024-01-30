package com.gordonfromblumberg.games.core.common.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.debug.DebugOptions;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.screens.UIRenderer;
import com.gordonfromblumberg.games.core.common.ui.UpdatableLabel;

import java.util.function.Supplier;

import static com.gordonfromblumberg.games.core.common.utils.StringUtils.floatToString;

public class WorldUIRenderer<T extends World> extends UIRenderer {
    private static final Logger log = LogManager.create(WorldUIRenderer.class);

    private final WorldCameraParams worldCameraParams = new WorldCameraParams();
    private final Supplier<Vector3> viewCoords;

    protected final T world;

    public WorldUIRenderer(SpriteBatch batch, T world, Supplier<Vector3> viewCoords) {
        super(batch);
        log.info("WorldUIRenderer constructor for " + getClass().getSimpleName());
        this.world = world;
        this.viewCoords = viewCoords;

        if (DebugOptions.DEBUG) {
            final AssetManager assets = Main.getInstance().assets();
            final Skin uiSkin = assets.get("ui/uiskin.json", Skin.class);
            Window debugWindow = createCoordsDebugWindow(uiSkin);
            debugWindow.setWidth(250);
            debugWindow.setY(viewport.getWorldHeight());
            stage.addActor(debugWindow);
            stage.addListener(new InputListener() {
                private final Window window = debugWindow;
                @Override
                public boolean keyUp(InputEvent event, int keycode) {
                    if (keycode == Input.Keys.F9) {
                        window.setVisible(!window.isVisible());
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    WorldCameraParams getWorldCameraParams() {
        return worldCameraParams;
    }

    private Window createCoordsDebugWindow(Skin skin) {
        final Window window = new Window("Coords debug", skin);
        window.add("Camera pos");
        window.add(new UpdatableLabel(skin, () ->
                floatToString(worldCameraParams.position.x, 2) + ", " + floatToString(worldCameraParams.position.y, 2)));

        window.row();
        window.add("Zoom");
        window.add(new UpdatableLabel(skin, () -> floatToString(worldCameraParams.zoom, 3)));

        window.row();
        window.add("Screen");
        window.add(new UpdatableLabel(skin, () -> Gdx.input.getX() + ", " + Gdx.input.getY()));

        window.row();
        window.add("Viewport");
        window.add(new UpdatableLabel(skin, () -> {
            Vector3 coords = viewCoords.get();
            return floatToString(coords.x, 2) + ", " + floatToString(coords.y, 2);
        }));

        window.row();
        window.add("World");
        window.add(new UpdatableLabel(skin,
                () -> floatToString(world.getMouseX(), 2) + ", " + floatToString(world.getMouseY(), 2)));
        return window;
    }
}
