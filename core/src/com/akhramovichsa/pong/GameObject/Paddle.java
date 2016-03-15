package com.akhramovichsa.pong.GameObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

/**
 *
 */
public class Paddle extends Rectangle {
    public Paddle(World _world, ShapeRenderer _shapeRenderer, float _width, float _height) {
        super(_world, _shapeRenderer, _width, _height);
    }

    public void processMovement(float move_velocity, float min_y, float max_y) {
        float body_x = body.getPosition().x;
        float body_y = body.getPosition().y;

        // Управление клавишами
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) { // && body_y - height > 0) {
            body.setLinearVelocity(0f, -move_velocity);
        } else if(Gdx.input.isKeyPressed(Input.Keys.UP)) { // && body_y + height < height){
            body.setLinearVelocity(0f, move_velocity);
        } else {
            body.setLinearVelocity(0f, 0f);
        }

        // Управление тач скрином
        if (Gdx.input.isTouched()) {
            float touch_y = 90 - Gdx.input.getY()/(720/90);
            if (touch_y > min_y && touch_y +height < max_y) {
                body.setTransform(body_x, touch_y, 0f);
            }
            // Gdx.app.log("touched", Integer.toString(90 - Gdx.input.getY() / (720 / 90)));
        }

        // Ограничения
        if (body_y < min_y) {
            body.setTransform(body_x, min_y, 0f);
        } else if (body_y + height> max_y) {
            body.setTransform(body_x, max_y - height, 0f);

        }
    }
}
