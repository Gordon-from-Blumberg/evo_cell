package com.gordonfromblumberg.games.core.common.model;

import com.badlogic.gdx.math.Vector2;
import com.gordonfromblumberg.games.core.common.physics.MovingStrategy;

public class PhysicsGameObject extends GameObject {
    public final Vector2 velocity = new Vector2();
    public final Vector2 acceleration = new Vector2();
    // x - rotation, y - angle velocity
    public final Vector2 rotation = new Vector2();

    protected MovingStrategy movingStrategy;

    {
        this.colliding = true;
    }

    public PhysicsGameObject() {}

    @Override
    public void update(final float delta) {
        super.update(delta);

        if (movingStrategy != null)
            movingStrategy.update(position, velocity, acceleration, rotation, delta);

        setRotation(rotation.x);

        adjustPosition();
        polygon.setPosition(position.x, position.y);
    }

    /**
     * Checks whether position out of limit bounds and handles such case
     */
    protected void adjustPosition() {}

    public void setMovingStrategy(MovingStrategy movingStrategy) {
        this.movingStrategy = movingStrategy;
    }

    @Override
    public void reset() {
        super.reset();

        this.velocity.setZero();
        this.acceleration.setZero();
        this.rotation.setZero();
        this.movingStrategy = null;
    }
}
