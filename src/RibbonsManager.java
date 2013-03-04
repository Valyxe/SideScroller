
import java.awt.*;

/*
 * This class manages movement of the background ribbons.
 */
public class RibbonsManager
{
	private String ribImages[] = {"Far", "Middle", "Near"};
	private double moveFactors[] = {0.1, 0.5, 1.0};

	private Ribbon[] ribbons;
	private int numRibbons;
	private int moveSize;

	public RibbonsManager(int w, int h, int brickMvSz, ImagesLoader imsLd)
	{
		moveSize = brickMvSz;

		numRibbons = ribImages.length;
		ribbons = new Ribbon[numRibbons];

		for(int i = 0; i < numRibbons; i++)
			ribbons[i] = new Ribbon(w, h, imsLd.getImage( ribImages[i] ), (int)(moveFactors[i]*moveSize));
	}

	/*
	 * Sets all ribbons to moving right.
	 */
	public void moveRight()
	{
		for(int i=0; i < numRibbons; i++)
			ribbons[i].moveRight();
	}

	/*
	 * Sets all ribbons to moving left.
	 */
	public void moveLeft()
	{
		for(int i=0; i < numRibbons; i++)
			ribbons[i].moveLeft();
	}

	/*
	 * Sets all ribbons to staying still.
	 */
	public void stayStill()
	{
		for(int i=0; i < numRibbons; i++)
			ribbons[i].stayStill();
	}
	
	/*
	 * Updates all ribbons.
	 */
	public void update()
	{
		for(int i=0; i < numRibbons; i++)
			ribbons[i].update();
	}

	/*
	 * Displays all ribbons.
	 */
	public void display(Graphics g)
	{
		for(int i=0; i < numRibbons; i++)
			ribbons[i].display(g);
	}
}
