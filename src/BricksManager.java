import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.io.*;

/*
 * This class handles movement and drawing of all bricks in 
 * the brick map.
 */
public class BricksManager
{
	private final static String IMAGE_DIR = "Images/";
	private final static int MAX_BRICKS_LINES = 40;

	private final static double MOVE_FACTOR = 0.25;  

	private int pWidth, pHeight;
	private int width, height;


	private int imWidth, imHeight;
	private int numCols, numRows;


	private int moveSize;
	private int brickOffset;

	private boolean isMovingRight;
	private boolean isMovingLeft;
	private boolean isMovingUp;
	private boolean isMovingDown;

	private int xMapHead;
	/* The x-coord in the panel where the start of the bricks map
		(its head) should be drawn. 
		It can range between -width to width (exclusive), so can
		have a value beyond the confines of the panel (0-pWidth).

		As xMapHead varies, the on-screen map will usually
		be a combination of its tail followed by its head.

		xMapHead plays the same role as xImHead in a map object.*/


	private ArrayList<Brick> bricksList;
	private ArrayList<Brick>[] columnBricks;

	private ImagesLoader imsLoader;
	private ArrayList<BufferedImage> brickImages = null;

	public BricksManager(int w, int h, String fnm, ImagesLoader il)
	{
		pWidth = w; pHeight = h;
		imsLoader = il;

		bricksList = new ArrayList<Brick>();
		loadBricksFile(fnm);
		initBricksInfo();
		createColumns();

		moveSize = (int)(imWidth * MOVE_FACTOR);
		brickOffset = (int)(imHeight * MOVE_FACTOR);
		if(moveSize == 0)
		{
			System.out.println("moveSize cannot be 0, setting it to 1");
			moveSize = 1;
		}
		
		if(brickOffset == 0)
		{
			System.out.println("brickOffset cannot be 0, setting it to 1");
			brickOffset = 1;
		}

		isMovingRight = false;
		isMovingLeft = false;
		isMovingUp = false;
		isMovingDown = false;
		xMapHead = 0;
	}


	// ----------- Load the bricks information -------------------

	/*
	 * This method reads in the location of all the bricks from a configuration file.
	 */
	private void loadBricksFile(String fnm)
	{ 
		//Get the name of the file.
		String imsFNm = IMAGE_DIR + fnm;
		System.out.println("Reading bricks file: " + imsFNm);

		int numStripImages = -1;
		int numBricksLines = 0;
		try
		{
			//Read in the file.
			BufferedReader br = new BufferedReader( new FileReader(imsFNm));
			String line;
			char ch;
			//Read the file, line by line.
			while((line = br.readLine()) != null)
			{
				//Disregard these lines.
				if(line.length() == 0)
					continue;
				if(line.startsWith("//"))
					continue;
				
				//Change character to lower case.
				ch = Character.toLowerCase(line.charAt(0));
				//Load in brick image if an s.
				if(ch == 's')
					numStripImages = getStripImages(line);
				else
				{
					//Can only have MAX_BRICKS_LINES number of lines.
					if(numBricksLines > MAX_BRICKS_LINES) 
						System.out.println("Max reached, skipping bricks line: " + line);
					//Have to have at elast one brick image.
					else if(numStripImages == -1)
						System.out.println("No strip image, skipping bricks line: " + line);
					else
					{
						//Store the brick in the list.
						storeBricks(line, numBricksLines, numStripImages);
						numBricksLines++;
					}
				}
			}
			br.close();
		}
		//Error reading in file.
		catch(IOException e)
		{
			System.out.println("Error reading file: " + imsFNm);
			System.exit(1);
		}
	}


	/*
	 * Load in the brick images from the strip and stor them.
	 */
	@SuppressWarnings("unchecked")
	private int getStripImages(String line)
	{
		//Start processing the line.
		StringTokenizer tokens = new StringTokenizer(line);
		//Check for correct number of arguments.
		if(tokens.countTokens() != 3)
		{
			System.out.println("Wrong no. of arguments for " + line);
			return -1;
		}
		else
		{
			//Process image.
			tokens.nextToken();
			System.out.print("Bricks strip: ");

			String fnm = tokens.nextToken();
			int number = -1;
			try
			{
				//Load in images.
				number = Integer.parseInt(tokens.nextToken());
				imsLoader.loadStripImages(fnm, number);
				brickImages = imsLoader.getImages(getPrefix(fnm));
			}
			catch(Exception e)
			{
				System.out.println("Number is incorrect for " + line);
			}

			return number;
		}
	}

	/*
	 * Get the prefix for the file name.
	 */
	private String getPrefix(String fnm)
	{
		int posn;
		if((posn = fnm.lastIndexOf(".")) == -1)
		{
			System.out.println("No prefix found for filename: " + fnm);
			return fnm;
		}
		else
			return fnm.substring(0, posn);
	}

	/*
	 * Store the bricks from the line into the list.
	 */
	private void storeBricks(String line, int lineNo, int numImages)
	{
		int imageID;
		System.out.println(lineNo);
		//Process each individual brick.
		for(int x=0; x < line.length(); x++)
		{
			//Only care about specific characters. Ignore others.
			char ch = line.charAt(x);
			if(ch == ' ' || ch == 'd' || ch == 'e' || ch == 'b')
			{
				continue;
			}
			if(Character.isDigit(ch))
			{
				//Store the individual brick in the list.
				imageID = ch - '0';
				if(imageID >= numImages)
					System.out.println("Image ID " + imageID + " out of range");
				else
					bricksList.add(new Brick(imageID, x, lineNo));
			}
			else
				System.out.println("Brick char " + ch + " is not a digit");
		}
	}


	// --------------- Initialize bricks data structures -----------------

	/*
	 * Initialize the bricks map.
	 */
	private void initBricksInfo()
	{
		//Check for the number of images and bricks.
		if(brickImages == null)
		{
			System.out.println("No bricks images were loaded");
			System.exit(1);
		}

		if(bricksList.size() == 0)
		{
			System.out.println("No bricks map were loaded");
			System.exit(1);
		}

		//Set the variables for the bricks.
		BufferedImage im = brickImages.get(0);
		imWidth = im.getWidth();
		imHeight = im.getHeight(); 

		//Calculate various variables.
		findNumBricks();
		calcMapDimensions();
		checkForGaps();
		
		//Add images to the bricks.
		addBrickDetails();
	}

	/*
	 * Finds the number of rows and columns in the bricks map.
	 */
	private void findNumBricks()
	{
		Brick b;
		numCols = 0;
		numRows = 0;

		for(int i = 0; i < bricksList.size(); i++)
		{
			b = bricksList.get(i);
			if(numCols < b.getMapX())
				numCols = b.getMapX();
			if(numRows < b.getMapY())
				numRows = b.getMapY();
		}

		numCols++;
		numRows++;
	}


	/*
	 * Finds the dimensions of the brick map based on the 
	 * number of rows, columns, and the image height
	 */
	private void calcMapDimensions()
	{
		width = imWidth * numCols;
		height = imHeight * numRows;

		if(width < pWidth)
		{
			System.out.println("Bricks map is less wide than the panel");
			System.exit(0);
		}
	}

	/*
	 * Check for gaps in the bottom row of the bricks map.
	 * We do not want the hero to be able to fall out of the map.
	 */
	private void checkForGaps()
	{
		//Initialize the counter.
		boolean[] hasBrick = new boolean[numCols];
		for(int j=0; j < numCols; j++)
			hasBrick[j] = false;

		//Check each brick in the final row of map.
		Brick b;
		for(int i=0; i < bricksList.size(); i++)
		{
			b = bricksList.get(i);
			if(b.getMapY() == numRows-1)
				hasBrick[b.getMapX()] = true;   
		}

		//Check each boolean to determine if the map is good.
		for(int j=0; j < numCols; j++)
		{
			if(!hasBrick[j])
			{
				System.out.println("Gap found in bricks map bottom line at position " + j);
				System.exit(0);
			}
		}
	}
	
	/*
	 * Give each brick an image and a location.
	 */
	private void addBrickDetails()
	{
		Brick b;
		BufferedImage im;
		for(int i = 0; i < bricksList.size(); i++)
		{
			b = bricksList.get(i);
			im = brickImages.get( b.getImageID());
			b.setImage(im);
			b.setLocY(pHeight, numRows);
			b.setLocX(pWidth, numRows);
		}
	}

	/*
	 * Turns the brick array list into an array list sorted by x value.
	 * Does not sort the bricks by y value.
	 */
	@SuppressWarnings("unchecked")
	private void createColumns()
	{
		//Create columnBricks.
		columnBricks = new ArrayList[numCols];
		for(int i=0; i < numCols; i++)
			columnBricks[i] = new ArrayList<Brick>();

		//Sort the bricks into the columns.
		Brick b;
		for(int j=0; j < bricksList.size(); j++)
		{
			b = bricksList.get(j);
			columnBricks[b.getMapX()].add(b);    //Bricks not stored in any order.
		}
	}
	

	// ---------------------- Move the bricks map ---------------

	/*
	 * Sets the flags for the brick map to move right.
	 */
	public void moveRight()
	{
		isMovingRight = true;
		isMovingLeft = false;
	}

	/*
	 * Sets the flags for the brick map to move left.
	 */
	public void moveLeft()
	{
		isMovingRight = false;
		isMovingLeft = true;
	}
	
	/*
	 * Sets the flags for the brick map to move up.
	 */
	public void moveUp()
	{
		isMovingUp = true;
		isMovingDown = false;
	}
	
	/*
	 * Sets the flags for the brick map to move down.
	 */
	public void moveDown()
	{
		isMovingUp = false;
		isMovingDown = true;
	}

	/*
	 * Sets the flags for the brick map to stop moving horizontally.
	 */
	public void stayStill()
	{
		isMovingRight = false;
		isMovingLeft = false;
	}
	
	/*
	 * Sets the flags for the brick map to stop moving vertically.
	 */
	public void stayStillVert()
	{
		isMovingUp = false;
		isMovingDown = false;
	}
	
	/*
	 * Returns whether or not the brick map is moving right.
	 */
	public boolean isMovingRight()
	{
		return isMovingRight;
	}
	
	/*
	 * Returns whether or not the brick map is moving left.
	 */
	public boolean isMovingLeft()
	{
		return isMovingLeft;
	}
	
	/*
	 * Returns whether or not the brick map is moving up.
	 */
	public boolean isMovingUp()
	{
		return isMovingUp;
	}
	
	/*
	 * Returns whether or not the brick map is moving down.
	 */
	public boolean isMovingDown()
	{
		return isMovingDown;
	}

	/*
	 * Updates the location of all the bricks in the map based on movement.
	 */
	public void update()
	{
		//Update horizontal position, defined by the xMapHead.
		if(isMovingRight)
			xMapHead = (xMapHead + moveSize) % width;
		else if(isMovingLeft)
			xMapHead = (xMapHead - moveSize) % width;

		//Update the vertical position of all bricks, based on vertical scrolling.
		for(int i = 0; i < columnBricks.length; i++)
		{
			for(int j = 0; j < columnBricks[i].size(); j++)
			{
				if(isMovingUp)
				{
					((Brick)columnBricks[i].get(j)).setLocY(4 * -brickOffset);
				}
				else if(isMovingDown)
				{
					((Brick)columnBricks[i].get(j)).setLocY(brickOffset);
				}
			}
		}
	}



	// -------------- Draw the bricks ----------------------

	/*
	 * Displays the current set of visible bricks on the screen.
	 * There are four cases to consider:
	 * Map is moving right and the head of the map is showing.
	 * Map is moving right normally.
	 * Map is moving left normally.
	 * Map is moving left and the tail of the map is showing.
	 */
	public void display(Graphics g)
	{
		//Get the coordinate of the column.
		int bCoord = (int)(xMapHead/imWidth) * imWidth;
		int offset;
		
		//Clamp bCoord.
		if(bCoord >= 0)
			offset = xMapHead - bCoord;
		else
			offset = bCoord - xMapHead;
		
		//Cases
		if((bCoord >= 0) && (bCoord < pWidth))
			drawBricks(g, xMapHead, pWidth, 0);
		else if(bCoord >= pWidth)
			drawBricks(g, 0-(imWidth-offset), pWidth, width-bCoord-imWidth);
		else if((bCoord < 0) && (bCoord >= pWidth-width+imWidth))
			drawBricks(g, 0-offset, pWidth, -bCoord);
		else if (bCoord < pWidth-width+imWidth)
			drawBricks(g, 0-offset, width+xMapHead, -bCoord);
	}

	/*
	 * Draw only the bricks on the screen.
	 */
	private void drawBricks(Graphics g, int xStart, int xEnd, int xBrick)
	{
		int xMap = xBrick/imWidth;
		ArrayList<Brick> column;
		Brick b;
		//Loop through all columns currently on screen.
		for (int x = xStart; x < xEnd; x += imWidth)
		{
			column = columnBricks[ xMap ];
			for (int i=0; i < column.size(); i++)
			{
				b = (Brick) column.get(i);
				b.display(g, x);   //Draw brick b at JPanel position x.
			}
			xMap++;  //Examine the next column of bricks.
		}
	}

	// ----------------- HeroSprite related methods -------------
	// various forms of collision detection with the bricks

	/*
	 * Returns the height of hte brick.
	 */
	public int getBrickHeight()
	{
		return imHeight;
	}

	/*
	 * Called at sprite initialization to find a brick 
	 *	containing the xSprite location which is higher up
	 *	than other bricks containing that same location.
	 *	Return the brick's y position. 
	 *
	 *	xSprite is the same coordinate in the panel and the 
	 *	bricks map since the map has not moved yet.
	 *
	 *	xSprite is converted to an x-index in the brick map,
	 *	and this is used to search the relevant bricks column
	 *	for a max y location.
	 *
	 *	The returned y-location is the 'floor' of the bricks
	 *	where the sprite will be standing initially.
	 */
	public int findFloor(int xSprite)
	{
		int xMap = (int)(xSprite/imWidth);

		int locY = pHeight;
		ArrayList<Brick> column = columnBricks[xMap];
		
		Brick b;
		for(int i = 0; i < column.size(); i++)
		{
			b = column.get(i);
			if(b.getLocY() < locY)
			{
				locY = b.getLocY();
			}
		}
		return locY;
	}

	/*
	 * Returns the horizontal move size of the brick map.
	 */
	public int getMoveSizeX()
	{
		return moveSize;
	}
	
	/*
	 * Returns the vertical move size of the brick map.
	 */
	public int getMoveSizeY()
	{
		return brickOffset;
	}
	
	/*
	 * Determines if the point specified is inside of a brick of the map.
	 */
	public boolean insideBrick(int xWorld, int yWorld)
	{
		//Find the column that the point is in.
		Point mapCoord = worldToMap(xWorld, yWorld);
		ArrayList<Brick> column = columnBricks[mapCoord.x];

		//Check every brick in the column.
		Brick b;
		for(int i = 0; i < column.size(); i++)
		{
				b = column.get(i);
				//Determine if a collision has occurred.
				if(mapCoord.y == ((Point)worldToMap(b.getLocX(), b.getLocY())).y)
				{
					return true;
				}
		}
		return false;
	}
	
	/*
	 * Returns the brick hit by the hero.
	 */
	public Brick getHitBrick(int xWorld, int yWorld)
	{
		//Find the column that the point is in.
		Point mapCoord = worldToMap(xWorld, yWorld);
		ArrayList<Brick> column = columnBricks[mapCoord.x];

		//Check every brick in the column.
		Brick brick = null;
		Brick b;
		for(int i = 0; i < column.size(); i++)
		{
				b = column.get(i);
				//Determine if a collision has occurred.
				if(mapCoord.y == ((Point)worldToMap(b.getLocX(), b.getLocY())).y)
				{
					brick = b;
				}
		}
		return brick;
	}

	/*
	 * This method transforms a point of world space into a map grid coordinate.
	 */
	private Point worldToMap(int xWorld, int yWorld)
	{
		//Translate x-coordinate.
		xWorld = xWorld % width;
		if(xWorld < 0)
			xWorld += width;
		int mapX = (int)(xWorld/imWidth);

		//Translate y-coordinate.
		yWorld = yWorld - (pHeight-height);
		int mapY = (int) (yWorld/imHeight);

		//Clamp y-value.
		if(yWorld < 0)
			mapY = mapY-1;
		
		return new Point(mapX, mapY);
	}

	/*
	 * This method find the distance between the character and the
	 * bottom of the brick above it.
	 */
	public int checkBrickBase(int xWorld, int yWorld, int step)
	{
		//Only returns a value if inside the brick.
		if(insideBrick(xWorld, yWorld))
		{
			int yMapWorld = yWorld - (pHeight-height);
			int mapY = (int)(yMapWorld/imHeight);
			int topOffset = yMapWorld - (mapY * imHeight);
			int smallStep = step - (imHeight-topOffset);
			return smallStep;
		}
		return step;
	}

	/*
	 * This method finds the distance between the character and the
	 * top of the brick below it.
	 */
	public int checkBrickTop(int xWorld, int yWorld, int step)
	{
		//Only returns a value if inside the brick.
		if(insideBrick(xWorld, yWorld))
		{
			int yMapWorld = yWorld - (pHeight-height);
			int mapY = (int)(yMapWorld/imHeight);
			int topOffset = yMapWorld - (mapY * imHeight);
			int smallStep = step - topOffset;
			return smallStep;
		}
		return step;
	}
	
	/*
	 * Removes the specified brick from the brick map.
	 * This is used to remove keys the character has run into from the map.
	 */
	public void deleteBrick(Brick b)
	{
		columnBricks[b.getMapX()].remove(b);
	}
	
	/*
	 * Returns the width of the brick map.
	 */
	public int getWidth()
	{
		return width;
	}
	
	/*
	 * Returns the height of the brick map.
	 */
	public int getHeight()
	{
		return height;
	}
}
