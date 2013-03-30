package game;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopGame
{
	public static void main(String[] args)
	{
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
	      cfg.title = "Dungeon Crawl";
	      cfg.useGL20 = false;
	      cfg.width = 800;
	      cfg.height = 480;
	      new LwjglApplication(new Game(), cfg);
	}
}
