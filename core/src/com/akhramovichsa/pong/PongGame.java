package com.akhramovichsa.pong;

import com.akhramovichsa.pong.Screens.FirstScreen;
import com.akhramovichsa.pong.Screens.GameScreen;
import com.badlogic.gdx.Game;


/**
 * @see * https://github.com/epes/libgdx-box2d-pong
 */
public class PongGame extends Game {
	// 1280x720 = 16:9
	public static final int WORLD_WIDTH  = 160; // 320; // 1280 / PPM;
	public static final int WORLD_HEIGHT = 90;  // 180; // 720  / PPM;

	public FirstScreen firstScreen;
	public GameScreen  gameScreen;

	@Override
	public void create () {
		// firstScreen = new FirstScreen(this);
		// gameScreen  = new GameScreen(this);

		setScreen(new FirstScreen(this));
		// setScreen(gameScreen);
	}
	/*
	@Override
	public void render() {
	}
	*/

	@Override
	public void dispose () {
		// firstScreen.dispose();
		// gameScreen.dispose();
	}

	/*
	@Override
	public void pause () {
	}

	@Override
	public void resume () {
	}

	@Override
	public void resize (int width, int height) {
	}
	*/
}