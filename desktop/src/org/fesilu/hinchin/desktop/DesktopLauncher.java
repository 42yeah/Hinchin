package org.fesilu.hinchin.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import org.fesilu.hinchin.Game;


public class DesktopLauncher {
	/**
	 * main 是桌面构建的游戏主入口。从这里会初始化 Game, 之后就可以参考 core 中 Game 类的 create() 了。
	 * @param arg 调用参数，咱可以不管
	 */
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(new Game(), config);
	}
}
