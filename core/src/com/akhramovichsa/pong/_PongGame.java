package com.akhramovichsa.pong;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.CollisionObjectWrapper;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionAlgorithm;
import com.badlogic.gdx.physics.bullet.collision.btCollisionAlgorithmConstructionInfo;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btDispatcherInfo;
import com.badlogic.gdx.physics.bullet.collision.btManifoldResult;
import com.badlogic.gdx.physics.bullet.collision.btSphereBoxCollisionAlgorithm;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.utils.Array;

// http://stackoverflow.com/questions/24429249/bouncing-ball-in-libgdx-using-bullet3d-physics

public class _PongGame extends ApplicationAdapter {
	public PerspectiveCamera cam;
	public Model model;
	public Array<ModelInstance> instances;
	public ModelInstance groundInstance;
	public ModelInstance ballInstance;
	public ModelBatch modelBatch;
	public Environment environment;
	public CameraInputController camController;

	boolean collision_flag;

	btCollisionShape  groundShape,  ballShape;
	btCollisionObject groundObject, ballObject;
	btRigidBody       groundBody,   ballBody;

	btCollisionConfiguration collisionConf;
	btDispatcher collisionDispatcher;
	btBroadphaseInterface broadphase;
	btDynamicsWorld dynWorld;
	btConstraintSolver solver;
	btRigidBody.btRigidBodyConstructionInfo groundInfo, ballInfo;

	private btDefaultMotionState ballMotionState;

	ObjectBuilder obj_ground;

	@Override
	public void create () {
		Bullet.init();
		modelBatch = new ModelBatch();

		//-------------------------------------------------------//
		//                       Окружение                       //
		//-------------------------------------------------------//
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		//-------------------------------------------------------//
		//                         Камера                        //
		//-------------------------------------------------------//
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(10f, 10f, 10f);
		cam.lookAt(0f, 0f, 0f);
		cam.near = 1f;
		cam.far = 300f;
		cam.update();

		camController = new CameraInputController(cam);
		Gdx.input.setInputProcessor(camController);

		//-------------------------------------------------------//
		//                     Модели объектов                   //
		//-------------------------------------------------------//
		model = createModel();


		ballInstance = new ModelInstance(model, "ball");
		ballInstance.transform.setToTranslation(0f, 5f, 0f);

		groundInstance = new ModelInstance(model, "ground");

		instances = new Array<ModelInstance>();
		instances.add(ballInstance);
		instances.add(groundInstance);

		//-------------------------------------------------------//
		//                 Объекты столкновения                  //
		//-------------------------------------------------------//
		groundShape  = new btBoxShape(new Vector3(5f, 0.05f, 5f));
		groundObject = new btCollisionObject();
		groundObject.setCollisionShape(groundShape);
		groundObject.setWorldTransform(groundInstance.transform);

		ballShape  = new btSphereShape(0.5f);
		ballObject = new btCollisionObject();
		ballObject.setCollisionShape(ballShape);
		ballObject.setWorldTransform(ballInstance.transform);

		ballMotionState = new btDefaultMotionState(ballInstance.transform);


		//-------------------------------------------------------//
		//                   Динамический мир                    //
		//-------------------------------------------------------//
		collisionConf       = new btDefaultCollisionConfiguration();
		collisionDispatcher = new btCollisionDispatcher(collisionConf);
		broadphase          = new btDbvtBroadphase();
		solver              = new btSequentialImpulseConstraintSolver();

		dynWorld = new btDiscreteDynamicsWorld(collisionDispatcher, broadphase, solver, collisionConf);
		dynWorld.setGravity(new Vector3(0f, -9.81f, 0f)); // Направление силы тяжести

		// contacter = new Contacter();

		//-------------------------------------------------------//
		//        Динамический мир - добавление объектов         //
		//-------------------------------------------------------//
		Vector3 groundInertia = new Vector3();
		// groundShape.calculateLocalInertia(0f, groundInertia);
		groundInertia.set(0f, 0f, 0f);
		groundInfo = new btRigidBody.btRigidBodyConstructionInfo(0f, null, groundShape, groundInertia);
		groundInfo.setRestitution(1.0f);
		//groundInfo.setFriction(1.0f);

		groundBody = new btRigidBody(groundInfo);
		dynWorld.addRigidBody(groundBody);

		Vector3 ballInertia = new Vector3();
		ballShape.calculateLocalInertia(10f, ballInertia);

		ballInfo = new btRigidBody.btRigidBodyConstructionInfo(1f, ballMotionState, ballShape, ballInertia);
		ballInfo.setRestitution(0.8f);
		// ballInfo.setFriction(1.0f);
		ballBody = new btRigidBody(ballInfo);

		ballBody.applyCentralImpulse(new Vector3(0.0f, 5.0f, 0.0f));
		dynWorld.addRigidBody(ballBody);

	}

	@Override
	public void render () {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(0.3f, 0.5f, 1f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		final float delta = Math.min(1f/30f, Gdx.graphics.getDeltaTime());

		dynWorld.stepSimulation(delta, 5, 1f/60f);
		ballMotionState.getWorldTransform(ballInstance.transform);

		camController.update();
		modelBatch.begin(cam);
		modelBatch.render(instances, environment);
		modelBatch.end();

		/* if(!collision_flag){
			ballInstance.transform.translate(0f, -delta, 0f);
			thingObject.setWorldTransform(ballInstance.transform);
			collision_flag = CheckCollision();
			*/
		}

	@Override
	public void dispose() {
		ballShape.dispose();
		groundShape.dispose();
		ballObject.dispose();
		groundObject.dispose();
		ballInfo.dispose();
		groundInfo.dispose();

		collisionConf.dispose();
		collisionDispatcher.dispose();
		modelBatch.dispose();
		model.dispose();
	}

	/**
	 * Создание объектов модели
	 * @return Model
	 */
	private Model createModel() {
		ModelBuilder modelBuilder = new ModelBuilder();

		modelBuilder.begin();
		modelBuilder.node().id = "ground";

		modelBuilder.part(  "ground",
				GL20.GL_TRIANGLES,
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
				new Material(ColorAttribute.createDiffuse(Color.GRAY))).box(10f, 0.1f, 10f);

		modelBuilder.node().id = "ball";
		modelBuilder.part(  "ball",
				GL20.GL_TRIANGLES,
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
				new Material(ColorAttribute.createDiffuse(Color.RED))).sphere(1f, 1f, 1f, 20, 20);


		// obj_ground = new ObjectBuilder(modelBuilder);
		// obj_ground.createBox("ground", 10f, 10f, 10f, Color.BLACK, 0f);
		// instances.add(obj_ground.instance);

		return modelBuilder.end();
	}

	private boolean CheckCollision() {
		CollisionObjectWrapper COWmodel  = new CollisionObjectWrapper(ballObject);
		CollisionObjectWrapper COWground = new CollisionObjectWrapper(groundObject);

		btCollisionAlgorithmConstructionInfo constInfo = new btCollisionAlgorithmConstructionInfo();
		constInfo.setDispatcher1(collisionDispatcher);

		btCollisionAlgorithm alg    = new btSphereBoxCollisionAlgorithm(null, constInfo, COWmodel.wrapper, COWground.wrapper, false);
		btDispatcherInfo     info   = new btDispatcherInfo();
		btManifoldResult     result = new btManifoldResult(COWmodel.wrapper, COWground.wrapper);

		alg.processCollision(COWmodel.wrapper, COWground.wrapper, info, result);
		boolean res = result.getPersistentManifold().getNumContacts() > 0;
		result.dispose();
		info.dispose();
		alg.dispose();
		constInfo.dispose();
		COWmodel.dispose();
		COWground.dispose();
		return res;
	}
}
