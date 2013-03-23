package sprite;

import java.util.ArrayList;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.collision.BoundingBox;

/*
 * This is the class that controls the player character, the Hero.
 */
public class Hero extends Entity
{
	private boolean isFacingRight, isStill;
	private VertMode vertMode;
	private int moveSize = 5, distanceMoved = 0;
	private static final int maxMove = 11;
	
	private ArrayList<Entity> objects;
	private int hitPoints;
	private int keys = 0;

	public Hero(int x, int y, ArrayList<Texture> images, int hp, EntityType type, ArrayList<Entity> world)
	{
		super(x, y, images, type);

		//Set Variables
		isFacingRight = true;
		isStill = true;
		hitPoints = hp;
		vertMode = VertMode.NOT_JUMPING;
		objects = world;
	}


	/*
	 * Makes the hero appear to move to the left.
	 */
	public void moveLeft()
	{
		setImage(0, 1, 4);
		isFacingRight = false;
		isStill = false;
	}

	/*
	 * Makes the hero appear to move to the right.
	 */
	public void moveRight()
	{
		setImage(1, 1, 4);
		isFacingRight = true;
		isStill = false;
	}

	/*
	 * Makes the hero appear to stop moving;
	 */
	public void stayStill()
	{
		isStill = true;
	}
	
	/*
	 * Finds if the hero is moving or not.
	 */
	public boolean isStill()
	{
		return isStill;
	}
	
	/*
	 * The hero can jump, increasing its y location.
	 */
	public void jump()
	{
		//Can only jump if not jumping; no double-jumping in my game.
		if(vertMode == VertMode.NOT_JUMPING)
		{ 
			//Set mode to jumping.
			vertMode = VertMode.RISING;
			distanceMoved = 0;
			if(isStill)
			{
				//Set sprite image.
				if(!isFacingRight)
					setImage(2, 1, 1);
				else
					setImage(3, 1, 1);
			}
		}
	}


	public boolean willHitObject()
	{	
		if(isStill)
			return false;

		BoundingBox b = this.getBoundingBox();
		b.ext(1, 0, 0);
		
		boolean hit = false;
		for(int i = 0; i < objects.size(); i++)
		{
			if(b.contains(objects.get(i).getBoundingBox()));
			{
				if(objects.get(i).getType() == EntityType.DOOR && ((Door)objects.get(i)).isLocked() && keys > 0)
				{
					keys--;
					((Door)objects.get(i)).openDoor();
				}
				else
				{
					hit = true;
				}
			}
		}
		return hit;
	}

	/*
	 * Add a key to the hero's 'inventory'.
	 */
	public void addKey()
	{
		keys++;
	}
	
	/*
	 * Returns the number of keys the hero has on him.
	 */
	public int getKeys()
	{
		return keys;
	}
	
	/*
	 * Returns the number of hit points the hero currently has left.
	 */
	public int getHitPoints()
	{
		return hitPoints;
	}
	
	/*
	 * Sets the hero into attack mode.
	 */
	public void attack()
	{
		if(!isFacingRight)
			setImage(4, 1, 4);
		else
			setImage(5, 1, 4);
	}
	
	/*
	 * Overrides the base Sprite method for updating.
	 * Updates position, checks for falling, and checks
	 * if vertical scrolling is necessary.
	 */
	public void update()
	{
		//Update position.
		if(!isStill)
		{
			if (isFacingRight)  // moving right
				translate(moveSize, 0);
			else // moving left
				translate(-moveSize, 0);
		}
		
		if(vertMode == VertMode.NOT_JUMPING)   // If not jumping.
			checkIfFalling();   // May have moved out into empty space.

		//Vertical movement has two components: RISING and FALLING.
		if(vertMode == VertMode.RISING)
			updateJumping();
		else if (vertMode == VertMode.FALLING)
			updateFalling();
	}

	/*
	 * Check the bricks beneath the hero to see if he can fall.
	 */
	private void checkIfFalling()
	{
		BoundingBox b = this.getBoundingBox();
		b.ext(0, -1, 0);
		
		for(int i = 0; i < objects.size(); i++)
		{
			if(b.contains(objects.get(i).getBoundingBox()))
			{
				vertMode = VertMode.FALLING;   //Set it to be in falling mode
			}
		}
	}

	/*
	 * If the hero is jumping, we need to update its location.
	 */
	private void updateJumping()
	{		
		if(distanceMoved == maxMove)
		{
			vertMode = VertMode.FALLING;   //At top, now start falling.
			distanceMoved = 0;
		}
		else
		{
			BoundingBox b = this.getBoundingBox();
			b.ext(0, moveSize, 0);
			
			for(int i = 0; i < objects.size(); i++)
			{
				if(b.contains(objects.get(i).getBoundingBox()))
				{
					vertMode = VertMode.FALLING;   //Start falling.
					distanceMoved = 0;
				}
				else
				{
					translate(0, moveSize);   //Update position.
					distanceMoved+=moveSize;
				}
			}

		}
	}

	/*
	 * If the hero is falling, we need to update its location.
	 */
	private void updateFalling()
	{
		BoundingBox b = this.getBoundingBox();
		b.ext(0, -1, 0);
		
		for(int i = 0; i < objects.size(); i++)
		{
			if(b.contains(objects.get(i).getBoundingBox()))
			{
				finishJumping();
			}
			else
			{
				translate(0, -moveSize);
			}
		}
	}

	/*
	 * Once jumping is done with, resume normal movement.
	 */
	private void finishJumping()
	{
		vertMode = VertMode.NOT_JUMPING;
		distanceMoved = 0;
		if(isStill)
		{
			if(isFacingRight)
				setImage(1, 1, 4);
			else    //Facing left.
				setImage(0, 1, 4);
		}
	}
}