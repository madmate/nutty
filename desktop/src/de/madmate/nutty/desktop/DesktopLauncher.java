package de.madmate.nutty.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import de.madmate.nutty.NuttyGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 960;
		config.height = 544;
		new LwjglApplication(new NuttyGame(), config);
		//TexturePacker.process("../assets", "../assets", "nutty_assets");
	}
}
