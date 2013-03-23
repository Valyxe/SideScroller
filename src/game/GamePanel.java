package game;

import javax.swing.*;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import sprite.Brick;
import sprite.Door;
import sprite.Enemy;
import sprite.Entity;
import sprite.EntityType;
import sprite.Hero;

import java.awt.event.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/*
 * This class runs the main game loop.
 */
public class GamePanel extends JPanel implements Runnable, ApplicationListener
{
	private static final long serialVersionUID = 1L;
	private static final int PWIDTH = 1366;
	private static final int PHEIGHT = 768;
	private static final int NO_DELAYS_PER_YIELD = 16;
	private static final int MAX_FRAME_SKIPS = 5;
	private static final int BRICK_SIZE = 64;
	private static final String LEVEL_INFO = "levelInfo.txt";

	private Thread animator;
	private volatile boolean running = false;
	private volatile boolean isPaused = false;
	private long period;
	private long gameStartTime;
	
	private Hero hero;
	private ArrayList<Entity> objects;
	private OrthographicCamera camera;

	private volatile boolean gameOver = false;
	private boolean showTitle;
	private Texture titleIm;
	private boolean showHelp;
	private Texture helpIm;
	
	public GamePanel(GameFrame gf, long period)
	{
		//Set the period.
		this.period = period;

		//Initialize window settings.
		setDoubleBuffered(false);
		setBackground(Color.white);
		setPreferredSize(new Dimension(PWIDTH, PHEIGHT));

		setFocusable(true);
		requestFocus();		//The JPanel now has focus, so receives key events.

		addKeyListener( new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				processKey(e);
			}
		});
		
		//Initialize sprites.
		create();
		objects = new ArrayList<Entity>();
		
		//Prepare title/help screen.
		titleIm = new Texture("../Images/Title.png");
		helpIm = new Texture("../Images/Help.png");
		showTitle = true;
		showHelp = false;    // show at start-up
		isPaused = true;
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
	private void processKey(KeyEvent e)
	{
		int keyCode = e.getKeyCode();

		/*Termination keys: Listen for esc, q, end, ctrl-c on the canvas to
		 * allow a convenient exit from the full screen configuration
		 */
		if ((keyCode == KeyEvent.VK_ESCAPE) || (keyCode == KeyEvent.VK_Q) || (keyCode == KeyEvent.VK_END) || ((keyCode == KeyEvent.VK_C) && e.isControlDown()) )
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
	}

	/*
	 * Notifies this component that it now has a parent component.
	 */
	public void addNotify()
	{
		super.addNotify();   //Creates the peer.
		startGame();         //Start the thread.
	}

	/*
	 * This method starts the game thread.
	 */
	private void startGame()
	{ 
		if(animator == null || !running)
		{
			animator = new Thread(this);
			animator.start();
		}
	}
    

	// ------------- game life cycle methods ------------
	// called by the JFrame's window listener methods

	/*
	 * Resumes the game if paused.
	 */
	public void resumeGame()
	{
		if(!showHelp)
			isPaused = false;
	} 

	/*
	 * Pauses the game so that updates do not take place.
	 */
	public void pauseGame()
	{
		isPaused = true;
	}

	/*
	 * Set running to false so that the game ends.
	 */
	public void stopGame()
	{
		running = false;
	}

	/*
	 * Main loop that updates the game components, renders the components, and then
	 * paints them to the screen using active rendering. This loop also handles the thread,
	 * making it yield if it is taking too long to update.
	 */
	public void run()
	{
		long beforeTime, afterTime, timeDiff, sleepTime;
		long overSleepTime = 0L;
		int noDelays = 0;
		long excess = 0L;

		gameStartTime = System.nanoTime();
		beforeTime = gameStartTime;

		running = true;

		while(running)
		{
			gameUpdate();
			render();
			//paintScreen();

			afterTime = System.nanoTime();
			timeDiff = afterTime - beforeTime;
			sleepTime = (period - timeDiff) - overSleepTime;

			if(sleepTime > 0)
			{
				try
				{
					Thread.sleep(sleepTime/1000000L);
				}
				catch(InterruptedException ex)
				{
					ex.printStackTrace();
				}
				overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
			}
			else
			{
				excess -= sleepTime;
				overSleepTime = 0L;

				if(++noDelays >= NO_DELAYS_PER_YIELD)
				{
					Thread.yield();
					noDelays = 0;
				}
			}

			beforeTime = System.nanoTime();

			/* If frame animation is taking too long, update the game state
				without rendering it, to get the updates/sec nearer to
				the required FPS. */
			int skips = 0;
			while((excess > period) && (skips < MAX_FRAME_SKIPS))
			{
				excess -= period;
				gameUpdate();
				skips++;
			}
		}
		System.exit(0);
	}

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
	
	public void create()
	{
		initObjects();
	}
	
	public void render()
	{
		camera.update();
		camera.apply(Gdx.gl10);
		
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT|GL10.GL_DEPTH_BUFFER_BIT);
		
		Image im;
		SpriteBatch sb;
		if(showTitle)
		{
			im = new Image(titleIm);
			sb = new SpriteBatch();
			im.draw(sb, 1.0f);
		}
		
		if(showHelp)
		{
			im = new Image(helpIm);
			sb = new SpriteBatch();
			im.draw(sb, 1.0f);
		}
		
		hero.render();
		for(int i = 0; i < objects.size(); i++)
		{
			objects.get(i).render();
		}
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
						keyIms.add(new Texture("../Images/Key.png"));
						int locX = x * BRICK_SIZE;
						int locY = (lineNo * BRICK_SIZE) - (PHEIGHT + BRICK_SIZE);
						objects.add(new Brick(locX, locY, keyIms, EntityType.KEY));
					}
					else if(ch == 'h')
					{
						ArrayList<Texture> heroIms = new ArrayList<Texture>();
						heroIms.add(new Texture("../Images/Hero_Left.png"));
						heroIms.add(new Texture("../Images/Hero_Right.png"));
						heroIms.add(new Texture("../Images/Hero_Running_Left.png"));
						heroIms.add(new Texture("../Images/Hero_Running_Right.png"));
						heroIms.add(new Texture("../Images/Hero_Attacking_Left.png"));
						heroIms.add(new Texture("../Images/Hero_Attacking_Right.png"));
						int locX = x * BRICK_SIZE;
						int locY = (lineNo * BRICK_SIZE) - (PHEIGHT + BRICK_SIZE);
						hero  = new Hero(locX, locY, heroIms, 100, EntityType.HERO, objects);
					}
					else if(ch  == 'e')
					{
						//Initialize the enemy at the given location.
						ArrayList<Texture> enemyIms = new ArrayList<Texture>();
						Texture imLeft = new Texture("../Images/Enemy_Left");
						Texture imRight = new Texture("../Images/Enemy_Right");
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
						Texture imLeft = new Texture("../Images/Boss_Left");
						Texture imRight = new Texture("../Images/Boss_Right");
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
						doorIms.add(new Texture("../Images/Door.png"));
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

	public void pause(){}

	public void resume(){}

	public void dispose(){}
}
