package game;
import javax.swing.*;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import sprite.Door;
import sprite.Enemy;
import sprite.EntityType;
import sprite.Hero;

import java.awt.image.*;
import java.awt.event.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Vector;

/*
 * This class runs the main game loop.
 */
@SuppressWarnings("serial")
public class GamePanel extends JPanel implements Runnable, ApplicationListener
{
	private static final int PWIDTH = 1366;
	private static final int PHEIGHT = 768;
	
	private static final int BRICK_SIZE = 64;

	private static final int NO_DELAYS_PER_YIELD = 16;
	private static final int MAX_FRAME_SKIPS = 5;

	private static final String IMS_INFO = "imsInfo.txt";
	private static final String BRICKS_INFO = "bricksInfo.txt";

	private Thread animator;
	private volatile boolean running = false;
	private volatile boolean isPaused = false;

	private long period;

	private ImagesLoader imsLoader;
	private Hero hero;
	private ArrayList<Door> doors;
	private ArrayList<Enemy> enemies;
	private Enemy boss;

	private long gameStartTime;
	private int timeSpentInGame;

	private Font msgsFont;
	private FontMetrics metrics;

	private Graphics dbg; 
	private Image dbImage = null;

	private volatile boolean gameOver = false;
	private boolean showTitle;
	private BufferedImage titleIm;
	private boolean showHelp;
	private BufferedImage helpIm;

	
	
	
	
	
	
	private OrthographicCamera camera;
	private Vector<Mesh> meshes = new Vector<Mesh>();
	
	
	@SuppressWarnings("unchecked")
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

		//Initialize the loaders.
		imsLoader = new ImagesLoader(IMS_INFO);

		//Initialize sprites.
		create();
		
		
		
		ArrayList<BufferedImage> heroIms = new ArrayList<BufferedImage>();
		heroIms.add(imsLoader.getImages("Hero_Running_Left"));
		heroIms.add(imsLoader.getImages("Hero_Running_Right"));
		heroIms.add(imsLoader.getImages("Hero_Left"));
		heroIms.add(imsLoader.getImages("Hero_Right"));
		heroIms.add(imsLoader.getImages("Hero_Attack_Left"));
		heroIms.add(imsLoader.getImages("Hero_Attack_Right"));
		hero = new Hero(PWIDTH, PHEIGHT, heroIms, (int)(period/1000000L), 100, EntityType.HERO);
		
		doors = new ArrayList<Door>();
		enemies = new ArrayList<Enemy>();
		create();
		
		//Prepare title/help screen.
		titleIm = imsLoader.getImage("Title");
		helpIm = imsLoader.getImage("Help");
		showTitle = true;
		showHelp = false;    // show at start-up
		isPaused = true;

		//Set up message font.
		msgsFont = new Font("SansSerif", Font.BOLD, 24);
		metrics = this.getFontMetrics(msgsFont);
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
				//Bricks and ribbons move the other way.
				hero.moveLeft();
				camera.translate(new Vector3(-1, 0, 0));
			}
			else if(keyCode == KeyEvent.VK_RIGHT)
			{
				//Move sprite right.
				//Bricks and ribbons move the other way.
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
			if(hero.willHitBrick() || hero.willHitDoor(doors))
			{
				hero.stayStill();
			}
			//Update hero.			
			hero.updateSprite();
			hero.nearDoor(doors);
			hero.hitEnemy(enemies, boss);
			
			//Update other sprites.
			for(int i = 0; i < doors.size(); i++)
				doors.get(i).updateSprite();
			for(int i = 0; i < enemies.size(); i++)
				enemies.get(i).updateSprite();
			boss.updateSprite();
			
			//Make hero, bricks, and ribbons stay still, so that
			// movement is only caused by holding down the key.
			hero.stayStill();
			
			//Game over if either the hero or the boss dies.
			if(hero.getHitPoints() <= 0 || !boss.isActive())
				gameOver = true;
		}
	}
//		
//			/*
//			 * Render the game image after components have updated.
//			 */
//			private void gameRender()
//			{
//				//Snag graphics context.
//				if (dbImage == null)
//				{
//					dbImage = createImage(PWIDTH, PHEIGHT);
//					if(dbImage == null)
//					{
//						System.out.println("dbImage is null");
//						return;
//					}
//					else
//						dbg = dbImage.getGraphics();
//				}
//		
//				//Initialize screen.
//				dbg.setColor(Color.black);
//				dbg.fillRect(0, 0, PWIDTH, PHEIGHT);
//		
//				
//				/*
//				 */
//				
//				
//				
//				
//				//Draw components in specific order/
//				ribsMan.display(dbg);       //The background ribbons.
//				bricksMan.display(dbg);     //The bricks.
//				hero.drawSprite(dbg);       //The sprites.
//				for(int i = 0; i < doors.size(); i++)
//					doors.get(i).drawSprite(dbg);
//				for(int i = 0; i < enemies.size(); i++)
//					enemies.get(i).drawSprite(dbg);
//				boss.drawSprite(dbg);
//		
//				//HUD for stats.
//				reportStats(dbg);
//		
//				//Game over message.
//				if(gameOver)
//					gameOverMessage(dbg);
//		
//				//Title message.
//				if(showTitle)
//					dbg.drawImage(titleIm, (PWIDTH-helpIm.getWidth())/2, (PHEIGHT-helpIm.getHeight())/2, null);
//				
//				//Help message.
//				if(showHelp)
//					dbg.drawImage(helpIm, (PWIDTH-helpIm.getWidth())/2, (PHEIGHT-helpIm.getHeight())/2, null);
//			}
//		
//			/*
//			 * Generates HUD that relays:
//			 * Current HP
//			 * Number of keys in 'inventory'
//			 * Ammount of time playing the game
//			 * Current Boss HP
//			 */
//			private void reportStats(Graphics g)
//			{
//				if(!gameOver)
//					timeSpentInGame = (int) ((System.nanoTime() - gameStartTime)/1000000000L);
//				g.setColor(Color.red);
//				g.setFont(msgsFont);
//				g.drawString("HP: " + hero.getHitPoints(), 15, 30);
//				g.drawString("Keys: " + hero.getKeys(), 15, 50);
//				g.drawString("Time: " + timeSpentInGame + " secs", 15, 70);
//				g.drawString("Boss HP: " + boss.getHitPoints(), 15, 90);
//				g.setColor(Color.black);
//			}
//		
//			/*
//			 * Displays the game over message on the screen.
//			 */
//			private void gameOverMessage(Graphics g)
//			{
//				String msg = "Game Over.";
//		
//				int x = (PWIDTH - metrics.stringWidth(msg))/2; 
//				int y = (PHEIGHT - metrics.getHeight())/2;
//				g.setColor(Color.black);
//				g.setFont(msgsFont);
//				g.drawString(msg, x, y);
//			}
//		
//			/*
//			 * Forces Java to paint to the screen instead of waiting on
//			 * a call to paint() to be processed.
//			 */
//			private void paintScreen()
//			{ 
//				//Grab the current graphics context.
//				Graphics g;
//				try
//				{
//					//Draw the game image to the screen.
//					g = this.getGraphics();
//					if((g != null) && (dbImage != null))
//						g.drawImage(dbImage, 0, 0, null);
//					Toolkit.getDefaultToolkit().sync();
//					g.dispose();
//				}
//				catch(Exception e)
//				{
//					System.out.println("Graphics context error: " + e);
//				}
//			}
	
	@Override
	public void create()
	{
		initObjects();
	}
	
	@Override
	public void render()
	{
		camera.update();
		camera.apply(Gdx.gl10);
		
		
		
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT|GL10.GL_DEPTH_BUFFER_BIT);
	}
	
	/*
	 * Initializes all doors in the game level.
	 * Reads in the same configuration file as for the bricks
	 * manager, but only cares about 'd's.
	 */
	@SuppressWarnings("unchecked")
	private void initObjects()
	{
		try
		{
			//Get file for reading.
			InputStream in = this.getClass().getResourceAsStream("../Images/" + BRICKS_INFO);
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
					//Ignore anything that is not a 'd'.
					ch = line.charAt(x);
					if(ch  == 'e')
					{
						//Initialize the enemy at the given location.
						ArrayList<ArrayList> enemyIms = new ArrayList<ArrayList>();
						ArrayList<BufferedImage> imLeft = imsLoader.getImages("Enemy_Left");
						ArrayList<BufferedImage> imRight = imsLoader.getImages("Enemy_Right");
						enemyIms.add(imLeft);
						enemyIms.add(imRight);
						int locX = x * BRICK_SIZE;
						int locY = (lineNo * BRICK_SIZE) - (PHEIGHT + BRICK_SIZE);
						enemies.add(new Enemy(locX, locY, PWIDTH, PHEIGHT, enemyIms, bricksMan.getMoveSizeX(), bricksMan.getMoveSizeY(), 1));
					}
					else if(ch == 'b')
					{
						//Initialize the boss at the given location.
						ArrayList<ArrayList> bossIms = new ArrayList<ArrayList>();
						ArrayList<BufferedImage> imLeft = imsLoader.getImages("Boss_Left");
						ArrayList<BufferedImage> imRight = imsLoader.getImages("Boss_Right");						
						bossIms.add(imLeft);
						bossIms.add(imRight);
						int locX = x * BRICK_SIZE;
						int locY = (lineNo * BRICK_SIZE) - (PHEIGHT + imLeft.get(0).getHeight());
						boss = new Enemy(locX, locY, bossIms, 15, EntityType.ENEMY);
					}
					else if(ch == 'd')
					{
						//Initialize the door at the given location.
						ArrayList<BufferedImage> doorIms = imsLoader.getImages("Door");
						int locX = x * BRICK_SIZE;
						int locY = (lineNo * BRICK_SIZE) - (doorIms.get(0).getHeight() + PHEIGHT);
						doors.add(new Door(locX, locY, doorIms, EntityType.DOOR));
					}
				}
				lineNo++;
			}
			br.close();
		}
		//Error reading in file.
		catch(IOException e)
		{
			System.out.println("Error reading file: " + "..Images/" + BRICKS_INFO);
			System.exit(1);
		}
	}

	@Override
	public void resize(int width, int height)
	{
		float aspectRatio = (float) width / (float) height;
        camera = new OrthographicCamera(2f * aspectRatio, 2f);
	}

	@Override
	public void pause(){}

	@Override
	public void resume(){}

	@Override
	public void dispose(){}
}
