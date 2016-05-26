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

    public void processMovement(float move_velocity, float min_x, float max_x, float world_width) {
        float ball_x = ball.body.getPosition().x;
        float ball_y = ball.body.getPosition().y;

        float ball_velocity_x = ball.body.getLinearVelocity().x;
        float ball_velocity_y = ball.body.getLinearVelocity().y;
        float body_x = body.getPosition().x;
        float body_y = body.getPosition().y;

        float delta_x = Math.abs(ball_x - body_x);
        float delta_y = Math.abs(ball_y - body_y);


        // Управлять только если шарик летит в сторону ракетки
        if (ball_velocity_y > 0) {
            // Коэфициент скорости в завсимости от полжения шарика и ракетки по оси x
            if      (delta_x < 1f)  { move_velocity *= 0.4f; }
            else if (delta_x < 8f)  { move_velocity *= 0.7f; }
            else if (delta_x < 12f) { move_velocity *= 1.0f; }

            // Gdx.app.log("delta_y",       new Float(delta_y).toString());
            // Gdx.app.log("move_velocity", new Float(move_velocity).toString());

            // Управление через скорость
            if (ball_x < body_x) {
                body.setLinearVelocity(-move_velocity, 0f); // Движение влево
            } else if (ball_x > body_x + width) {
                body.setLinearVelocity(move_velocity, 0f);  // Движение вправо
            } else {
                body.setLinearVelocity(0f, 0f);
            }
        } else {
            body.setLinearVelocity(0f, 0f);
        }



        // Gdx.app.log("velocity", Float.toString(ball_velocity_x));
        // Управление через положение
        /*
        if (ball_x < world_width/2f && ball_velocity_x < 0) {
            if (ball_y - height / 2f > ground_bottom_y && ball_y + height / 2f < ground_top_y) {
                body.setTransform(body_x, ball_y - height / 2f, 0f);
            }
        }
        */

        // Ограничения
        if (body_x < min_x) {
            body.setTransform(min_x, body_y, 0f);
        } else if (body_x + width > max_x) {
            body.setTransform(max_x - width, body_y, 0f);

        }
    }
}
