package sprite;
import java.util.ArrayList;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

/*
 * This is the parent class for all Sprites in this game.
 */
public class Entity 
{
	//Default dimensions when there is no image
	protected static final int SIZE = 12;

	//Image-related
	private ArrayList<Texture> ims;
	private TextureRegion[] currentImage;
	private Animation currentAnimation;
	private SpriteBatch spriteBatch;
	private TextureRegion currentFrame;
	private int width, height, locx, locy;
	private float stateTime;
	private boolean isActive = true;
	
	private EntityType eType;

	public Entity(int x, int y, ArrayList<Texture> images, EntityType type) 
	{
		locx = x; locy = y;
		ims = images;
		setImage(0, 1, 1);
		eType = type;
	}

	/*
	 * This method sets the current image set of the Sprite
	 * to the specified number.
	 */
	public void setImage(int num, int cols, int rows)
	{
		//Set to initial image of set.		
		TextureRegion[][] tmp = TextureRegion.split(ims.get(num), ims.get(num).getWidth()/cols, ims.get(num).getHeight()/rows);
		currentImage = new TextureRegion[cols * rows];
		int index = 0;
		for(int i = 0; i < rows; i++)
		{
			for (int j = 0; j < cols; j++)
			{
				currentImage[index++] = tmp[i][j];
			}
		}
		
		if(currentImage == null)
		{
			System.out.println("No sprite image for " + this.getClass() + " " + num);
			width = SIZE;
			height = SIZE;
		}
		else
		{
			width = ims.get(num).getWidth()/cols;
			height = ims.get(num).getHeight()/rows;
		}
		
		currentAnimation = new Animation(0.025f, currentImage);
        spriteBatch = new SpriteBatch();
        stateTime = 0f;
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
	 * Returns the type of the entity.
	 */
	public EntityType getType()
	{
		return eType;
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
	 * Draw the Sprite at its location on the screen.
	 */
	public void render()
	{
		stateTime += Gdx.graphics.getDeltaTime();
        currentFrame = currentAnimation.getKeyFrame(stateTime, true);
        spriteBatch.begin();
        spriteBatch.draw(currentFrame, locx, locy);
        spriteBatch.end();
	}
	
	/*
	 * Gets a bounding rectangle for the Sprite using its current image.
	 * This is used for collision detection.
	 */
	public BoundingBox getBoundingBox()
	{
		return new BoundingBox(new Vector3(locx, locy, 0), new Vector3(locx + width, locy + height, 0));
	}
}
