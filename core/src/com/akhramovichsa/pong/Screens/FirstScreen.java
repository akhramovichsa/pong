package com.akhramovichsa.pong.Screens;

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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * @see * http://www.gamefromscratch.com/page/LibGDX-Tutorial-series.aspx
 */
public class FirstScreen implements Screen {

    static final int WORLD_WIDTH  = PongGame.WORLD_WIDTH;
    static final int WORLD_HEIGHT = PongGame.WORLD_HEIGHT;

    private Game game;
    private OrthographicCamera camera;
    private Stage stage;
    private SpriteBatch batch;
    private BitmapFont font, font_big;

    public FirstScreen(PongGame _game) {
        game = _game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
        camera.update();

        batch = new SpriteBatch();
        batch.setProjectionMatrix(camera.combined);

        // Звуки
        final Sound f_sharp_5 = Gdx.audio.newSound(Gdx.files.internal("desktop/assets/pongblip_f_sharp_5.mp3"));

        // Шрифты
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("desktop/assets/04b_24.ttf"));
        // FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("desktop/assets/04b_03b.ttf"));
        // FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("desktop/assets/04b_08.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 80; // WORLD_HEIGHT / 10;
        font = generator.generateFont(parameter);

        parameter.size = 160;
        font_big = generator.generateFont(parameter);

        generator.dispose();

        Label.LabelStyle label_style = new Label.LabelStyle();
        label_style.font      = font;
        label_style.fontColor = Color.WHITE;

        Label.LabelStyle label_big_style = new Label.LabelStyle();
        label_big_style.font      = font_big;
        label_big_style.fontColor = Color.WHITE;

        //-------------------------------------------------------//
        //                        PONG                           //
        //-------------------------------------------------------//
        Label label_pong = new Label("PONG", label_big_style);

        //-------------------------------------------------------//
        //                      1 PLAYER                         //
        //-------------------------------------------------------//
        Label label_1_player = new Label("1 PLAYER", label_style);
        label_1_player.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("button", "clicked 1 player");
                f_sharp_5.play();
                game.setScreen(new GameScreen((PongGame) game));
                dispose();
                // label_1_player.setText("PONG 1");
            }
        });

        //-------------------------------------------------------//
        //                      2 PLAYERS                        //
        //-------------------------------------------------------//
        Label label_2_players = new Label("2 PLAYERS", label_style);
        label_2_players.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("button", "clicked 2 players");
                f_sharp_5.play();
            }
        });

        //-------------------------------------------------------//
        //                      SETTINGS                         //
        //-------------------------------------------------------//
        Label label_settings = new Label("SETTINGS", label_style);
        label_settings.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("button", "clicked settings");
                f_sharp_5.play();
            }
        });

        //-------------------------------------------------------//
        //                        EXIT                           //
        //-------------------------------------------------------//
        Label label_exit = new Label("EXIT", label_style);
        label_exit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("button", "clicked exit");
                f_sharp_5.play();
                Gdx.app.exit();
                // label_1_player.setText("PONG 1");
            }
        });

        //-------------------------------------------------------//
        //                      ТАБЛИЦА                          //
        //-------------------------------------------------------//
        final Table table = new Table();
        // table.setDebug(true);
        // table.setFillParent(true);
        table.setPosition(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f);
        table.add(label_pong).expandX().center().pad(WORLD_HEIGHT / 32);
        table.row();
        table.add(label_1_player).expandX().center().pad(WORLD_HEIGHT / 32);
        table.row();
        table.add(label_2_players).expandX().center().pad(WORLD_HEIGHT / 32);
        table.row();
        table.add(label_settings).expandX().center().pad(WORLD_HEIGHT / 32);
        table.row();
        table.add(label_exit).expandX().center();

        stage = new Stage();
        stage.getViewport().setCamera(camera);
        Gdx.input.setInputProcessor(stage);

        stage.addActor(table);

        // stage.addActor(label_2_players);
        // stage.addActor(label_settings);
        // stage.addActor(label_exit);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // batch.begin();
        stage.draw();
        // batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
        // camera.update();

        // stage.getViewport().setCamera(camera);
        stage.getViewport().update(width,height, false);
        // stage.getCamera().update();
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
        batch.dispose();
        stage.dispose();
        font.dispose();
    }
}
