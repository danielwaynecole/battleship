import java.util.Random;

// This player is as rudimentary as it gets.  It simply puts the ships in a static 
// location, and makes random moves until one sticks.  Your player can use this 
// as a base to expand upon. It is a good idea to play against this player until yours
// gets good enough to beat it regularly.

public class Player1 extends Player {
	private int xcoord;
	private int ycoord;
	private boolean shipDir;
	private char shipBoard[][] = new char[10][10];
	private int shipLength;

	// You must call the super to establish the necessary game variables
	// and register the game.
	public Player1(int playerNum) {
		super(playerNum);
		for(int i=0; i<10; i++){
			for(int j=0; j<10; j++) {
				shipBoard[i][j] = 'e';
			}
		}
	}

	public void makeMove() {
		// Try making a move until successful
		while(!game.makeMove(hisShips, myMoves, randomRow(), randomCol()));

		numMoves++;
		System.out.println("Player " + myPlayerNum + " num Moves = " + numMoves);
	}

	public boolean addShips() {
		placeShip(Ships.SHIP_CARRIER);
		game.putShip(myShips, Ships.SHIP_CARRIER, ycoord, xcoord, shipDir ? Ships.SHIP_EAST : Ships.SHIP_SOUTH);
		setShipBoard(shipLength, xcoord, ycoord, shipDir);
		
		placeShip(Ships.SHIP_BATTLESHIP);
		game.putShip(myShips, Ships.SHIP_BATTLESHIP, ycoord, xcoord, shipDir ? Ships.SHIP_EAST : Ships.SHIP_SOUTH);
		setShipBoard(shipLength, xcoord, ycoord, shipDir);
		
		placeShip(Ships.SHIP_CRUISER);
		game.putShip(myShips, Ships.SHIP_CRUISER, ycoord, xcoord, shipDir ? Ships.SHIP_EAST : Ships.SHIP_SOUTH);
		setShipBoard(shipLength, xcoord, ycoord, shipDir);
		
		placeShip(Ships.SHIP_DESTROYER);
		game.putShip(myShips, Ships.SHIP_DESTROYER, ycoord, xcoord, shipDir ? Ships.SHIP_EAST : Ships.SHIP_SOUTH);
		setShipBoard(shipLength, xcoord, ycoord, shipDir);
		
		placeShip(Ships.SHIP_SUBMARINE);
		game.putShip(myShips, Ships.SHIP_SUBMARINE, ycoord, xcoord, shipDir ? Ships.SHIP_EAST : Ships.SHIP_SOUTH);
		setShipBoard(shipLength, xcoord, ycoord, shipDir);
		
		
//		game.putShip(myShips, Ships.SHIP_CARRIER, 1, 4, Ships.SHIP_EAST);
//		game.putShip(myShips, Ships.SHIP_BATTLESHIP, 5, 5, Ships.SHIP_EAST);
//		game.putShip(myShips, Ships.SHIP_CRUISER, 6, 7, Ships.SHIP_EAST);
//		game.putShip(myShips, Ships.SHIP_DESTROYER, 8, 3, Ships.SHIP_EAST);
//		game.putShip(myShips, Ships.SHIP_SUBMARINE, 5, 1, Ships.SHIP_SOUTH);

		return true;
	}
	
	public void placeShip(int ship) {	
		Random generator = new Random();
		Boolean successful = false;	
		shipLength = ship;
		if(ship == 1) { shipLength = 3; }

			do {
				Integer orientation = generator.nextInt(2);
	            if(orientation == 0)
	            	shipDir = true; // Horizontal
	            else
	            	shipDir = false; // Vertical
            
	            int x = generator.nextInt(10)+1;
	            int y = generator.nextInt(10)+1;
	            
	            successful = checkShip(x, y, shipLength, shipDir);
	            						
			} while(!successful);
		System.out.println("placing ship type = " + ship + " at location: (" + xcoord + ", " + ycoord + ") with layout: " + shipDir);
		}

	
	public boolean checkShip(int xval, int yval, int shipSize, boolean horizontal) {

		int length = shipSize;
		if(length == 0){ return true; }
		//check if in bounds
		if(xval > 10 || yval > 10 ) return false;
		if(xval <1 || yval <1 ) return false;
		if (shipBoard[xval-1][yval-1] != 'e'){
			return false;
		}
		if(horizontal){
			if(checkShip(xval+1, yval, --length, horizontal)) {
				xcoord = xval;
				ycoord = yval;
				return true;
			}
			else{
				return false;
			}
			
		}
		else{
			yval++;
			if(checkShip(xval, yval, --length, horizontal)){
				yval--;
				xcoord = xval;
				ycoord = yval;
				return true;
			}
			else{
				return false;
			}
		}
		
	}
	
	public void setShipBoard(int length, int x, int y, boolean dir) {
		if(dir){
			for(int m=x; m<length + x; m++){
				shipBoard[m-1][y-1] = 's';
			}
		}
		else{
			for(int d=y; d<length + y; d++){
				shipBoard[x-1][d-1] = 's';
			}
		}
	}


}
