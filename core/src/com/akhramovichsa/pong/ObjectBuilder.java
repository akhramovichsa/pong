package com.akhramovichsa.pong;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;

/**
 *
 */
public class ObjectBuilder {
    public String                                  node;     // Строковое обозначение фигуры
    Color                                          color;
    float                                          mass;
    Vector3                                        inertia;

    public Model                                   model;    // Модель отображения
    public ModelInstance                           instance; // Экземпляр
    public btCollisionShape                        collisionShape;
    public btCollisionObject                       collisionObject;
    public btRigidBody.btRigidBodyConstructionInfo rigidBodyConstructionInfo;
    public btRigidBody                             rigidBody;
    public btDefaultMotionState                    motionState;
    public ModelBuilder                            modelBuilder;

    public ObjectBuilder(ModelBuilder _modelBuilder) {
        modelBuilder = new ModelBuilder(); // _modelBuilder;
    }

    public void createBox(String _node, float width, float height, float depth, Color _color, float _mass) {
        node  = _node;
        color = _color;
        mass  = _mass;

        /*
        modelBuilder.node().id = node;

        modelBuilder.part(  node,
                            GL20.GL_TRIANGLES,
                            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                            new Material(ColorAttribute.createDiffuse(Color.GRAY))).box(width, height, depth);
        */

        model = modelBuilder.createBox( width, height, depth,
                                        new Material(ColorAttribute.createDiffuse(color)),
                                        VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);


        instance = new ModelInstance(model);

        collisionShape  = new btBoxShape(new Vector3(width, height, depth));
        collisionObject = new btCollisionObject();
        collisionObject.setCollisionShape(collisionShape);
        collisionObject.setWorldTransform(instance.transform);

        Vector3 inertia = new Vector3();

        if (mass > 0f) {
            collisionShape.calculateLocalInertia(mass, inertia);
        } else {
            inertia = Vector3.Zero;
        }

        motionState = new btDefaultMotionState(instance.transform);

        rigidBodyConstructionInfo = new btRigidBody.btRigidBodyConstructionInfo(1f, motionState, collisionShape, inertia);
        rigidBodyConstructionInfo.setRestitution(1.0f);
        // ballInfo.setFriction(1.0f);
        rigidBody = new btRigidBody(rigidBodyConstructionInfo);

        // rigidBody.applyCentralImpulse(new Vector3(0.0f, 5.0f, 0.0f));
        // dynWorld.addRigidBody(rigidBody);
    }

    public void createSphere(Model model){

    }
}
