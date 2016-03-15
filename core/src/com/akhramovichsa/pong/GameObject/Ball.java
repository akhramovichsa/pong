package com.akhramovichsa.pong.GameObject;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

/**
 *
 */
public class Ball {
    private float radius;

    protected ShapeRenderer shapeRenderer;

    public BodyDef    bodyDef;
    public FixtureDef fixtureDef;
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

    Matrix4 transform = new Matrix4();
    public void draw() {
        Vector2 pos = body.getWorldCenter();
        // float angle = body.getAngle();

        // transform.setToTranslation(pos.x, pos.y, 0);
        // transform.rotate(0, 0, 1, (float) Math.toDegrees(angle));

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.identity();
        shapeRenderer.setColor(Color.WHITE);
        // shapeRenderer.setTransformMatrix(transform);
        // shapeRenderer.translate((body.getPosition().x - width/2) * ppm, (body.getPosition().y - height/2) * ppm, 0f);
        // shapeRenderer.translate(body.getPosition().x * ppm, body.getPosition().y * ppm, 0f);
        shapeRenderer.circle(pos.x, pos.y, radius, 16);
        shapeRenderer.end();
    }
}
