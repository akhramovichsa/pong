package com.akhramovichsa.pong;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.akhramovichsa.pong.PongGame;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		// Для экономии заряда батареи
		config.useAccelerometer = false;
		config.useGyroscope     = false;
		config.useCompass       = false;
		initialize(new PongGame(), config);
	}
}
