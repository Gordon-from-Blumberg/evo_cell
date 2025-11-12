package com.gordonfromblumberg.games.core.common.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import com.gordonfromblumberg.games.core.common.utils.Assets;
import com.gordonfromblumberg.games.core.common.utils.Poolable;

@SuppressWarnings("rawtypes")
public class GameObject implements Disposable, Poolable {
    protected static final int X1 = 0;
    protected static final int Y1 = 1;
    protected static final int X2 = 2;
    protected static final int Y2 = 3;
    protected static final int X3 = 4;
    protected static final int Y3 = 5;
    protected static final int X4 = 6;
    protected static final int Y4 = 7;

    protected int id;

    protected final Sprite sprite = new Sprite();
    public final Polygon polygon = createPolygon();
    public final Vector2 position = new Vector2();
    protected float width, height;

    protected boolean active = false;
    protected boolean colliding = false;

    protected boolean flippedX, flippedY;

    protected GameObject() {
    }

    public int getId() {
        return id;
    }

    public void update(float delta) {}

    public void render(Batch batch) {
        final Sprite sprite = this.sprite;
        final Polygon polygon = this.polygon;
        final float width = this.width;
        final float height = this.height;
        sprite.setRotation(polygon.getRotation());
        sprite.setBounds(polygon.getX() - width / 2, polygon.getY() - height / 2, width, height);
        sprite.setOriginCenter();
        sprite.draw(batch);
    }

    protected Polygon createPolygon() {
        return new Polygon(new float[8]);
    }

    public Rectangle getBoundingRectangle() {
        return polygon.getBoundingRectangle();
    }

    public Polygon getPolygon() {
        return polygon;
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
        polygon.setPosition(x, y);
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;

        updateVertices();
    }

    public void setRotation(float deg) {
        polygon.setRotation(deg);
    }

    protected void updateVertices() {
        final float width = this.width;
        final float height = this.height;

        // for correct work of Intersector vertices should be clockwise ordered
        final float[] vertices = polygon.getVertices();
        vertices[X1] = - width / 2;
        vertices[Y1] = - height / 2;
        vertices[X2] = - width / 2;
        vertices[Y2] = height / 2;
        vertices[X3] = width / 2;
        vertices[Y3] = height / 2;
        vertices[X4] = width / 2;
        vertices[Y4] = - height / 2;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    protected static TextureAtlas getTextureAtlas() {
        return Assets.manager().get("image/texture_pack.atlas", TextureAtlas.class);
    }

    public void setRegion(TextureRegion region) {
        sprite.setRegion(region);
    }

    public void setRegion(String name) {
        sprite.setRegion(getTextureAtlas().findRegion(name));
    }

    public void collide(GameObject other) {
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public Color getColor() {
        return sprite.getColor();
    }

    public void setColor(Color color) {
        sprite.setColor(color);
    }

    public Sprite getSprite() {
        return sprite;
    }

    // 0 - x, 1 - y, 2 - both x and y
    public boolean isFlipped(int axis) {
        return switch (axis) {
            case 0 -> flippedX;
            case 1 -> flippedY;
            case 2 -> flippedX && flippedY;
            default -> throw new IllegalArgumentException("Only 0, 1, 2 allowed, but " + axis + " is passed");
        };
    }

    public void flip(boolean x, boolean y) {
        sprite.flip(x != flippedX, y != flippedY);
        flippedX = x;
        flippedY = y;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "#" + id;
    }

    @Override
    public void reset() {
        id = -1;
        active = false;
        setPosition(0, 0);
        polygon.setRotation(0);
    }

    public void release() {}

    @Override
    public void dispose() {
    }
}
