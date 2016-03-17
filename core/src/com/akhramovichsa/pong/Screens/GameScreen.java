package com.akhramovichsa.pong.Screens;

import com.akhramovichsa.pong.GameObject.Ball;
import com.akhramovichsa.pong.GameObject.Paddle;
import com.akhramovichsa.pong.GameObject.PaddleEnemy;
import com.akhramovichsa.pong.GameObject.Rectangle;
import com.akhramovichsa.pong.PongGame;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 *
 */
public class GameScreen implements Screen {
    private Game game;

    static final int WORLD_WIDTH  = PongGame.WORLD_WIDTH;
    static final int WORLD_HEIGHT = PongGame.WORLD_HEIGHT;

    // Верхняя граница, размеры и позиция
    static final float GROUND_TOP_SIZE_X     = WORLD_WIDTH;
    static final float GROUND_TOP_SIZE_Y     = WORLD_HEIGHT / 32f;
    static final float GROUND_TOP_POSITION_X = 0f;
    static final float GROUND_TOP_POSITION_Y = WORLD_HEIGHT; // WORLD_HEIGHT - GROUND_TOP_SIZE_Y;

    // Нижняя граница, размеры и позиция
    static final float GROUND_BOTTOM_SIZE_X     = WORLD_WIDTH;
    static final float GROUND_BOTTOM_SIZE_Y     = WORLD_HEIGHT / 32f;
    static final float GROUND_BOTTOM_POSITION_X = 0f;
    static final float GROUND_BOTTOM_POSITION_Y = -GROUND_BOTTOM_SIZE_Y; // 0f;

    // Шар
    static final float BALL_RADIUS     = WORLD_WIDTH  / 128f;
    static final float BALL_POSITION_X = WORLD_WIDTH  / 2f;
    static final float BALL_POSITION_Y = WORLD_HEIGHT / 2f;
    static final float BALL_VELOCITY_X = WORLD_WIDTH  / 8f;
    static final float BALL_VELOCITY_Y = WORLD_HEIGHT / 4f;

    // Ракетка
    static final float PADDLE_SIZE_X     = WORLD_WIDTH  / 64f;
    static final float PADDLE_SIZE_Y     = WORLD_HEIGHT / 10f;
    static final float PADDLE_POSITION_X = WORLD_WIDTH - PADDLE_SIZE_X - WORLD_WIDTH/16f;  // Правая сторона
    static final float PADDLE_POSITION_Y = WORLD_HEIGHT/2f - PADDLE_SIZE_Y/2f; // Середина экрана

    // Ракетка, соперника
    static final float PADDLE_ENEMY_SIZE_X     = WORLD_WIDTH  / 64f;
    static final float PADDLE_ENEMY_SIZE_Y     = WORLD_HEIGHT / 10f;
    static final float PADDLE_ENEMY_POSITION_X = WORLD_WIDTH / 16f;                          // Левая сторона
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
    private Viewport viewport;
    private World world;

    Rectangle groundTop;
    Rectangle groundBottom;
    Ball ball;
    Paddle paddle;
    PaddleEnemy paddleEnemy;

    protected ShapeRenderer shapeRenderer;

    // Сосотояние игры
    private enum State { PAUSE, RUN, RESUME, STOPPED }
    private State state          = State.RUN;
    private int paddleScore      = 0;
    private int paddleEnemyScore = 0;
    private boolean gameActive    = false;

    final private Sound f_sharp_3;

    public GameScreen(PongGame game) {
        this.game = game;

        f_sharp_3 = Gdx.audio.newSound(Gdx.files.internal("desktop/assets/pongblip_f_sharp_3.mp3"));
    }

    @Override
    public void show() {
        debugRenderer = new Box2DDebugRenderer(true, true, true, true, true, true);

        //-------------------------------------------------------//
        //                          Звуки                        //
        //-------------------------------------------------------//
        final Sound f_sharp_5 = Gdx.audio.newSound(Gdx.files.internal("desktop/assets/pongblip_f_sharp_5.mp3"));
        final Sound f_sharp_4 = Gdx.audio.newSound(Gdx.files.internal("desktop/assets/pongblip_f_sharp_4.mp3"));
        // f_sharp_3 = Gdx.audio.newSound(Gdx.files.internal("desktop/assets/pongblip_f_sharp_3.mp3"));

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
        final FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
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

        //-------------------------------------------------------//
        //                     Столкновения                      //
        //-------------------------------------------------------//
        world.setContactListener(new ContactListener() {

            @Override
            public void beginContact(Contact contact) {
                //-------------------------------------------------------//
                //             Столкновение с ракеткой игрока            //
                //-------------------------------------------------------//
                if ((contact.getFixtureA().getBody() == ball.body   && contact.getFixtureB().getBody() == paddle.body) ||
                    (contact.getFixtureA().getBody() == paddle.body && contact.getFixtureB().getBody() == ball.body)) {
                    f_sharp_5.play();

                    // Gdx.app.log("collision1", contact.getWorldManifold().getPoints()[0].toString());
                    // Gdx.app.log("paddle_pos", paddle.body.getPosition().toString());

                    // Вычисление точки удара шарика об ракетку
                    float contact_pos_y = contact.getWorldManifold().getPoints()[0].y - paddle.body.getPosition().y - paddle.height/2f;
                    // Gdx.app.log("relative_pos",  new Float(contact_pos_y).toString() );

                    // Вычисление угла отражения, максимальный угол = 60 град.
                    float angle = contact_pos_y*60 / (PADDLE_SIZE_Y/2f);
                    // Gdx.app.log("angle", new Float(angle).toString());

                    Vector2 velocity = ball.body.getLinearVelocity();
                    ball.body.setLinearVelocity(velocity.setAngle(angle));

                }

                //-------------------------------------------------------//
                //          Столкновение с ракеткой противника           //
                //-------------------------------------------------------//
                if ((contact.getFixtureA().getBody() == ball.body        && contact.getFixtureB().getBody() == paddleEnemy.body) ||
                    (contact.getFixtureA().getBody() == paddleEnemy.body && contact.getFixtureB().getBody() == ball.body)) {
                    f_sharp_5.play();

                    float contact_pos_y = contact.getWorldManifold().getPoints()[0].y - paddleEnemy.body.getPosition().y - paddleEnemy.height/2f;
                    float angle         = contact_pos_y*60 / (PADDLE_ENEMY_SIZE_Y/2f);
                    Vector2 velocity    = ball.body.getLinearVelocity();
                    ball.body.setLinearVelocity(velocity.setAngle(angle));

                }

                if ((contact.getFixtureA().getBody() == ball.body      && contact.getFixtureB().getBody() == groundTop.body) ||
                    (contact.getFixtureA().getBody() == groundTop.body && contact.getFixtureB().getBody() == ball.body)) {
                    f_sharp_4.play();
                }

                if ((contact.getFixtureA().getBody() == ball.body         && contact.getFixtureB().getBody() == groundBottom.body) ||
                    (contact.getFixtureA().getBody() == groundBottom.body && contact.getFixtureB().getBody() == ball.body)) {
                    f_sharp_4.play();
                }
            }

            @Override
            public void endContact(Contact contact) {
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        // Старт игры
        if (!gameActive) {
            ball.body.setTransform(BALL_POSITION_X, BALL_POSITION_Y, 0f);
            ball.pushBall(BALL_VELOCITY_X * 50, 0 * BALL_VELOCITY_Y * 1);
            gameActive = true;
        }

        // Процесс игры
        if (gameActive) {
            // world.step(Gdx.app.getGraphics().getDeltaTime(), 4, 4);
            world.step(1 / 60f, 4, 4);
            paddle.processMovement(50f, 0f, GROUND_TOP_POSITION_Y, Gdx.graphics.getHeight(), WORLD_HEIGHT);
            paddleEnemy.processMovement(80f, 0f, GROUND_TOP_POSITION_Y, WORLD_WIDTH); // - GROUND_TOP_SIZE_Y);

            batch.setProjectionMatrix(camera.combined);
            batch.begin();

            groundTop.draw();
            groundBottom.draw();
            paddle.draw();
            paddleEnemy.draw();
            ball.draw();

            batch.end();

            //-------------------------------------------------------//
            //                 Проверка выйгрыша                     //
            //-------------------------------------------------------//
            int check_win = checkWin();
            if (check_win != 0) {
                if (check_win == 1) {
                    f_sharp_3.play();
                    paddleEnemyScore += 1;
                    gameActive = false;
                } else {
                    f_sharp_3.play();
                    paddleScore += 1;
                    gameActive = false;
                }
            }

            // Проверка окончения игры
            if (paddleScore > 2) {
                game.setScreen(new FinishScreen((PongGame)game, false));
                dispose();
                return;
            } else if (paddleEnemyScore > 2) {
                game.setScreen(new FinishScreen((PongGame)game, true));
                dispose();
                return;
            }

            drawScore(paddleEnemyScore, paddleScore);

            drawCenterLine();

            debugRenderer.render(world, camera.combined);
        }

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
        // ball.body.setTransform(BALL_POSITION_X, BALL_POSITION_Y, 0f);
        // ball.pushBall(BALL_VELOCITY_X * 100, 0*BALL_VELOCITY_Y * 1);

        // Ракетка
        paddle = new Paddle(world, shapeRenderer, PADDLE_SIZE_X, PADDLE_SIZE_Y);
        paddle.body.setTransform(PADDLE_POSITION_X, PADDLE_POSITION_Y, 0f);

        // Ракетка соперника
        paddleEnemy = new PaddleEnemy(world, shapeRenderer, PADDLE_ENEMY_SIZE_X, PADDLE_ENEMY_SIZE_Y, ball);
        paddleEnemy.body.setTransform(PADDLE_ENEMY_POSITION_X, PADDLE_ENEMY_POSITION_Y, 0f);
    }

    /**
     * Проверка выйгрыша
     * @return int 1 - player 1 win, 2 - player 2 win, 0 - game is continue
     */
    private int checkWin() {
        float ball_x = ball.body.getPosition().x;
        if (ball_x < 0) {
            return 1;
        } else if (ball_x > WORLD_WIDTH) {
            return 2;
        } else {
            return 0;
        }

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
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        debugRenderer.dispose();
        world.dispose();
        batch.dispose();
        font.dispose();
    }
}
