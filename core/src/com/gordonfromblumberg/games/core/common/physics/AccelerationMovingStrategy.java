package com.gordonfromblumberg.games.core.common.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

public class AccelerationMovingStrategy implements MovingStrategy {
    private static final Vector2 temp = new Vector2();

    protected float maxVelocity, maxVelocity2;
    protected float maxAcceleration, maxAcceleration2;
    protected float friction = 0f;
    protected boolean turnInVelocityDirection = true;

    public AccelerationMovingStrategy() {}

    public AccelerationMovingStrategy(float maxVelocity, float maxAcceleration) {
        setMaxVelocity(maxVelocity);
        setMaxAcceleration(maxAcceleration);
    }

    @Override
    public void update(Vector2 position, Vector2 velocity, Vector2 acceleration, Vector2 rotation, float dt) {
//        Gdx.app.log("Acceleration", "Before limit " + acceleration.len());
        limitAcceleration(velocity, acceleration);
//        Gdx.app.log("Acceleration", "After limit " + acceleration.len());

        updateVelocity(velocity, acceleration, rotation, dt);

        position.mulAdd(velocity, dt);
//        Gdx.app.log("Position", position + ", mag = " + position.len());
//        Gdx.app.log("Velocity", velocity + ", mag = " + velocity.len());
//        Gdx.app.log("Acceleration", acceleration + ", mag = " + acceleration.len());

        rotate(velocity, rotation, dt);

        temp.set(velocity).scl(-friction * dt);
        velocity.add(temp);
//        Gdx.app.log("Velocity", "After friction " + velocity.len());
    }

    public void setMaxVelocity(float maxVelocity) {
        this.maxVelocity = maxVelocity;
        this.maxVelocity2 = maxVelocity * maxVelocity;
    }

    public void setMaxAcceleration(float maxAcceleration) {
        this.maxAcceleration = maxAcceleration;
        this.maxAcceleration2 = maxAcceleration * maxAcceleration;
    }

    public void setFriction(float friction) {
        this.friction = friction;
    }

    public void setTurnInVelocityDirection(boolean value) {
        this.turnInVelocityDirection = value;
    }

    protected void updateVelocity(Vector2 velocity, Vector2 acceleration, Vector2 rotation, float dt) {
        velocity.mulAdd(acceleration, dt);
        if (maxVelocity2 > 0)
            velocity.limit2(maxVelocity2);
    }

    protected void limitAcceleration(Vector2 velocity, Vector2 acceleration) {
        if (maxAcceleration2 > 0) {
            acceleration.limit2(maxAcceleration2);
        }
    }

    protected void rotate(Vector2 velocity, Vector2 rotation, float dt) {
        if (turnInVelocityDirection)
            // vector 0 deg - right
            // sprite / polygon 0 deg - top
            rotation.x = velocity.angleDeg() - 90;
        else
            rotation.x += rotation.y * dt;
    }
}
