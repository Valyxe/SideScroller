import java.awt.*;
import java.awt.image.*;

/*
 * This class controls a specific background ribbon.
 * A ribbon is a background image that scrolls.
 */
public class Ribbon
{
	private BufferedImage im;
	private int width;
	private int pWidth, pHeight;

	private int moveSizeX;
	private boolean isMovingRight;
	private boolean isMovingLeft;

	private int xImHead;
	private int yImHead;
	/* The x-coord in the panel where the start of the image
	(its head) should be drawn. 
	It can range between -width to width (exclusive), so can
	have a value beyond the confines of the panel (0-pWidth).

	As xImHead varies, the on-screen ribbon will usually
	be a combination of its tail followed by its head.*/


	public Ribbon(int w, int h, BufferedImage im, int moveSz)
	{
		pWidth = w; pHeight = h;

		this.im = im;
		width = im.getWidth();
		if(width < pWidth) 
			System.out.println("Ribbon width < panel width");
		moveSizeX = moveSz;
		isMovingRight = false;
		isMovingLeft = false;
		xImHead = 0;
		yImHead = 0;
	}


	/*
	 * Set the flags for moving to the right.
	 */
	public void moveRight()
	{
		isMovingRight = true;
		isMovingLeft = false;
	}

	/*
	 * Set the flags for moving to the left.
	 */
	public void moveLeft()
	{
		isMovingRight = false;
		isMovingLeft = true;
	}
	
	/*
	 * Set the flags for not moving in either direction.
	 */
	public void stayStill()
	{
		isMovingRight = false;
		isMovingLeft = false;
	}

	/*
	 * Update the position of the image head based on the direction it is
	 * moving in.
	 */
	public void update()
	{
		if(isMovingRight)
		{
			xImHead = (xImHead + moveSizeX) % width;
		}
		else if(isMovingLeft)
		{
			xImHead = (xImHead - moveSizeX) % width;
		}
	}


	/*
	 * Draws the ribbon on the screen based on the image head position.
	 * There are four cases to consider:
	 * Ribbon moving to the right and both ends are on screen
	 * Ribbon moving to the right normally
	 * Ribbon moving to the left normally
	 * Ribbon moving to the left and both ends are on screen
	 */
	public void display(Graphics g)
	{
		if (xImHead == 0) //Draw im head at (0,0)
			draw(g, im, 0, pWidth, 0, pWidth);
		else if((xImHead > 0) && (xImHead < pWidth))
		{  
			//Draw im tail at (0,0) and im head at (xImHead,0).
			draw(g, im, 0, xImHead, width-xImHead, width);
			draw(g, im, xImHead, pWidth, 0, pWidth-xImHead);
		}
		else if(xImHead >= pWidth)
			draw(g, im, 0, pWidth, width-xImHead, width-xImHead+pWidth);
		else if((xImHead < 0) && (xImHead >= pWidth-width))
			draw(g, im, 0, pWidth, -xImHead, pWidth-xImHead);
		else if (xImHead < pWidth-width)
		{
			//Draw im tail at (0,0) and im head at (width+xImHead,0).
			draw(g, im, 0, width+xImHead, -xImHead, width);
			draw(g, im, width+xImHead, pWidth, 0, pWidth-width-xImHead);
		}
	}

	/*
	 * Draw the ribbon image segment at the location.
	 */
	private void draw(Graphics g, BufferedImage im, int scrX1, int scrX2, int imX1, int imX2)
	{
		g.drawImage(im, scrX1, 0, scrX2, pHeight, imX1, yImHead,  imX2, pHeight+yImHead, null);
	}
}
