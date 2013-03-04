import java.awt.*;
import java.awt.image.*;

/*
 * This class encompasses all data for each individual brick on the screen.
 */
public class Brick
{
	private int mapX, mapY;
	private int imageID;

	private BufferedImage image;
	private int height, width;

	private int locY, locX;

	public Brick(int id, int x, int y)
	{
		mapX = x;  mapY = y;
		imageID = id;
	}

	/*
	 * Return the map x-coordinate of the brick.
	 */
	public int getMapX()
	{
		return mapX;
	}

	/*
	 * Return the map y-coordinate of the brick.
	 */
	public int getMapY()
	{
		return mapY;
	}

	/*
	 * Get the strip image number of the brick.
	 */
	public int getImageID()
	{
		return imageID;
	}

	/*
	 * Set the current image to the index specified.
	 */
	public void setImage(BufferedImage im)
	{
		image = im;
		height = im.getHeight();
		width = im.getWidth();
	}
	
	/*
	 * Set the y-coordinate location based on the height of the panel and the number of rows.
	 */
	public void setLocY(int pHeight, int maxYBricks)
	{
		locY = pHeight - ((maxYBricks-mapY) * height);
	}

	/*
	 * Set the x-coordinate location based on the width of the panel and the number of columns
	 */
	public void setLocX(int pWidth, int maxXBricks)
	{
		locX = (mapX * width) + width;
	}
	
	/*
	 * Offset the y-coordinate of the brick based on the input.
	 * This is used for vertical scrolling of the brick map.
	 */
	public void setLocY(int offset)
	{
		locY += offset;
	}
	
	/*
	 * Return the y-coordinate of the brick.
	 */
	public int getLocY()
	{
		return locY;
	}
	
	/*
	 * Return the x-coordinate of the brick.
	 */
	public int getLocX()
	{
		return locX;
	}
	
	/*
	 * Return the width of the brick's current image.
	 */
	public int getWidth()
	{
		return image.getWidth();
	}
	
	/*
	 * Return the height of the brick's current image.
	 */
	public int getHeight()
	{
		return image.getHeight();
	}

	/*
	 * Draw the brick at the specified x-coordinate and its y-coordinate.
	 */
	public void display(Graphics g, int xScr)
	{
		g.drawImage(image, xScr, locY, null);
	}
}

