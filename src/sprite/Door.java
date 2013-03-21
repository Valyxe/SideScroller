package sprite;
import java.awt.image.BufferedImage;
import java.util.ArrayList;


/*
 * This is the class for all doors in the game.
 */
public class Door extends Entity
{
	private static double DURATION = 0.5;
	private boolean doorUnlocked;
	
	public Door(int x, int y, ArrayList<BufferedImage> images, EntityType type)
	{
		super(x, y, images, DURATION, type);
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
}
