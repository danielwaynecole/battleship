
public class PlayerMDM extends Player{

	public PlayerMDM(int playerNum) {
		super(playerNum);
	}
	
	public void makeMove() {
		// Try making a move until successful
		while(!game.makeMove(hisShips, myMoves, randomRow(), randomCol()));

		numMoves++;
		System.out.println("Player " + myPlayerNum + " num Moves = " + numMoves);
	}

	public boolean addShips() {
		
		game.putShip(myShips, Ships.SHIP_CARRIER, 2, 2, Ships.SHIP_SOUTH);
		game.putShip(myShips, Ships.SHIP_BATTLESHIP, 5, 5, Ships.SHIP_EAST);
		game.putShip(myShips, Ships.SHIP_CRUISER, 6, 7, Ships.SHIP_EAST);
		game.putShip(myShips, Ships.SHIP_DESTROYER, 8, 3, Ships.SHIP_EAST);
		game.putShip(myShips, Ships.SHIP_SUBMARINE, 9, 9, Ships.SHIP_NORTH);

		return true;
	}

}
