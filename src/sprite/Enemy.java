package sprite;
import java.util.ArrayList;
import com.badlogic.gdx.graphics.Texture;


/*
 * This is the base class for all enemies in the game.
 */
public class Enemy extends Entity
{
	private boolean isFacingRight = true;
	private int hitPoints;
	private int moveSize = 5, distanceMoved = 0;
	private static final int maxMove = 25;
	
	public Enemy(int x, int y, ArrayList<Texture> images, int hp, EntityType type)
	{
		super(x, y, images, type);
		hitPoints = hp;
	}
	
	/*
	 * This method takes care of what happens when an enemy has been hit by the hero.
	 * One hit point is deducted; if the total is 0 or less, the enemy is defeated.
	 */
	public void takeHit()
	{
		hitPoints--;
		if(hitPoints <= 0)
		{
			setActive(false);
		}
	}
	
	/*
	 * Return the number of hit points the enemy has remaining.
	 */
	public int getHitPoints()
	{
		return hitPoints;
	}
	
	/*
	 * This method overwrites the update method in Sprite.
	 * The enemy's location is updated; the enemy patrols
	 * a location, moving left and then right over a central location.
	 */
	public void update()
	{
		//Determine facing.
		if (isFacingRight)
		{
			//Update position.
			translate(moveSize, 0);
			distanceMoved+=moveSize;
			//Check to see if movement is past max distance.
			if(distanceMoved >= maxMove)
			{
				//Go in other direction.
				isFacingRight = false;
				setImage(0, 1, 1);
			}
		}
		else
		{
			//Update position.
			translate(-moveSize, 0);
			distanceMoved-=moveSize;
			//Check to see if movement is past max distance.
			if(distanceMoved <= (-maxMove))
			{
				//Go in other direction.
				isFacingRight = true;
				setImage(1, 1, 1);
			}
		}
	}
}
