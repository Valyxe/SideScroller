package sprite;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/*
 * This is the class that controls the player character, the Hero.
 */
public class Hero extends Entity
{
	private static double DURATION = 0.1;  // secs
    // total time to cycle through all the images

	private static final int NOT_JUMPING = 0;   
	private static final int RISING = 1;   
	private static final int FALLING = 2;
	// used by vertMoveMode 
	//  (in J2SE 1.5 we could use a enumeration for these)

	private static final int MAX_UP_STEPS = 11;
    // max number of steps to take when rising upwards in a jump

	 // end of JumperSprite
	private int period;    // in ms; the game's animation period

	private boolean isFacingRight, isStill, isAttacking;

	private int vertMoveMode;
	/* can be NOT_JUMPING, RISING, or FALLING */
	private int vertStep;   // distance to move vertically in one step
	private int upCount;

	private int moveSize;   // obtained from BricksManager

	private int xWorld, yWorld;
    /* the current position of the sprite in 'world' coordinates.
       The x-values may be negative. The y-values will be between
       0 and pHeight. */
	
	private int hitPoints;
	private int keys = 0;

	@SuppressWarnings("unchecked")
	public Hero(int x, int y, ArrayList<BufferedImage> images, int p, int hp, EntityType type)
	{
		super(x, y, images, DURATION, type);

		//Set Variables
		moveSize = 1;
		period = p/6;

		isFacingRight = true;
		isStill = true;
		isAttacking = false;

		hitPoints = hp;
		
		/* Adjust the sprite's y- position so it is
		standing on the brick at its mid x- psoition. */
		locy = brickMan.findFloor(locx+getWidth()/2)-getHeight();
		xWorld = locx;
		yWorld = locy;    // store current position

		vertMoveMode = NOT_JUMPING;
		vertStep = brickMan.getBrickHeight()/2;   
        // the jump step is half a brick's height
		upCount = 0;
	}


	/*
	 * Makes the hero appear to move to the left.
	 */
	public void moveLeft()
	{
		setImage(0);
		//Cycle through the images.
		if(!isLooping())
			loopImage(period, DURATION);
		isFacingRight = false;
		isStill = false;
	}

	/*
	 * Makes the hero appear to move to the right.
	 */
	public void moveRight()
	{
		setImage(1);
		//Cycle through the images.
		if(!isLooping())
			loopImage(period, DURATION);
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
		if(vertMoveMode == NOT_JUMPING)
		{ 
			//Set mode to jumping.
			vertMoveMode = RISING;
			upCount = 0;
			if(isStill)
			{
				//Set sprite image.
				if(!isFacingRight)
					setImage(2);
				else
					setImage(3);
				stopLooping();
			}
		}
	}


	public boolean willHitBrick()
	{
		if(isStill)
			return false;
		
		int xTest;   // for testing the new x- position
		if(isFacingRight)   // moving right
			xTest = xWorld + moveSize;
		else // moving left
			xTest = xWorld - moveSize;

		// test a point near the base of the sprite
		int xMid = xTest + getWidth()/2;
		int yMid = yWorld + (int)(getHeight()*0.8);   // use current y posn
		
		//System.out.println("" + xMid + " " + yMid);
		
		boolean hit = brickMan.insideBrick(xMid,yMid);
		if(hit)
		{
			Brick b = brickMan.getHitBrick(xMid, yMid);
			if(b.getImageID() == 1)
			{
				addKey();
				brickMan.deleteBrick(b);
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
	 * Method to find if the hero will run into a door.
	 * Running into an unlocked door halts movement.
	 */
	public boolean willHitDoor(ArrayList<Door> doors)
	{	
		//Cannot hit a door if still.
		if(isStill)
			return false;
		
		//Generate a test coordinate
		int xTest;   // for testing the new x- position
		if(isFacingRight)   // moving right
			xTest = locx + moveSize/2;
		else // moving left
			xTest = locx - moveSize;

		// test a point near the base of the sprite
		int xMid = xTest + getWidth()/2;
		int yMid = yWorld + (int)(getHeight()*0.8);   // use current y posn
		Point p = new Point(xMid, yMid);
		
		//Find the door nearest to the hero.
		Door d;
		boolean hit = false;
		for(int i = 0; i < doors.size(); i++)
		{
			d = doors.get(i);
			//Use bounding rectangles to detect collision.
			if(d.getMyRectangle().contains(p) && d.isActive())
			{
				hit = true;
				break;
			}
		}
		
		return hit;
	}
	
	/*
	 * This method finds the nearest door to the hero and unlocks
	 * it if it has at least one key. Unlocking a door de-activates it
	 * and sets a flag so that no more keys will be used on the same door.
	 */
	public void nearDoor(ArrayList<Door> doors)
	{
		int i;
		//Use bounding rectangles to detect collision.
		Rectangle heroBox = this.getMyRectangle();
		heroBox.grow(getCurrentImage().getWidth()*2, 0);
		for(i = 0; i < doors.size(); i++)
		{
			//Door needs to be locked and hero has to have at least one key.
			if(heroBox.intersects(doors.get(i).getMyRectangle()) && !doors.get(i).isUnlocked() && keys > 0)
			{
				keys--;
				doors.get(i).openDoor();
				break;
			}
		}
	}
	
	/*
	 * This method tests for enemy collision. If the hero is facing the right direction
	 * and attacking, the enemy is damaged; otherwise the hero is damaged.
	 */
	public void hitEnemy(ArrayList<Enemy> enemies, Enemy boss)
	{
		int i;
		Rectangle heroBox = this.getMyRectangle();
		//Cycle through all enemies.
		for(i = 0; i < enemies.size(); i++)
		{
			//Use bounding rectangle for collision detection.
			//If not attacking, take damage.
			if(heroBox.intersects(enemies.get(i).getMyRectangle()) && !isAttacking)
			{
				hitPoints--;
			}
			else if(heroBox.intersects(enemies.get(i).getMyRectangle()) && isAttacking)
			{
				//Need to be facing the enemy for a hit to land.
				if(isFacingRight && enemies.get(i).getXPosn() > getXPosn())
				{
					enemies.get(i).wasHit();
					enemies.remove(i);
					break;	
				}
				else if(!isFacingRight && enemies.get(i).getXPosn() < getXPosn())
				{
					enemies.get(i).wasHit();
					enemies.remove(i);
					break;	
				}
			}
		}
		
		//Same checks, only for the boss enemy.
		if(heroBox.intersects(boss.getMyRectangle()) && !isAttacking && boss.isActive())
		{
			hitPoints--;
		}
		else if(heroBox.intersects(boss.getMyRectangle()) && isAttacking && boss.isActive())
		{
			if(isFacingRight && boss.getXPosn() > getXPosn())
			{
				boss.wasHit();
			}
			else if(!isFacingRight && boss.getXPosn() < getXPosn())
			{
				boss.wasHit();
			}
		}
		isAttacking = false;
	}
	
	/*
	 * Sets the hero into attack mode.
	 */
	public void attack()
	{
		if(!isFacingRight)
			setImage(4);
		else
			setImage(5);
		loopImage(period, DURATION); 
		isAttacking = true;
	}
	
	/*
	 * Checks if the hero is in attack mode.
	 */
	public boolean isAttacking()
	{
		return isAttacking;
	}
	
	/*
	 * Overrides the base Sprite method for updating.
	 * Updates position, checks for falling, and checks
	 * if vertical scrolling is necessary.
	 */
	public void updateSprite()
	{
		//Update position.
		if(!isStill)
		{
			if (isFacingRight)  // moving right
				xWorld += moveSize;
			else // moving leftx
				xWorld -= moveSize;
		}
		
		if(vertMoveMode == NOT_JUMPING)   // If not jumping.
			checkIfFalling();   // May have moved out into empty space.

		//Vertical movement has two components: RISING and FALLING.
		if(vertMoveMode == RISING)
			updateRising();
		else if (vertMoveMode == FALLING)
			updateFalling();

		//If the hero is going off of the panel in either direction,
		// move the world around it accordingly.
		if(locy < 0)
		{
			brickMan.moveDown();
		}
		else if(locy+(getHeight()*2) > getPHeight())
		{
			finishJumping();
			locy -= 4*brickMan.getMoveSizeY();
			yWorld -= 4*brickMan.getMoveSizeY();
			brickMan.moveUp();
		}		
		super.updateSprite();
	}

	/*
	 * Check the bricks beneath the hero to see if he can fall.
	 */
	private void checkIfFalling()
	{
		//Could the sprite move downwards if it wanted to?
		//Test its center x-coord, base y-coord
		int yTrans = brickMan.checkBrickTop(xWorld+(getWidth()/2), yWorld+getHeight()+vertStep, vertStep);
		if (yTrans != 0)   // yes it could
		{
			vertMoveMode = FALLING;   //Set it to be in falling mode
		}
	}

	/*
	 * If the hero is jumping, we need to update its location.
	 */
	private void updateRising()
	{		
		if(upCount == MAX_UP_STEPS)
		{
			vertMoveMode = FALLING;   //At top, now start falling.
			upCount = 0;
		}
		else
		{
			int yTrans = brickMan.checkBrickBase(xWorld+(getWidth()/2), yWorld-vertStep, vertStep);
			if(yTrans == 0)
			{
				vertMoveMode = FALLING;   //Start falling.
				upCount = 0;
			}
			else
			{
				translate(0, -yTrans);
				yWorld -= yTrans;   //Update position.
				upCount++;
			}
		}
	}

	/*
	 * If the hero is falling, we need to update its location.
	 */
	private void updateFalling()
	{
		int yTrans = brickMan.checkBrickTop(xWorld+(getWidth()/2), yWorld+getHeight()+vertStep, vertStep);
		if(yTrans == 0)   //Hit the top of a brick.
			finishJumping();
		else
		{
			translate(0, yTrans);
			yWorld += yTrans;   //Update position.
		}
	}

	/*
	 * Once jumping is done with, resume normal movement.
	 */
	private void finishJumping()
	{
		vertMoveMode = NOT_JUMPING;
		upCount = 0;
		if(isStill)
		{
			if(isFacingRight)
				setImage(1);
			else    //Facing left.
				setImage(0);
		}
	}
}