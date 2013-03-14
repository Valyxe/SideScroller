import java.util.ArrayList;


/*
 * This is the class for all doors in the game.
 */
public class DoorSprite extends Sprite
{
	private static double DURATION = 0.5;

	private int moveSizeX;
	private int moveSizeY;
	
	private boolean doorUnlocked;
	
	@SuppressWarnings("unchecked")
	public DoorSprite(int x, int y, int w, int h, ArrayList<ArrayList> images, int moveX, int moveY)
	{
		super(x, y, w, h, images, DURATION, 0, 0);
	
		moveSizeX = moveX;
		moveSizeY = moveY;
		doorUnlocked = false;
	}

	/*
	 * This method unlocks the door and sets it as inactive so that it is no longer rendered.
	 */
	public void openDoor()
	{
		doorUnlocked = true;
		setActive(false);
	}
	
	/*
	 * This method returns whether or not the door is locked.
	 */
	public boolean isUnlocked()
	{
		return doorUnlocked;
	}
	
	/*
	 * Move the door in the positive x-direction.
	 * This is used for horizontal scrolling across the screen.
	 */
	public void moveRight()
	{
		locx += moveSizeX;
	}
	
	/*
	 * Move the door in the negative x-direction.
	 * This is used for horizontal scrolling across the screen.
	 */
	public void moveLeft()
	{
		locx -= moveSizeX;
	}
	
	/*
	 * Move the door in the positive y-direction.
	 * This is used for vertical scrolling across the screen.
	 */
	public void moveUp()
	{
		locy -= 4*moveSizeY;
	}
	
	/*
	 * Move the door in the negative y-direction.
	 * This is used for vertical scrolling across the screen.
	 */
	public void moveDown()
	{
		locy += moveSizeY;
	}
}
