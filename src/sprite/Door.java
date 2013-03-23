package sprite;
import java.util.ArrayList;
import com.badlogic.gdx.graphics.Texture;


/*
 * This is the class for all doors in the game.
 */
public class Door extends Entity
{
	private boolean doorLocked;
	
	public Door(int x, int y, ArrayList<Texture> images, EntityType type)
	{
		super(x, y, images, type);
		doorLocked = true;
	}

	/*
	 * This method unlocks the door and sets it as inactive so that it is no longer rendered.
	 */
	public void openDoor()
	{
		doorLocked = false;
		setActive(false);
	}
	
	/*
	 * This method returns whether or not the door is locked.
	 */
	public boolean isLocked()
	{
		return doorLocked;
	}
}
