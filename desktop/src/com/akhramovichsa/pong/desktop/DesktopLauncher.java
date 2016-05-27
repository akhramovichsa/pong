package com.akhramovichsa.pong.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.akhramovichsa.pong.PongGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title  = "Pong";
		config.width  = 720/2;
		config.height = 1280/2;
		new LwjglApplication(new PongGame(), config);
	}
}
