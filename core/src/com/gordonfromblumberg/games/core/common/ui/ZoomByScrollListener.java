package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

public class ZoomByScrollListener extends InputListener {
    private final OrthographicCamera camera;
    private final float step;
    private final float minZoom;
    private final float maxZoom;

    public ZoomByScrollListener(OrthographicCamera camera, float step, float minZoom, float maxZoom) {
        this.camera = camera;
        this.step = step;
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
    }

    @Override
    public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
        if (amountY > 0)
            camera.zoom *= step;
        else if (amountY < 0)
            camera.zoom /= step;
        if (camera.zoom < minZoom)
            camera.zoom = minZoom;
        if (camera.zoom > maxZoom)
            camera.zoom = maxZoom;
        return true;
    }
}
