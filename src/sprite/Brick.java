package sprite;
import java.util.ArrayList;
import com.badlogic.gdx.graphics.Texture;


/*
 * This class encompasses all data for each individual brick on the screen.
 */
public class Brick extends Entity
{
	public Brick(int x, int y, ArrayList<Texture> images, EntityType type)
	{
		super(x, y, images, type);
	}
}

