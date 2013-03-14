/*
 * Allan Simmons
 * CSC493
 * Dr.Mooney
 * Side Scroller
 * 
 * This side scroller was made using the architecture designed by Andrew Davison.
 * Images credits:
 * 	Fish and Foot enemies: Commander Keen, id Software
 *  Background: http://mariomods.com/p/nsmbw-backgrounds-2 (Presumably originally from Super Mario World, Nintendo)
 *  Bricks, Keys, Doors, and Hero: Allan Simmons
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/*
 * This class is the frame in which the game is run.
 */
@SuppressWarnings("serial")
public class GameFrame extends JFrame implements WindowListener
{
	private static int DEFAULT_FPS = 30;

	private GamePanel gp;

	public GameFrame(long period)
	{
		super("Dungeon Crawler");


		Container c = getContentPane();
		gp = new GamePanel(this, period);
		c.add(gp, "Center");

		addWindowListener( this );
		pack();
		setResizable(false);
		setVisible(true);
	}

	/*
	 * Resume game play while the window has focus.
	 */
	public void windowActivated(WindowEvent e) 
	{
		gp.resumeGame();
	}

	/*
	 * Pause the game if the window does not have focus.
	 */
	public void windowDeactivated(WindowEvent e) 
	{
		gp.pauseGame();
	}

	/*
	 * Resume game play while window is not minimized.
	 */
	public void windowDeiconified(WindowEvent e) 
	{
		gp.resumeGame();
	}

	/*
	 * Pause the game if the window is minimized.
	 */
	public void windowIconified(WindowEvent e) 
	{
		gp.pauseGame();
	}


	/*
	 * End the game if the window is closed.
	 */
	public void windowClosing(WindowEvent e)
	{
		gp.stopGame();
	}

	/*
	 * Unused methods.
	 */
	public void windowClosed(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}

	/*
	 * Main method. Starts the GameFrame, the animation loop thread
	 * of the game.
	 */
	public static void main(String args[])
	{
		long period = (long) 1000.0/DEFAULT_FPS;
		new GameFrame(period*1000000L);
	}

}
