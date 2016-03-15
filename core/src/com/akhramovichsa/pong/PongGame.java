package com.akhramovichsa.pong;

import com.akhramovichsa.pong.GameObject.Ball;
import com.akhramovichsa.pong.GameObject.Paddle;
import com.akhramovichsa.pong.GameObject.PaddleEnemy;
import com.akhramovichsa.pong.GameObject.Rectangle;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.Viewport;


/**
 * @see * https://github.com/epes/libgdx-box2d-pong
 */
public class PongGame implements ApplicationListener {
	static final int PPM = 1; //(int)Math.pow(2, 2); // Pixels per metr

	// 1280x720 = 16:9
	static final int WORLD_WIDTH  = 160; // 320; // 1280 / PPM;
	static final int WORLD_HEIGHT = 90;  // 180; // 720  / PPM;

	// Верхняя граница, размеры и позиция
	static final float GROUND_TOP_SIZE_X     = WORLD_WIDTH;
	static final float GROUND_TOP_SIZE_Y     = WORLD_HEIGHT / 32f;
	static final float GROUND_TOP_POSITION_X = 0f;
	static final float GROUND_TOP_POSITION_Y = WORLD_HEIGHT - GROUND_TOP_SIZE_Y;

	// Нижняя граница, размеры и позиция
	static final float GROUND_BOTTOM_SIZE_X     = WORLD_WIDTH;
	static final float GROUND_BOTTOM_SIZE_Y     = WORLD_HEIGHT / 32f;
	static final float GROUND_BOTTOM_POSITION_X = 0f;
	static final float GROUND_BOTTOM_POSITION_Y = 0f;

	// Шар
	static final float BALL_RADIUS     = WORLD_WIDTH  / 128f;
	static final float BALL_POSITION_X = WORLD_WIDTH  / 2f;
	static final float BALL_POSITION_Y = WORLD_HEIGHT / 2f;
	static final float BALL_VELOCITY_X = WORLD_WIDTH  / 8f;
	static final float BALL_VELOCITY_Y = WORLD_HEIGHT / 4f;

	// Ракетка
	static final float PADDLE_SIZE_X     = WORLD_WIDTH  / 64f;
	static final float PADDLE_SIZE_Y     = WORLD_HEIGHT / 8f;
	static final float PADDLE_POSITION_X = WORLD_WIDTH - PADDLE_SIZE_X;        // Правая сторона
	static final float PADDLE_POSITION_Y = WORLD_HEIGHT/2f - PADDLE_SIZE_Y/2f; // Середина экрана

	// Ракетка, соперника
	static final float PADDLE_ENEMY_SIZE_X     = WORLD_WIDTH  / 64f;
	static final float PADDLE_ENEMY_SIZE_Y     = WORLD_HEIGHT / 8f;
	static final float PADDLE_ENEMY_POSITION_X = 0f;                                       // Левая сторона
	static final float PADDLE_ENEMY_POSITION_Y = WORLD_HEIGHT/2f - PADDLE_ENEMY_SIZE_Y/2f; // Середина экрана

	// Центральная линия
	static final float CENTER_LINE_POSITION_X = WORLD_WIDTH/2f - WORLD_WIDTH/256f;
	static final float CENTER_LINE_POSITION_Y = GROUND_BOTTOM_SIZE_Y + WORLD_HEIGHT/96f;
	static final float CENTER_LINE_SIZE_X     = WORLD_WIDTH/128f;
	static final float CENTER_LINE_SIZE_Y     = WORLD_WIDTH/64f;
	static final float CENTER_LINE_STEP_Y     = WORLD_WIDTH/32f;

	private SpriteBatch batch;
	private BitmapFont font;

	private Box2DDebugRenderer debugRenderer;
	private OrthographicCamera camera;
	private Viewport           viewport;
	private World              world;

	Rectangle   groundTop;
	Rectangle   groundBottom;
	Ball        ball;
	Paddle      paddle;
	PaddleEnemy paddleEnemy;

	protected ShapeRenderer shapeRenderer;

	// Сосотояние игры
	private enum State { PAUSE, RUN, RESUME, STOPPED }
	private State state          = State.RUN;
	private int paddleScore      = 0;
	private int paddleEnemyScore = 0;

	@Override
	public void create () {
		debugRenderer = new Box2DDebugRenderer(true, true, true, true, true, true);

		/*
		font          = new BitmapFont();
		font.setColor(Color.WHITE);
		// font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
		// font.getData().setScale(2);
		*/

		//-------------------------------------------------------//
		//                         Шрифт                         //
		//-------------------------------------------------------//
		// FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("desktop/assets/04b_24.ttf"));
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("desktop/assets/04b_03b.ttf"));
		// FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("desktop/assets/04b_08.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.size = WORLD_WIDTH / 10;
		font = generator.generateFont(parameter);
		generator.dispose();


		//-------------------------------------------------------//
		//                         Камера                        //
		//-------------------------------------------------------//
		camera = new OrthographicCamera(); //100, 100 * (Gdx.graphics.getHeight() / Gdx.graphics.getWidth()));
		camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
		// camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0f); // Начало координат в центре
		// camera.position.set(0f, 0f, 0f);  // Начало координат в левом нижнем углу

		//viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
		//viewport.apply();
		camera.update();

		batch = new SpriteBatch();
		batch.setProjectionMatrix(camera.combined);

		world = new World(new Vector2(0f, 0f), true);

		shapeRenderer = new ShapeRenderer();
		shapeRenderer.setProjectionMatrix(camera.combined);

		//-------------------------------------------------------//
		//                        Объекты                        //
		//-------------------------------------------------------//
		createObjects();
	}


	@Override
	public void render() {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();

		// world.step(Gdx.app.getGraphics().getDeltaTime(), 4, 4);
		world.step(1/60f, 4, 4);
		paddle.processMovement(50f, GROUND_BOTTOM_SIZE_Y, GROUND_TOP_POSITION_Y);
		paddleEnemy.processMovement(100f, GROUND_BOTTOM_SIZE_Y, GROUND_TOP_POSITION_Y); // - GROUND_TOP_SIZE_Y);

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
			/*
			// 0, 0
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			shapeRenderer.identity();
			shapeRenderer.setColor(Color.RED);
			//shapeRenderer.translate(0f, 0f, 0f);
			shapeRenderer.rect(0f, 0f, 10f, 10f);
			shapeRenderer.setColor(Color.GREEN);
			// shapeRenderer.translate(120f, 710f, 0f);
			shapeRenderer.rect(WORLD_WIDTH - 10f, WORLD_HEIGHT - 10f, 10f, 10f);
			shapeRenderer.end();
			*/

			groundTop.draw();
			groundBottom.draw();
			paddle.draw();
			paddleEnemy.draw();
			ball.draw();

		batch.end();

		drawScore(0, 0);
		drawCenterLine();

		// debugRenderer.render(world, camera.combined);
	}

	@Override
	public void dispose () {
		debugRenderer.dispose();
		world.dispose();
		batch.dispose();
		font.dispose();
	}

	/**
	 * Создание игрового мира
	 */
	private void createObjects() {
		// Верхняя граница
		groundTop = new Rectangle(world, shapeRenderer, GROUND_TOP_SIZE_X, GROUND_TOP_SIZE_Y);
		groundTop.body.setTransform(GROUND_TOP_POSITION_X, GROUND_TOP_POSITION_Y, 0f);

		// Нижняя ганица
		groundBottom = new Rectangle(world, shapeRenderer, GROUND_BOTTOM_SIZE_X, GROUND_BOTTOM_SIZE_Y);
		groundBottom.body.setTransform(GROUND_BOTTOM_POSITION_X, GROUND_BOTTOM_POSITION_Y, 0f);

		// Шарик
		ball = new Ball(world, shapeRenderer, BALL_RADIUS);
		ball.body.setTransform(BALL_POSITION_X, BALL_POSITION_Y, 0f);
		ball.body.setLinearVelocity(BALL_VELOCITY_X*1000, BALL_VELOCITY_Y*1000);

		// Ракетка
		paddle = new Paddle(world, shapeRenderer, PADDLE_SIZE_X, PADDLE_SIZE_Y);
		paddle.body.setTransform(PADDLE_POSITION_X, PADDLE_POSITION_Y, 0f);

		// Ракетка соперника
		paddleEnemy = new PaddleEnemy(world, shapeRenderer, PADDLE_ENEMY_SIZE_X, PADDLE_ENEMY_SIZE_Y, ball);
		paddleEnemy.body.setTransform(PADDLE_ENEMY_POSITION_X, PADDLE_ENEMY_POSITION_Y, 0f);
	}

	private void drawScore(int score_1, int score_2) {
		batch.begin();
		// shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		// shapeRenderer.setColor(Color.GREEN);
		// font.draw(batch, "PLAY", 10, 10);
		font.draw(batch, Integer.toString(paddleScore),      WORLD_WIDTH/2f - WORLD_WIDTH/12f, WORLD_HEIGHT - WORLD_HEIGHT/16f);
		font.draw(batch, Integer.toString(paddleEnemyScore), WORLD_WIDTH/2f + WORLD_WIDTH/32f, WORLD_HEIGHT - WORLD_HEIGHT/16f);
		// shapeRenderer.end();
		batch.end();
	}

	private void drawCenterLine() {
		batch.begin();
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(Color.WHITE);

		float pos_y = CENTER_LINE_POSITION_Y;
		for (int i = 0; i < 17; i++) {
			shapeRenderer.rect(CENTER_LINE_POSITION_X, pos_y, CENTER_LINE_SIZE_X, CENTER_LINE_SIZE_Y);
			pos_y += CENTER_LINE_STEP_Y;
		}

		// shapeRenderer.line(WORLD_WIDTH/2f, 0f, WORLD_WIDTH/2f, WORLD_HEIGHT);

		shapeRenderer.end();
		batch.end();
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