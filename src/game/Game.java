package game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import sprite.Brick;
import sprite.Door;
import sprite.Enemy;
import sprite.Entity;
import sprite.EntityType;
import sprite.Hero;

/*
 * This class runs the main game loop.
 */
public class Game implements ApplicationListener, InputProcessor
{
	private static final int PWIDTH = 1366;
	private static final int PHEIGHT = 768;
	private static final int BRICK_SIZE = 64;
	private static final String LEVEL_INFO = "levelInfo.txt";

	private volatile boolean running = false;
	private volatile boolean isPaused = false;
	
	private Hero hero;
	private ArrayList<Entity> objects;
	private OrthographicCamera camera;

	private volatile boolean gameOver = false;
	private boolean showTitle;
	private Texture titleIm;
	private boolean showHelp;
	private Texture helpIm;

	/*
	 * Update the various components of the game, including the hero,
	 * enemies, doors, bricks, and ribbons.
	 */
	private void gameUpdate() 
	{
		if(!isPaused && !gameOver)
		{
			if(hero.willHitObject())
			{
				hero.stayStill();
			}
			//Update hero.			
			hero.update();
			
			//Update other sprites.
			for(int i = 0; i < objects.size(); i++)
			{
				if(objects.get(i).getType() == EntityType.ENEMY);
					((Enemy)objects.get(i)).update();
			}
			
			//Make hero, bricks, and ribbons stay still, so that
			// movement is only caused by holding down the key.
			hero.stayStill();
			
			//Game over if either the hero or the boss dies.
			if(hero.getHitPoints() <= 0)
				gameOver = true;
		}
	}
	
	public void render()
	{
		gameUpdate();
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT|GL10.GL_DEPTH_BUFFER_BIT);
		camera.update();
		
		Image im;
		SpriteBatch sb;
		if(showTitle)
		{
			im = new Image(titleIm);
			sb = new SpriteBatch();
			sb.begin();
			im.draw(sb, 1.0f);
			sb.end();
		}
		
		if(showHelp)
		{
			im = new Image(helpIm);
			sb = new SpriteBatch();
			sb.begin();
			im.draw(sb, 1.0f);
			sb.end();
		}
		
		hero.render();
		for(int i = 0; i < objects.size(); i++)
		{
			objects.get(i).render();
		}
	}
	
	public void create()
	{		
		//Initialize sprites.
		objects = new ArrayList<Entity>();
			
		//Prepare title/help screen.
		System.out.printf("%b\n", Gdx.files);
		titleIm = new Texture(Gdx.files.internal("data/images/Title.png"));
		helpIm = new Texture(Gdx.files.internal("data/images/Help.png"));
		showTitle = true;
		showHelp = false;    // show at start-up
		isPaused = true;
				
		initObjects();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);
		camera.translate(hero.getXPosn(), hero.getYPosn());
	}
	
	/*
	 * Initializes all doors in the game level.
	 * Reads in the same configuration file as for the bricks
	 * manager, but only cares about 'd's.
	 */
	private void initObjects()
	{
		try
		{
			//Get file for reading.
			InputStream in = this.getClass().getResourceAsStream(LEVEL_INFO);
			BufferedReader br = new BufferedReader( new InputStreamReader(in));
			String line;
			char ch;
			//Line counter.
			int lineNo = 0;
			//Read in the lines, one by one. 
			while((line = br.readLine()) != null)
			{
				//Ignore the following lines.
				if(line.length() == 0)
					continue;
				if(line.startsWith("//"))
					continue;

				//Step through the line.
				for(int x=0; x < line.length(); x++)
				{
					ch = line.charAt(x);
					if(ch == '0')
					{
						ArrayList<Texture> brickIms = new ArrayList<Texture>();
						Texture t = new Texture(Gdx.files.internal("data/images/Bricks.png"));
						brickIms.add(t);
						int locX = x * BRICK_SIZE;
						int locY = (lineNo * BRICK_SIZE) - (PHEIGHT + BRICK_SIZE);
						objects.add(new Brick(locX, locY, brickIms, EntityType.BRICK));
					}
					else if(ch == '1')
					{
						ArrayList<Texture> keyIms = new ArrayList<Texture>();
						keyIms.add(new Texture(Gdx.files.internal("data/images/Key.png")));
						int locX = x * BRICK_SIZE;
						int locY = (lineNo * BRICK_SIZE) - (PHEIGHT + BRICK_SIZE);
						objects.add(new Brick(locX, locY, keyIms, EntityType.KEY));
					}
					else if(ch == 'h')
					{
						ArrayList<Texture> heroIms = new ArrayList<Texture>();
						heroIms.add(new Texture(Gdx.files.internal("data/images/Hero_Left.png")));
						heroIms.add(new Texture(Gdx.files.internal("data/images/Hero_Right.png")));
						heroIms.add(new Texture(Gdx.files.internal("data/images/Hero_Running_Left.png")));
						heroIms.add(new Texture(Gdx.files.internal("data/images/Hero_Running_Right.png")));
						heroIms.add(new Texture(Gdx.files.internal("data/images/Hero_Attack_Left.png")));
						heroIms.add(new Texture(Gdx.files.internal("data/images/Hero_Attack_Right.png")));
						int locX = x * BRICK_SIZE;
						int locY = (lineNo * BRICK_SIZE) - (PHEIGHT + BRICK_SIZE);
						hero  = new Hero(locX, locY, heroIms, 100, EntityType.HERO, objects);
					}
					else if(ch  == 'e')
					{
						//Initialize the enemy at the given location.
						ArrayList<Texture> enemyIms = new ArrayList<Texture>();
						Texture imLeft = new Texture(Gdx.files.internal("data/images/Enemy_Left.png"));
						Texture imRight = new Texture(Gdx.files.internal("data/images/Enemy_Right.png"));
						enemyIms.add(imLeft);
						enemyIms.add(imRight);
						int locX = x * BRICK_SIZE;
						int locY = (lineNo * BRICK_SIZE) - (PHEIGHT + BRICK_SIZE);
						objects.add(new Enemy(locX, locY, enemyIms, 1, EntityType.ENEMY));
					}
					else if(ch == 'b')
					{
						//Initialize the boss at the given location.
						ArrayList<Texture> enemyIms = new ArrayList<Texture>();
						Texture imLeft = new Texture(Gdx.files.internal("data/images/Boss_Left.png"));
						Texture imRight = new Texture(Gdx.files.internal("data/images/Boss_Right.png"));
						enemyIms.add(imLeft);
						enemyIms.add(imRight);
						int locX = x * BRICK_SIZE;
						int locY = (lineNo * BRICK_SIZE) - (PHEIGHT + BRICK_SIZE);
						objects.add(new Enemy(locX, locY, enemyIms, 10, EntityType.BOSS));
					}
					else if(ch == 'd')
					{
						//Initialize the door at the given location.
						ArrayList<Texture> doorIms = new ArrayList<Texture>();
						doorIms.add(new Texture(Gdx.files.internal("data/images/Door.png")));
						int locX = x * BRICK_SIZE;
						int locY = (lineNo * BRICK_SIZE) - (PHEIGHT + BRICK_SIZE);
						objects.add(new Door(locX, locY, doorIms, EntityType.DOOR));
					}
				}
				lineNo++;
			}
			br.close();
		}
		//Error reading in file.
		catch(IOException e)
		{
			System.out.println("Error reading file: " + "..Images/" + LEVEL_INFO);
			System.exit(1);
		}
	}

	public void resize(int width, int height)
	{
		float aspectRatio = (float) width / (float) height;
        camera = new OrthographicCamera(2f * aspectRatio, 2f);
	}

	public void pause()
	{
		isPaused = true;
	}
	
	public void resume()
	{
		if(!showHelp)
			isPaused = false;
	}
	
	public void dispose()
	{
		running = false;
	}

	/*
	 * This method processes all key input from the user.
	 *  ESC, Q, END, CTRL-C end the game.
	 *  H displays the help screen.
	 *  Left Arrow moves the character to the left.
	 *  Right Arrow moves the character to the right.
	 *  Up Arrow makes the character jump.
	 *  Space Bar makes the character attack.
	 */
	public boolean keyDown(int keyCode)
	{
		/*Termination keys: Listen for esc, q, end, ctrl-c on the canvas to
		 * allow a convenient exit from the full screen configuration
		 */
		if ((keyCode == KeyEvent.VK_ESCAPE) || (keyCode == KeyEvent.VK_Q) || (keyCode == KeyEvent.VK_END))
			running = false;

		//Help controls.
		if (keyCode == KeyEvent.VK_H)
		{
			showTitle = false;
				if (showHelp)
			{
				showHelp = false;  //Switch off.
				isPaused = false;
			}
			else
			{
				showHelp = true;    //Show it.
				isPaused = true;    //isPaused may already be true
			}
		}

		if(!isPaused && !gameOver)
		{
			//Move the sprite and ribbons based on the arrow key pressed.
			if(keyCode == KeyEvent.VK_LEFT)
			{
				//Move sprite left.
				hero.moveLeft();
				camera.translate(new Vector3(-1, 0, 0));
			}
			else if(keyCode == KeyEvent.VK_RIGHT)
			{
				//Move sprite right.
				hero.moveRight();
				camera.translate(new Vector3(1, 0, 0));
			}
			else if(keyCode == KeyEvent.VK_UP)
			{
				//Jumping has no effect on the bricks/ribbons.
				hero.jump();
			}
			else if(keyCode == KeyEvent.VK_SPACE)
			{
				//Attacking has no effect on the bricks/ribbons.
				hero.attack();
			}
		}
		return true;
	}

	public boolean keyUp(int keycode){return false;}
	public boolean keyTyped(char character){return false;}
	public boolean touchDown(int screenX, int screenY, int pointer, int button){return false;}
	public boolean touchUp(int screenX, int screenY, int pointer, int button){return false;}
	public boolean touchDragged(int screenX, int screenY, int pointer){return false;}
	public boolean mouseMoved(int screenX, int screenY){return false;}
	public boolean scrolled(int amount){return false;}
}
