package com.akhramovichsa.pong.GameObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import java.util.Random;

/**
 *
 */
public class Ball {
    private float radius;

    private ShapeRenderer shapeRenderer;

    private BodyDef    bodyDef;
    private FixtureDef fixtureDef;
    public Body       body;

    public Ball(World _world, ShapeRenderer _shapeRenderer, float _radius) {
        radius        = _radius;
        shapeRenderer = _shapeRenderer;
        createBody(_world);
    }

    private void createBody(World _world) {
        bodyDef = new BodyDef();

        bodyDef.type   = BodyDef.BodyType.DynamicBody;
        bodyDef.bullet = true;

        CircleShape shape = new CircleShape();
        shape.setRadius(radius);

        fixtureDef             = new FixtureDef();
        fixtureDef.shape       = shape;
        fixtureDef.density     = 1f;
        fixtureDef.friction    = 0f;
        fixtureDef.restitution = 1f;

        body = _world.createBody(bodyDef);

        body.createFixture(fixtureDef);

        shape.dispose();
    }

    /**
     * Запустить шарик
     * @param velocity скорость
     */
    public void pushBall(Vector2 velocity) {
        // Random random = new Random();
        // int velocity_rnd = random.nextInt((int)velocity_y);

        // velocity_y -= 2*velocity_rnd;
        // Gdx.app.log("random", new Integer((int)velocity_rnd).toString() );

        body.setLinearVelocity(velocity);
    }

    Matrix4 transform = new Matrix4();
    public void draw() {
        Vector2 pos = body.getWorldCenter();
        // float angle = body.getAngle();

        // transform.setToTranslation(pos.x, pos.y, 0);
        // transform.rotate(0, 0, 1, (float) Math.toDegrees(angle));

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.identity();
        shapeRenderer.setColor(Color.WHITE);
        // shapeRenderer.setTransformMatrix(transform);
        // shapeRenderer.translate((body.getPosition().x - width/2) * ppm, (body.getPosition().y - height/2) * ppm, 0f);
        // shapeRenderer.translate(body.getPosition().x * ppm, body.getPosition().y * ppm, 0f);

        // shapeRenderer.circle(pos.x, pos.y, radius, 4); //16);

        // Шарик сделаем квадратным
        shapeRenderer.rect(pos.x - radius, pos.y - radius, 2*radius, 2*radius);

        shapeRenderer.end();
    }
}
