package sprite;
import java.awt.*;
import java.awt.image.*;
import java.util.ArrayList;

/*
 * This is the parent class for all Sprites in this game.
 */
public class Entity 
{
	//Default dimensions when there is no image
	protected static final int SIZE = 12;   

	//Image-related
	private ArrayList<BufferedImage> ims;
	private String imageName;
	protected BufferedImage image;
	private int width, height;

	protected boolean isLooping;

	private boolean isActive = true;      

	protected int locx, locy;
	
	private boolean isRepeating, ticksIgnored;
	private int animPeriod;
	private long animTotalTime;
	private int showPeriod;
	private double seqDuration;
	private int numImages;
	private int imPosition;

	public Entity(int x, int y, ArrayList<BufferedImage> images, double seqDuration, EntityType type) 
	{
		locx = x; locy = y;
		ims = images;
		setImage(0);
		
		
		numImages = ims.size();
		imPosition = 0;
		ticksIgnored = false;
		showPeriod = (int) (1000 * seqDuration / numImages);
	}

	/*
	 * This method sets the current image set of the Sprite
	 * to the specified number.
	 */
	public void setImage(int num)
	{
		//Set to initial image of set.
		image = (BufferedImage) ims.get(0);
		imageNum = num;
		if(image == null)
		{
			System.out.println("No sprite image for " + imageName);
			width = SIZE;
			height = SIZE;
		}
		else
		{
			width = image.getWidth();
			height = image.getHeight();
		}
 		
		numImages = ims.size();
		animTotalTime = 0;
		//Not looping, even if was before.
		isLooping = false;
	}

	/*
	 * Starts the looping of the Sprite's image.
	 */
	public void loopImage(int animPeriod, double seqDuration)
	{
		//Can only loop if there is more than one image.
		imPosition = 0;
		if(ims.size() > 1)
		{
			this.animPeriod = animPeriod;
			this.seqDuration = seqDuration;
			isLooping = true;
			ticksIgnored = false;
		}
		else
			System.out.println(imageName + " is not a sequence of images");
	}

	/*
	 * Stops the looping of the current image and resets
	 * the associated variables.
	 */
	public void stopLooping()
	{
		stop();
		isLooping = false;
		imPosition = 0;
	}

	/*
	 * Returns the width of the current Sprite image.
	 */
	public int getWidth()
	{
		return width;
	}

	/*
	 * Returns the height of the current Sprite image.
	 */
	public int getHeight()
	{
		return height;
	}
	
	/*
	 * Returns if the Sprite is active.
	 */
	public boolean isActive() 
	{
		return isActive;
	}

	/*
	 * Set whether or not the Sprite is active.
	 */
	public void setActive(boolean a) 
	{
		isActive = a;
	}

	/*
	 * Set the position of the Sprite to the given coordinates
	 * in the x and y planes.
	 */
	public void setPosition(int x, int y)
	{
		locx = x;
		locy = y;
	}

	/*
	 * Translate the Sprite the given x and y distances from where it is currently.
	 */
	public void translate(int xDist, int yDist)
	{
		locx += xDist;
		locy += yDist;
	}

	/*
	 * Return the x-coordinate location of the Sprite.
	 */
	public int getXPosn()
	{
		return locx;
	}

	/*
	 * Return the y-coordinate location of the Sprite.
	 */
	public int getYPosn()
	{
		return locy;
	}

	/*
	 * Update the Sprite by moving in the x and y directions by its
	 * given amounts. If the Sprite is looping, update the image and tick.
	 */
	public void updateSprite()
	{
		if(isActive())
		{
			locx += dx;
			locy += dy;
			if(isLooping)
				updateTick();
		}
	}
	
	/*
	 * For looping images, the image displayed needs to be updated with each game update.
	 */
	public void updateTick()
	{
		//Update only if it needs to.
		if(!ticksIgnored)
		{
			//Update total animation time, modulo the animation sequence duration.
			animTotalTime = (animTotalTime + animPeriod) % (long)(1000 * seqDuration);
			
			//Calculate current displayable image position.
			imPosition = (int) (animTotalTime / showPeriod);	//In range 0 to num-1.
			if((imPosition == numImages-1) && (!isRepeating))
			{
				// At end of sequence, stop at this image.
				ticksIgnored = true;
			}
		}
	}
	
	/*
	 * Returns the current image of the Sprite.
	 */
	public BufferedImage getCurrentImage()
	{
		if (numImages != 0)
			return (BufferedImage) ims.get(imPosition);
		else
			return null; 
	}

	/*
	 * Returns the current position in the current image strip of the Sprite.
	 */
	public int getCurrentPosition()
	{
		return imPosition;
	}
	
	/*
	 * Stop looping by ignoring the update ticks.
	 */
	public void stop()
	{
		ticksIgnored = true;
	}
	
	/*
	 * Returns whether or not the looping has been stopped.
	 */
	public boolean isStopped()
	{
		return ticksIgnored;
	}
	
	/*
	 * Returns whether or not the image is currently looping.
	 */
	public boolean isLooping()
	{
		return isLooping;
	}
	
	/*
	 * Returns if the looping is at it's end, but only true if not repeating loop.
	 */
	public boolean atSequenceEnd()
	{
		return ((imPosition == numImages-1) && (!isRepeating));
	}
	
	/*
	 * Restarts the imgage loop at the given position.
	 */
	public void restartAt(int imPosn)
	{
		if(numImages != 0)
		{
			if((imPosn < 0) || (imPosn > numImages-1))
			{
				System.out.println("Out of range restart, starting at 0");
				imPosn = 0;
			}

			imPosition = imPosn;
			// Calculate a suitable animation time.
			animTotalTime = (long) imPosition * showPeriod;
	      ticksIgnored = false;
	      }
	}

	/*
	 * Resume the looping of the image from the last location it was at.
	 * This is achieved by turning ticks back on.
	 */
	public void resume()
	{
		if(numImages != 0)
			ticksIgnored = false;
	}
	
	/*
	 * Draw the Sprite at its location on the screen.
	 */
	public void drawSprite(Graphics g) 
	{
		//Only draw if the Sprite is active.
		if(isActive())
		{
			//If it has no image, draw a yellow circle.
			if(image == null)
			{
				g.setColor(Color.yellow);
				g.fillOval(locx, locy, SIZE, SIZE);
				g.setColor(Color.black);
			}
			//Otherwise draw its current image.
			else
			{
				if(isLooping)
					image = getCurrentImage();
				g.drawImage(image, locx, locy, null);
			}
		}
	}
	
	/*
	 * Gets a bounding rectangle for the Sprite using its current image.
	 * This is used for collision detection.
	 */
	public Rectangle getMyRectangle()
	{
		return  new Rectangle(locx, locy, width, height);
	}
}
