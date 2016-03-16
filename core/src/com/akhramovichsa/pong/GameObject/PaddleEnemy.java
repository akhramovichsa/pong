package com.akhramovichsa.pong.GameObject;

import com.badlogic.gdx.Gdx;
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

    public void processMovement(float move_velocity, float ground_bottom_y, float ground_top_y, float world_width) {
        float ball_y = ball.body.getPosition().y;
        float ball_x = ball.body.getPosition().x;
        float ball_velocity_x = ball.body.getLinearVelocity().x;
        float body_x = body.getPosition().x;
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

        // Gdx.app.log("velocity", Float.toString(ball_velocity_x));
        // Управление через положение
        if (ball_x < world_width/2f && ball_velocity_x < 0) {
            if (ball_y - height / 2f > ground_bottom_y && ball_y + height / 2f < ground_top_y) {
                body.setTransform(body_x, ball_y - height / 2f, 0f);
            }
        }
    }
}
