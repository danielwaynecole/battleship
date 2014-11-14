// This class contains the methods that are used to put ships on the board.
// This is a utility class and is not used by anybody but makeMove in the Game class.
// You don't need to concern yourself with this class.
//

public class Ships {
	private Ship ships[][] = new Ship[4][SHIP_COUNT];

	public static final int SHIP_COUNT = 5;
	
	public static final int SHIP_NORTH = 0;
	public static final int SHIP_EAST = 1;
	public static final int SHIP_SOUTH = 2;
	public static final int SHIP_WEST = 3;
	
	public static final int SHIP_CARRIER = 5;  		// length 5
	public static final int SHIP_BATTLESHIP = 4;	// length 4
	public static final int SHIP_CRUISER = 3;		// length 3
	public static final int SHIP_SUBMARINE = 1;		// length 3
	public static final int SHIP_DESTROYER = 2;		// length 2;

	
	public Ships() {
		for (int i = 0; i < 4; i++) {
			for (int j = 0;j < SHIP_COUNT; j++) {
				ships[i][j] = new Ship();
				ships[i][j].setType(i+1);
			}
		}
	}
	
	public boolean putShip(char board[][][], int bd, int type, int row, int col, int dir) {
		
		int length = type;
		
		// The length is the type except for the submarine
		if (type == SHIP_SUBMARINE) {
			length = 3;
		}
		
		// TODO: verify the ship will not fall off the board
		
		ships[bd][type-1].setRow(row);
		ships[bd][type-1].setCol(col);
		ships[bd][type-1].setDirection(dir);

		int irow = row;
		int icol = col;
		// draw the ship
		for (int i = 0; i < length; i++) {
			if (board[bd][irow-1][icol-1] == BSGame.PEG_SHIP) {
				return false;
			}
			board[bd][irow-1][icol-1] = BSGame.PEG_SHIP;

			switch(dir) {
			case SHIP_NORTH:
				irow--;
				break;
				
			case SHIP_SOUTH:
				irow++;
				break;
				
			case SHIP_EAST:
				icol++;
				break;
				
			case SHIP_WEST:
				icol--;
				break;
				
			default:
				break;
				
			}
		}
		return true;
	}

	public boolean shipSunk(char board[][][], int shipBd, int pegBd, int type) {
		int length = type;
		
		// The length is the type except for the submarine
		if (type == SHIP_SUBMARINE) {
			length = 3;
		}
		
		// TODO: verify the ship will not fall off the board
		
		int row = ships[shipBd][type-1].getRow();
		int col = ships[shipBd][type-1].getCol();
		int dir = ships[shipBd][type-1].getDirection();

		int irow = row;
		int icol = col;
		// draw the ship
		for (int i = 0; i < length; i++) {
			
			// If there is not a ship peg, this has not been sunk
			if (board[pegBd][irow-1][icol-1] != BSGame.PEG_HIT) {
				return false;
			}
			
			// Adjust the direction
			switch(dir) {
			case SHIP_NORTH:
				irow--;
				break;
				
			case SHIP_SOUTH:
				irow++;
				break;
				
			case SHIP_EAST:
				icol++;
				break;
				
			case SHIP_WEST:
				icol--;
				break;
				
			default:
				break;
				
			}
		}
		
		// Adios ship
		return true;
	}
	
	
}
