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
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
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

    private static final int WORLD_WIDTH  = PongGame.WORLD_WIDTH;
    private static final int WORLD_HEIGHT = PongGame.WORLD_HEIGHT;

    // Левая граница, размеры и позиция
    private static final float GROUND_LEFT_SIZE_X     = WORLD_WIDTH / 32f;
    private static final float GROUND_LEFT_SIZE_Y     = WORLD_HEIGHT;
    private static final float GROUND_LEFT_POSITION_X = -GROUND_LEFT_SIZE_X; // 0f;
    private static final float GROUND_LEFT_POSITION_Y = 0f;

    // Правая граница, размеры и позиция
    private static final float GROUND_RIGHT_SIZE_X     = WORLD_WIDTH / 32f;
    private static final float GROUND_RIGHT_SIZE_Y     = WORLD_HEIGHT;
    private static final float GROUND_RIGHT_POSITION_X = WORLD_WIDTH;
    private static final float GROUND_RIGHT_POSITION_Y = 0f;

    // Шар
    private static final float BALL_RADIUS     = WORLD_HEIGHT / 120f;
    private static final float BALL_POSITION_X = WORLD_WIDTH  / 2f;
    private static final float BALL_POSITION_Y = WORLD_HEIGHT / 2f;
    private static final float BALL_VELOCITY_X = WORLD_WIDTH  / 4f;
    private static final float BALL_VELOCITY_Y = WORLD_HEIGHT / 4f;
    private static final float BALL_VELOCITY_START     = WORLD_HEIGHT / 2f;  // Начальная скорость шарика
    private static final float BALL_VELOCITY_MAX       = WORLD_HEIGHT;       // Максимальная скорость шарика
    private static final float BALL_VELOCITY_INCREMENT = WORLD_HEIGHT / 16f; // Величина увеличения скорости шарика поле 5-го удара

    private float ballVelocity; // Текущая скорость шарика


    // Ракетка
    private static final float PADDLE_SIZE_X     = WORLD_WIDTH  / 10f;
    private static final float PADDLE_SIZE_Y     = WORLD_HEIGHT / 64f;
    private static final float PADDLE_POSITION_X = WORLD_WIDTH/2f - PADDLE_SIZE_X/2f; // Середина экрана
    private static final float PADDLE_POSITION_Y = WORLD_HEIGHT/12f;                  // Нижняя ракетка

    // Ракетка, соперника
    private static final float PADDLE_ENEMY_SIZE_X     = WORLD_WIDTH / 10f;
    private static final float PADDLE_ENEMY_SIZE_Y     = WORLD_HEIGHT / 64f;
    private static final float PADDLE_ENEMY_POSITION_X = WORLD_WIDTH/2f - PADDLE_ENEMY_SIZE_X/2f;               // Середина экрана
    private static final float PADDLE_ENEMY_POSITION_Y = WORLD_HEIGHT - PADDLE_ENEMY_SIZE_Y - WORLD_WIDTH / 8f; // Верхняя ракетка

    // Центральная линия
    private static final float CENTER_LINE_POSITION_X = GROUND_LEFT_SIZE_X - WORLD_WIDTH/64f;
    private static final float CENTER_LINE_POSITION_Y = WORLD_HEIGHT/2f - WORLD_HEIGHT/256f;
    private static final float CENTER_LINE_SIZE_X     = WORLD_WIDTH/32f;
    private static final float CENTER_LINE_SIZE_Y     = WORLD_HEIGHT/64f;
    private static final float CENTER_LINE_STEP_X     = WORLD_WIDTH/16f;

    private SpriteBatch batch;
    private BitmapFont font;

    // private Box2DDebugRenderer debugRenderer;
    private OrthographicCamera camera;
    private Viewport viewport;
    private World world;

    private Rectangle groundTop;
    private Rectangle groundBottom;
    private Ball ball;
    private Paddle paddle;
    private PaddleEnemy paddleEnemy;

    private ShapeRenderer shapeRenderer;

    // Сосотояние игры
    private enum State { PAUSE, RUN, RESUME, STOPPED }
    private State state          = State.RUN;
    private int paddleScore      = 0;
    private int paddleEnemyScore = 0;
    private int paddleContact    = 0;     // Касаение с ракеткой, через 5 касаний увеливается скорость
    private boolean isPaddleGoal = false; // Кто забил гол, нужно для определения направления старта мячика
    private boolean gameActive   = false;
    private int scoreToWins;

    private Sound f_sharp_3;

    private FPSLogger fpsLogger;

    GameScreen(PongGame game, int score_to_wins) {
        this.game        = game;
        this.scoreToWins = score_to_wins;

        fpsLogger = new FPSLogger();

        f_sharp_3 = Gdx.audio.newSound(Gdx.files.internal("data/pongblip_f_sharp_3.mp3"));
    }

    @Override
    public void show() {
        this.gameActive = false;

        // debugRenderer = new Box2DDebugRenderer(true, true, true, true, true, true);

        //-------------------------------------------------------//
        //                          Звуки                        //
        //-------------------------------------------------------//
        final Sound f_sharp_5 = Gdx.audio.newSound(Gdx.files.internal("data/pongblip_f_sharp_5.mp3"));
        final Sound f_sharp_4 = Gdx.audio.newSound(Gdx.files.internal("data/pongblip_f_sharp_4.mp3"));
        // f_sharp_3 = Gdx.audio.newSound(Gdx.files.internal("desktop/assets/pongblip_f_sharp_3.mp3"));

        //-------------------------------------------------------//
        //                         Шрифт                         //
        //-------------------------------------------------------//
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("data/04b_24.ttf"));
        // FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("data/04b_03b.ttf"));
        // FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("desktop/assets/04b_08.ttf"));
        final FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = WORLD_HEIGHT / 10;
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
                    float contact_pos_x = contact.getWorldManifold().getPoints()[0].x - paddle.body.getPosition().x - paddle.width/2f;
                    // Gdx.app.log("relative_pos",  new Float(contact_pos_y).toString() );

                    // Вычисление угла отражения, максимальный угол = 60 град.
                    float angle = 90 - contact_pos_x*60 / (PADDLE_SIZE_X/2f);

                    Vector2 velocity = ball.body.getLinearVelocity();
                    velocity = velocity.nor().scl(ballVelocity);
                    velocity.setAngle(angle);
                    ball.body.setLinearVelocity(velocity);

                    // Gdx.app.log("velocity", Float.toString(velocity.len()));
                    // Gdx.app.log("angle", Float.toString(angle));

                    paddleContact += 1;

                }

                //-------------------------------------------------------//
                //          Столкновение с ракеткой противника           //
                //-------------------------------------------------------//
                if ((contact.getFixtureA().getBody() == ball.body        && contact.getFixtureB().getBody() == paddleEnemy.body) ||
                    (contact.getFixtureA().getBody() == paddleEnemy.body && contact.getFixtureB().getBody() == ball.body)) {
                    f_sharp_5.play();

                    float contact_pos_x = contact.getWorldManifold().getPoints()[0].x - paddleEnemy.body.getPosition().x - paddleEnemy.width/2f;
                    float angle         = 90 - contact_pos_x*60 / (PADDLE_ENEMY_SIZE_X/2f);
                    Vector2 velocity    = ball.body.getLinearVelocity();

                    velocity = velocity.nor().scl(ballVelocity);
                    velocity.setAngle(angle);
                    ball.body.setLinearVelocity(velocity);

                    // Gdx.app.log("velocity", Float.toString(velocity.len()));
                    // Gdx.app.log("angle", Float.toString(angle));

                    paddleContact += 1;
                }

                // Столкновение со стеной
                if ((contact.getFixtureA().getBody() == ball.body      && contact.getFixtureB().getBody() == groundTop.body) ||
                    (contact.getFixtureA().getBody() == groundTop.body && contact.getFixtureB().getBody() == ball.body)) {
                    f_sharp_4.play();
                }

                // Столкновение со стеной
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

    ////private float timeSpent;

    @Override
    public void render(float delta) {

        // fpsLogger.log();

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        //-------------------------------------------------------//
        //                       Старт игры                      //
        //-------------------------------------------------------//
        if (!gameActive) {
            ballVelocity = BALL_VELOCITY_START;
            ball.body.setTransform(BALL_POSITION_X, BALL_POSITION_Y, 0f);

            Vector2 vel = new Vector2(BALL_VELOCITY_X, BALL_VELOCITY_Y);
            vel = vel.nor().scl(ballVelocity);

            ball.body.setLinearVelocity(ball.body.getLinearVelocity().nor().scl(ballVelocity));

            if (isPaddleGoal) {
                vel.y = -vel.y;
            } else {
                // vel.y = vel.y;
            }
            ball.pushBall(vel);

            this.gameActive = true;
        }

        //-------------------------------------------------------//
        //                      Процесс игры                     //
        //-------------------------------------------------------//
        if (this.gameActive) {
            // world.step(Gdx.app.getGraphics().getDeltaTime(), 4, 4);
            world.step(1 / 60f, 3, 3);

            paddle.processMovement(50f, 0f, GROUND_RIGHT_POSITION_X, Gdx.graphics.getWidth(), WORLD_WIDTH);
            paddleEnemy.processMovement(120f, 0f, GROUND_RIGHT_POSITION_X, WORLD_WIDTH); // - GROUND_RIGHT_SIZE_Y);

            batch.setProjectionMatrix(camera.combined);
            batch.begin();

            groundTop.draw();
            groundBottom.draw();
            paddle.draw();
            paddleEnemy.draw();
            ball.draw();

            batch.end();

            //-------------------------------------------------------//
            //           Проверка выйгрыша, окончания раунда         //
            //-------------------------------------------------------//
            int check_win = checkWin();
            if (check_win != 0) {
                if (check_win == 1) {
                    paddleEnemyScore += 1;
                    isPaddleGoal      = false; // Гол забил соперник
                } else {
                    paddleScore += 1;
                    isPaddleGoal = true; // Гол забил игрок
                }
                f_sharp_3.play();
                paddleContact = 0; // обнулить касания
                gameActive    = false;
            }

            //-------------------------------------------------------//
            //              Проверка окончания игры                  //
            //-------------------------------------------------------//
            if (paddleScore >= this.scoreToWins) {
                game.setScreen(new FinishScreen((PongGame)game, false, this.scoreToWins));
                dispose();
                return;
            } else if (paddleEnemyScore >= this.scoreToWins) {
                game.setScreen(new FinishScreen((PongGame)game, true, this.scoreToWins));
                dispose();
                return;
            }

            //-------------------------------------------------------//
            //   Увеличение скорости шарика на 5 ударе об ракетку    //
            //-------------------------------------------------------//
            if ((paddleContact+1) % 4 == 0) {
                // float velocity_x = ball.body.getLinearVelocity().x;
                // float velocity_y = ball.body.getLinearVelocity().y;
                // ball.body.setLinearVelocity(velocity_x * (WORLD_WIDTH / 128f), velocity_y * (WORLD_WIDTH / 128f));

                if (ballVelocity < BALL_VELOCITY_MAX) {
                    ballVelocity += BALL_VELOCITY_INCREMENT;
                }
                ball.body.setLinearVelocity(ball.body.getLinearVelocity().nor().scl(ballVelocity));

                // Gdx.app.log("velocity_x", new Integer(paddleContact).toString() + " " + new Float(velocity_x).toString());
                paddleContact++;

            }

            drawScore(paddleScore, paddleEnemyScore);

            drawCenterLine();

            // debugRenderer.render(world, camera.combined);

        }
    }

    /**
     * Создание игрового мира
     */
    private void createObjects() {
        // Верхняя граница
        groundTop = new Rectangle(world, shapeRenderer, GROUND_RIGHT_SIZE_X, GROUND_RIGHT_SIZE_Y);
        groundTop.body.setTransform(GROUND_RIGHT_POSITION_X, GROUND_RIGHT_POSITION_Y, 0f);

        // Нижняя ганица
        groundBottom = new Rectangle(world, shapeRenderer, GROUND_LEFT_SIZE_X, GROUND_LEFT_SIZE_Y);
        groundBottom.body.setTransform(GROUND_LEFT_POSITION_X, GROUND_LEFT_POSITION_Y, 0f);

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
        float ball_y = ball.body.getPosition().y;
        if (ball_y < 0) {
            return 2;
        } else if (ball_y > WORLD_HEIGHT) {
            return 1;
        } else {
            return 0;
        }

    }

    private void drawScore(int score_1, int score_2) {
        batch.begin();
        // shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // shapeRenderer.setColor(Color.GREEN);
        // font.draw(batch, "PLAY", 10, 10);
        font.draw(batch, Integer.toString(score_1), WORLD_WIDTH - WORLD_WIDTH/10f, WORLD_HEIGHT/2f + WORLD_HEIGHT/10f); // Соперник
        font.draw(batch, Integer.toString(score_2), WORLD_WIDTH - WORLD_WIDTH/10f, WORLD_HEIGHT/2f - WORLD_HEIGHT/32f); // Игрок
        // shapeRenderer.end();
        batch.end();
    }

    private void drawCenterLine() {
        batch.begin();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);

        float pos_x = CENTER_LINE_POSITION_X;
        for (int i = 0; i < 16; i++) {
            shapeRenderer.rect(pos_x, CENTER_LINE_POSITION_Y, CENTER_LINE_SIZE_X, CENTER_LINE_SIZE_Y);
            pos_x += CENTER_LINE_STEP_X;
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
        // debugRenderer.dispose();
        world.dispose();
        batch.dispose();
        font.dispose();
    }
}
