package com.akhramovichsa.pong.GameObject;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.World;

/**
 *
 */
public class PaddleEnemy extends Rectangle {
    private Ball ball;

    public PaddleEnemy(World _world, ShapeRenderer _shapeRenderer, float _width, float _height, Ball _ball) {
        super(_world, _shapeRenderer, _width, _height);
        ball = _ball;
    }

    public void processMovement(float move_velocity, float ground_bottom_y, float ground_top_y) {
        float ball_y = ball.body.getPosition().y;
        float body_y = body.getPosition().y;

        /*
        // Управление через скорость
        if (ball_y < body_y + height/2f && body_y > ground_bottom_y) {
            body.setLinearVelocity(0f, -move_velocity); // Движение вниз
        } else if(ball_y > body_y + height/2f && body_y + height < ground_top_y) {
            body.setLinearVelocity(0f, move_velocity);  // Движение вверх

        } else {
            body.setLinearVelocity(0f, 0f);
        }
        */

        // Управление через положение
        // if (ball_y > )
        body.setTransform(0f, ball_y, 0f);
    }
}
