package com.akhramovichsa.pong;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Disposable;

/**
 * @see https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part2/
 * https://github.com/epes/libgdx-box2d-pong
 */
public class __PongGame implements ApplicationListener {
	final static short GROUND_FLAG = 1 << 8;
	final static short OBJECT_FLAG = 1 << 9;
	final static short ALL_FLAG    = -1;

	class MyContactListener extends ContactListener {
		@Override
		public boolean onContactAdded(int userValue0, int partId0, int index0, boolean match0,
									  int userValue1, int partId1, int index1, boolean match1) {
			/*
			Vector3 vel   = instances.get(4).body.getLinearVelocity();
			float vel_len = vel.len();
			if (vel_len < 280) {
				float delta_vel = 280 - vel_len;

				instances.get(4).body.setLinearVelocity(new Vector3(vel.x + 280/delta_vel, vel.y + 280/delta_vel, 0f));
			}
			*/
			// instances.get(4).body.setLinearVelocity(new Vector3(400f, 0f, 0f));
			Gdx.app.log("onContact", instances.get(4).body.getLinearVelocity().toString());
			Gdx.app.log("onContact", Float.toString(instances.get(4).body.getLinearVelocity().len()));
			/*if (match0)
				((ColorAttribute)instances.get(userValue0).materials.get(0).get(ColorAttribute.Diffuse)).color.set(Color.WHITE);
			if (match1)
				((ColorAttribute)instances.get(userValue1).materials.get(0).get(ColorAttribute.Diffuse)).color.set(Color.WHITE);*/
			return true;
		}
	}

	static class MyMotionState extends btMotionState {
		Matrix4 transform;

		@Override
		public void getWorldTransform(Matrix4 worldTrans) {
			worldTrans.set(transform);
		}

		@Override
		public void setWorldTransform(Matrix4 worldTrans) {
			transform.set(worldTrans);
		}
	}

	static class GameObject extends ModelInstance implements Disposable {
		public final btRigidBody   body;
		public final MyMotionState motionState;

		public GameObject(Model model, String node, btRigidBody.btRigidBodyConstructionInfo constructionInfo) {
			super(model, node);
			motionState = new MyMotionState();
			motionState.transform = transform;
			// constructionInfo.setLinearDamping(0f);
			// constructionInfo.setAngularDamping(0f);
			// constructionInfo.setRestitution(1.0f);
			// constructionInfo.setFriction(0.0f);
			body = new btRigidBody(constructionInfo);
			body.setMotionState(motionState);
		}

		@Override
		public void dispose() {
			body.dispose();
			motionState.dispose();
		}

		static class Constructor implements Disposable {
			public final Model            model;
			public final String           node;
			public final btCollisionShape shape;
			public final btRigidBody.btRigidBodyConstructionInfo constructionInfo;

			private static Vector3 localInertia = new Vector3();

			public Constructor(Model model, String node, btCollisionShape shape, float mass) {
				this.model = model;
				this.node  = node;
				this.shape = shape;
				if (mass > 0f)
					shape.calculateLocalInertia(mass, localInertia);
				else
					localInertia.set(0, 0, 0);
				this.constructionInfo = new btRigidBody.btRigidBodyConstructionInfo(mass, null, shape, localInertia);
			}

			public GameObject construct () {
				return new GameObject(model, node, constructionInfo);
			}

			@Override
			public void dispose () {
				shape.dispose();
				constructionInfo.dispose();
			}
		}
	}

	static final int WORLD_WIDTH  = 1280;
	static final int WORLD_HEIGHT = 720;

	// Нижняя граница, размеры и позиция
	static final float GROUND_BOTTOM_SIZE_X     = WORLD_WIDTH;
	static final float GROUND_BOTTOM_SIZE_Y     = 10f;
	static final float GROUND_BOTTOM_SIZE_Z     = 10f;
	static final float GROUND_BOTTOM_POSITION_X = 0f;
	static final float GROUND_BOTTOM_POSITION_Y = -WORLD_HEIGHT / 2f;
	static final float GROUND_BOTTOM_POSITION_Z = 0f;

	// Верхняя граница, размеры и позиция
	static final float GROUND_TOP_SIZE_X     = WORLD_WIDTH;
	static final float GROUND_TOP_SIZE_Y     = 10f;
	static final float GROUND_TOP_SIZE_Z     = 10f;
	static final float GROUND_TOP_POSITION_X = 0f;
	static final float GROUND_TOP_POSITION_Y = WORLD_HEIGHT / 2f;
	static final float GROUND_TOP_POSITION_Z = 0f;

	// Шар
	static final float SPHERE_RADIUS = 100f;

	// Ракетка
	static final float PADDLE_SIZE_X     = 10f;
	static final float PADDLE_SIZE_Y     = WORLD_HEIGHT / 10f;
	static final float PADDLE_SIZE_Z     = 10f;
	static final float PADDLE_POSITION_X = -WORLD_WIDTH / 2f;
	static final float PADDLE_POSITION_Y = 0f;
	static final float PADDLE_POSITION_Z = 0f;

	// Ракетка, соперника
	static final float PADDLE_ENEMY_SIZE_X     = 10f;
	static final float PADDLE_ENEMY_SIZE_Y     = WORLD_HEIGHT / 10f;
	static final float PADDLE_ENEMY_SIZE_Z     = 10f;
	static final float PADDLE_ENEMY_POSITION_X = WORLD_WIDTH / 2f;
	static final float PADDLE_ENEMY_POSITION_Y = 0f;
	static final float PADDLE_ENEMY_POSITION_Z = 0f;

	private PerspectiveCamera     cam_p;
	private OrthographicCamera    cam;
	private CameraInputController camController;
	private ModelBatch            modelBatch;
	private Environment           environment;
	private Model                 model;
	private Array<GameObject>     instances;
	private ArrayMap<String, GameObject.Constructor> constructors;
	private float spawnTimer;
	private boolean is_perspective = false;

	// Сосотояние игры
	private enum State { PAUSE, RUN, RESUME, STOPPED }
	private State state          = State.RUN;
	private int paddleScore      = 0;
	private int paddleEnemyScore = 0;

	private btCollisionConfiguration collisionConfig;
	private btDispatcher             dispatcher;
	private MyContactListener        contactListener;
	private btBroadphaseInterface    broadphase;
	private btDynamicsWorld          dynamicsWorld;
	private btConstraintSolver       constraintSolver;

	@Override
	public void create () {
		Bullet.init();
		modelBatch  = new ModelBatch();

		//-------------------------------------------------------//
		//                       Окружение                       //
		//-------------------------------------------------------//
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		//-------------------------------------------------------//
		//                         Камера                        //
		//-------------------------------------------------------//
		if (is_perspective) {
			cam_p = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			cam_p.position.set(3f, 7f, 10f);
			cam_p.lookAt(0, 4f, 0);
			cam_p.near = 1f;
			cam_p.far  = WORLD_WIDTH;
			cam_p.update();
			camController = new CameraInputController(cam_p);
		} else {
			cam = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT * (Gdx.graphics.getWidth() / Gdx.graphics.getHeight()));
			// cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 10f);
			// cam.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			cam.translate(0f, 0f, WORLD_WIDTH / 2f);
			cam.lookAt(0f, 0f, 0f);
			// cam.near = 1f;
			cam.far = WORLD_WIDTH;
			cam.update();
			camController = new CameraInputController(cam);
		}


		Gdx.input.setInputProcessor(camController);

		//-------------------------------------------------------//
		//                     Модели объектов                   //
		//-------------------------------------------------------//
		model = createModel();

		constructors = new ArrayMap<String, GameObject.Constructor>(String.class, GameObject.Constructor.class);
		constructors.put("sphere",        new GameObject.Constructor(model, "sphere",        new btSphereShape(SPHERE_RADIUS/2f), 1f));
		constructors.put("ground_bottom", new GameObject.Constructor(model, "ground_bottom", new btBoxShape(new Vector3(GROUND_BOTTOM_SIZE_X/2f, GROUND_BOTTOM_SIZE_Y/2f, GROUND_BOTTOM_SIZE_Z/2f)), 0f));
		constructors.put("ground_top",    new GameObject.Constructor(model, "ground_top",    new btBoxShape(new Vector3(GROUND_TOP_SIZE_X/2f,    GROUND_TOP_SIZE_Y/2f,    GROUND_TOP_SIZE_Z/2f)),    0f));
		constructors.put("paddle",        new GameObject.Constructor(model, "paddle",        new btBoxShape(new Vector3(PADDLE_SIZE_X/2f,        PADDLE_SIZE_Y/2f,        PADDLE_SIZE_Z/2f)),        0f));
		constructors.put("paddle_enemy",  new GameObject.Constructor(model, "paddle_enemy",  new btBoxShape(new Vector3(PADDLE_ENEMY_SIZE_X/2f,  PADDLE_ENEMY_SIZE_Y/2f,  PADDLE_ENEMY_SIZE_Z/2f)),  0f));
		// constructors.put("box", new GameObject.Constructor(model, "box", new btBoxShape(new Vector3(0.5f, 0.5f, 0.5f)), 1f));
		// constructors.put("cone", new GameObject.Constructor(model, "cone", new btConeShape(0.5f, 2f), 1f));
		// constructors.put("capsule", new GameObject.Constructor(model, "capsule", new btCapsuleShape(.5f, 1f), 1f));
		// constructors.put("cylinder", new GameObject.Constructor(model, "cylinder", new btCylinderShape(new Vector3(.5f, 1f, .5f)), 1f));

		collisionConfig  = new btDefaultCollisionConfiguration();
		dispatcher       = new btCollisionDispatcher(collisionConfig);
		broadphase       = new btDbvtBroadphase();
		constraintSolver = new btSequentialImpulseConstraintSolver();
		dynamicsWorld    = new btDiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfig);
		// dynamicsWorld.setGravity(new Vector3(0f, -9.81f, 0));
		dynamicsWorld.setGravity(new Vector3(0f, 0f, 0f));
		contactListener = new MyContactListener();

		instances = new Array<GameObject>();

		// Нижняя граница
		GameObject object_ground_bottom = constructors.get("ground_bottom").construct();
		object_ground_bottom.body.setCollisionFlags(object_ground_bottom.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
		object_ground_bottom.transform.trn(GROUND_BOTTOM_POSITION_X, GROUND_BOTTOM_POSITION_Y, GROUND_BOTTOM_POSITION_Z);
		object_ground_bottom.body.setFriction(0.0f);    // Трение
		object_ground_bottom.body.setRestitution(1.0f); // Упругость
		object_ground_bottom.body.setContactCallbackFlag(GROUND_FLAG);
		object_ground_bottom.body.setContactCallbackFilter(0);
		object_ground_bottom.body.setActivationState(Collision.DISABLE_DEACTIVATION);
		instances.add(object_ground_bottom);
		dynamicsWorld.addRigidBody(object_ground_bottom.body);

		// Верхняя граница
		GameObject object_ground_top = constructors.get("ground_top").construct();
		object_ground_top.body.setCollisionFlags(object_ground_bottom.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
		object_ground_top.transform.trn(GROUND_TOP_POSITION_X, GROUND_TOP_POSITION_Y, GROUND_TOP_POSITION_Z);
		object_ground_top.body.setFriction(0.0f);    // Трение
		object_ground_top.body.setRestitution(1.0f); // Упругость
		object_ground_top.body.setContactCallbackFlag(GROUND_FLAG);
		object_ground_top.body.setContactCallbackFilter(0);
		object_ground_top.body.setActivationState(Collision.DISABLE_DEACTIVATION);
		instances.add(object_ground_top);
		dynamicsWorld.addRigidBody(object_ground_top.body);

		// Ракетка
		GameObject object_paddle = constructors.get("paddle").construct();
		object_paddle.body.setCollisionFlags(object_ground_bottom.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
		object_paddle.transform.trn(PADDLE_POSITION_X, PADDLE_POSITION_Y, PADDLE_POSITION_Z);
		object_paddle.body.setFriction(0.0f);    // Трение
		object_paddle.body.setRestitution(1.0f); // Упругость
		object_paddle.body.setContactCallbackFlag(GROUND_FLAG);
		object_paddle.body.setContactCallbackFilter(0);
		object_paddle.body.setActivationState(Collision.DISABLE_DEACTIVATION);
		instances.add(object_paddle);
		dynamicsWorld.addRigidBody(object_paddle.body);

		// Ракетка соперника
		GameObject object_paddle_enemy = constructors.get("paddle_enemy").construct();
		object_paddle_enemy.body.setCollisionFlags(object_ground_bottom.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
		object_paddle_enemy.transform.trn(PADDLE_ENEMY_POSITION_X, PADDLE_ENEMY_POSITION_Y, PADDLE_ENEMY_POSITION_Z);
		object_paddle_enemy.body.setFriction(0.0f);    // Трение
		object_paddle_enemy.body.setRestitution(1.0f); // Упругость
		object_paddle_enemy.body.setContactCallbackFlag(GROUND_FLAG);
		object_paddle_enemy.body.setContactCallbackFilter(0);
		object_paddle_enemy.body.setActivationState(Collision.DISABLE_DEACTIVATION);
		instances.add(object_paddle_enemy);
		dynamicsWorld.addRigidBody(object_paddle_enemy.body);
	}

	public void spawn() {
		// GameObject obj = constructors.values[1 + MathUtils.random(constructors.size - 2)].construct();
		GameObject obj = constructors.values[0].construct();
		// obj.transform.setFromEulerAngles(MathUtils.random(360f), MathUtils.random(360f), MathUtils.random(360f));
		obj.body.setFriction(0.0f);    // Трение
		obj.body.setRestitution(1.0f); // Упругость
		// obj.body.setDamping(0f, 0f);// constructionInfo.setLinearDamping(0.0f);
		// obj.body.setAngularDamping(0.0f);
		// obj.transform.trn(MathUtils.random(-2.5f, 2.5f), 9f, MathUtils.random(-2.5f, 2.5f));
		obj.transform.trn(0f, 0f, 0f);

		obj.body.setLinearVelocity(new Vector3(200f, 200f, 0f)); // Начальная скорость
		// obj.body.applyCentralImpulse(new Vector3(200f, 200f, 0f));
		// obj.body.applyCentralForce(new Vector3(200f, 200f, 0f));

		obj.body.setLinearFactor(new Vector3(1, 1, 0)); // Ограничение движения только в плоскости x-y
		// obj.body.setAngularFactor(new Vector3(1, 1, 0)); // Ограничение углового движения вокруг осей

		obj.body.proceedToTransform(obj.transform);
		obj.body.setUserValue(instances.size);
		obj.body.setCollisionFlags(obj.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
		instances.add(obj);
		dynamicsWorld.addRigidBody(obj.body);
		obj.body.setContactCallbackFlag(OBJECT_FLAG);
		obj.body.setContactCallbackFilter(GROUND_FLAG);
	}

	float angle, speed = 90f;

	@Override
	public void render () {
		final float delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());

		// Новая игра
		if (state == State.RUN) {
			paddleScore      = 0;
			paddleEnemyScore = 0;
			state            = State.RESUME;

			spawn();
		}

		// Процесс игры
		if (state == State.RESUME) {
			/*
			Vector3 vel   = instances.get(0).body.getLinearVelocity();
			float vel_len = vel.len();
			if (vel_len < 200) {
				float delta_vel = 200f - vel_len;
				instances.get(0).body.setLinearVelocity(new Vector3(vel.x + vel.x*delta_vel, vel.y + vel.y*delta_vel, vel.z + vel.z*delta_vel));
			}
			*/
			dynamicsWorld.stepSimulation(delta, 5, 1f / 60f);
			/*
			if ((spawnTimer -= delta) < 0) {
				spawn();
				spawnTimer = 1.5f;
			}
			*/
		}

		camController.update();

		Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		if (is_perspective) {
			modelBatch.begin(cam_p);
		} else {
			modelBatch.begin(cam);
		}

		modelBatch.render(instances, environment);
		modelBatch.end();

		//-------------------------------------------------------//
		//                Управление стрелками                   //
		//-------------------------------------------------------//
		Vector3 paddle_position = new Vector3();
		instances.get(3).transform.getTranslation(paddle_position);

		float paddle_max_position = (WORLD_HEIGHT/2f - PADDLE_SIZE_Y/2f);
		float paddle_min_position = -paddle_max_position;
		float paddle_speed        = delta*400f;

		if (Gdx.input.isKeyPressed(Input.Keys.UP) && paddle_position.y < paddle_max_position) {
			instances.get(3).transform.translate(0, paddle_speed, 0); // setTranslation(0, 1f, 0f);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && paddle_position.y > paddle_min_position) {
			instances.get(3).transform.translate(0, -paddle_speed, 0); // setTranslation(0, 1f, 0f);
		}

		// Пауза игры
		if (Gdx.input.isKeyPressed(Input.Keys.P)) {
			if (state == State.PAUSE) {
				state = State.RESUME;
			}
			else if (state == State.RUN || state == State.RESUME) {
				state = State.PAUSE;
			}
		}
	}

	@Override
	public void dispose () {
		for (GameObject obj : instances)
			obj.dispose();
		instances.clear();

		for (GameObject.Constructor ctor : constructors.values())
			ctor.dispose();
		constructors.clear();

		dynamicsWorld.dispose();
		constraintSolver.dispose();
		broadphase.dispose();
		dispatcher.dispose();
		collisionConfig.dispose();

		contactListener.dispose();

		modelBatch.dispose();
		model.dispose();
	}

	/**
	 * Создание моделей объектов
	 * @return Model
	 */
	private Model createModel() {
		ModelBuilder mb = new ModelBuilder();
		mb.begin();

		mb.node().id = "sphere";
		mb.part("sphere",
				GL20.GL_TRIANGLES,
				Usage.Position | Usage.Normal,
				new Material(ColorAttribute.createDiffuse(Color.GREEN)))
				.sphere(SPHERE_RADIUS, SPHERE_RADIUS, SPHERE_RADIUS, 30, 30);

		mb.node().id = "ground_bottom";
		mb.part("ground_bottom",
				GL20.GL_TRIANGLES,
				Usage.Position | Usage.Normal,
				new Material(ColorAttribute.createDiffuse(Color.RED)))
				.box(GROUND_BOTTOM_SIZE_X, GROUND_BOTTOM_SIZE_Y, GROUND_BOTTOM_SIZE_Z);

		mb.node().id = "ground_top";
		mb.part("ground_top",
				GL20.GL_TRIANGLES,
				Usage.Position | Usage.Normal,
				new Material(ColorAttribute.createDiffuse(Color.RED)))
				.box(GROUND_TOP_SIZE_X, GROUND_TOP_SIZE_Y, GROUND_TOP_SIZE_Z);

		mb.node().id = "paddle";
		mb.part("paddle",
				GL20.GL_TRIANGLES,
				Usage.Position | Usage.Normal,
				new Material(ColorAttribute.createDiffuse(Color.RED)))
				.box(PADDLE_SIZE_X, PADDLE_SIZE_Y, PADDLE_SIZE_Z);

		mb.node().id = "paddle_enemy";
		mb.part("paddle_enemy",
				GL20.GL_TRIANGLES,
				Usage.Position | Usage.Normal,
				new Material(ColorAttribute.createDiffuse(Color.RED)))
				.box(PADDLE_ENEMY_SIZE_X, PADDLE_ENEMY_SIZE_Y, PADDLE_ENEMY_SIZE_Z);

		/*
		mb.node().id = "box";
		mb.part("box", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.BLUE)))
				.box(1f, 1f, 1f);
		mb.node().id = "cone";
		mb.part("cone", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.YELLOW)))
				.cone(1f, 2f, 1f, 10);
		mb.node().id = "capsule";
		mb.part("capsule", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.CYAN)))
				.capsule(0.5f, 2f, 10);
		mb.node().id = "cylinder";
		mb.part("cylinder", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal,
				new Material(ColorAttribute.createDiffuse(Color.MAGENTA))).cylinder(1f, 2f, 1f, 10);
		*/
		return mb.end();
	}

	@Override
	public void pause () {
	}

	@Override
	public void resume () {
	}

	@Override
	public void resize (int width, int height) {
	}
}