import java.util.ArrayList;


/*
 * This is the base class for all enemies in the game.
 */
public class EnemySprite extends Sprite
{
	private static double DURATION = 0.5;

	private boolean isFacingRight;
	private int hitPoints;
	private int moveSizeX;
	private int moveSizeY;
	private int distanceMoved = 0;
	
	@SuppressWarnings("unchecked")
	public EnemySprite(int x, int y, int w, int h, ArrayList<ArrayList> images, int moveX, int moveY, int hp)
	{
		super(x, y, w, h, images, DURATION, 0, 0);
		hitPoints = hp;
		isFacingRight  = true;
		moveSizeX = moveX;
		moveSizeY = moveY;
	}
	
	/*
	 * This method takes care of what happens when an enemy has been hit by the hero.
	 * One hit point is deducted; if the total is 0 or less, the enemy is defeated.
	 */
	public void wasHit()
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
	public void updateSprite()
	{
		//Determine facing.
		if (isFacingRight)
		{
			//Update position.
			locx += moveSizeX;
			distanceMoved += moveSizeX;
			//Check to see if movement is past max distance.
			if(distanceMoved >= ((getCurrentImage().getWidth())/1.5))
			{
				//Go in other direction.
				isFacingRight = false;
				setImage(0);
			}
		}
		else
		{
			//Update position.
			locx -= moveSizeX;
			distanceMoved -= moveSizeX;
			//Check to see if movement is past max distance.
			if(distanceMoved <= (-(getCurrentImage().getWidth())/1.5))
			{
				//Go in other direction.
				isFacingRight = true;
				setImage(1);
			}
		}
		//SupX;er!
		super.updateSprite();
	}
	
	/*
	 * Move the enemy in the positive x-direction.
	 * This is used for horizontal scrolling across the screen.
	 */
	public void moveRight()
	{
		locx += moveSizeX;
	}
	
	/* 
	 * Move the enemy in the negative x-direction.
	 * This is used for horizontal scrolling across the screen.
	 */
	public void moveLeft()
	{
		locx -= moveSizeX;
	}

	/* 
	 * Move the enemy in the positive y-direction.
	 * This is used for vertical scrolling across the screen.
	 */
	public void moveUp()
	{
		locy -= 4*moveSizeY;
	}
	
	/* 
	 * Move the enemy in the negative y-direction.
	 * This is used for vertical scrolling across the screen.
	 */
	public void moveDown()
	{
		locy += moveSizeY;
	}
}
