package com.akhramovichsa.pong;

import com.akhramovichsa.pong.Screens.FirstScreen;
import com.akhramovichsa.pong.Screens.GameScreen;
import com.badlogic.gdx.Game;


/**
 *
 */
public class PongGame extends Game {
	// 1280x720 = 16:9
	public static final int WORLD_WIDTH  = 90;  //160; // 320; // 1280 / PPM;
	public static final int WORLD_HEIGHT = 160; // 90;  // 180; // 720  / PPM;

	@Override
	public void create () {
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