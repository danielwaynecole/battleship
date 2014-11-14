//This is the base class for the player.  the only 2 methods you must implement are
//
//	public abstract boolean addShips();
//
//	public abstract void makeMove();
//
//All other routines are optional, but may come in handy when you are building your
//player.
//
//

import java.util.Random;


public abstract class Player {

	public int numMoves;
	public boolean startingTurn;
	protected char myShipsBoard[][];
	protected char myPegsBoard[][];
	protected BSGame game;
	
	// These variables are available to you at runtime.  
	protected int myPlayerNum;	// Which player you are (1 or 2)
	int myShips;				// Identifier of your ships, read only
	int hisShips;				// Identifier of your opponents ships, (not used by you)
	int myMoves;				// Identifiex of your moves, read only
	Random rnd = new Random();
	
	// The place where you will populate the board with your ships
	// The server will call you to have you add your ships
	public abstract boolean addShips();

	// The server will call you to make a move. 
	public abstract void makeMove();


	public Player(int playerNum) {

		if ((playerNum < 1) || (playerNum > 2)) {
			System.out.println("Player must be 1 or 2 in Player Constructor");
			System.exit(0);
		}
		numMoves = 0;
		myPlayerNum = playerNum;

		// Determine if we are player 1 or 2
		// and establish the boards
		if (myPlayerNum == BSGame.GAME_PLAYER1) {
			myShips = BSGame.BOARD_P1SHIPS;
			hisShips = BSGame.BOARD_P2SHIPS;
			myMoves = BSGame.BOARD_P1MOVES;
		} else {
			myShips = BSGame.BOARD_P2SHIPS;
			hisShips = BSGame.BOARD_P1SHIPS;
			myMoves = BSGame.BOARD_P2MOVES;
		}


	}

	// Leave this one alone
	// This assigns your boards to you
	public void registerBoards(char pegs[][], char ships[][]) {
		myShipsBoard = ships;
		myPegsBoard = pegs;
	}

	// Leave this one alone
	// It is used to register the game for the server
	public void registerGame(BSGame game) {
		this.game = game;
	}

	// Random row generator
	// Probably not necessary in your artificially intelligent implementation
	protected int randomRow() {
		return rnd.nextInt(10) + 1;
	}

	// Random column generator
	// Probably not necessary in your artificially intelligent implementation
	protected int randomCol() {
		return rnd.nextInt(10) + 1;
	}

	// ship feedback on sink.  These are callbacks.  You will be called by the system
	// when one of your ships sinks.  You may want to override these to do what you
	// need

	public void sankCarrier() {
		System.out.println("You Sank my Carrier(p" + myPlayerNum + ")");

	}

	public void sankBattleShip() {
		System.out.println("You Sank my Battleship(p" + myPlayerNum + ")");

	}

	public void sankCruiser() {
		System.out.println("You Sank my Cruiser(p" + myPlayerNum + ")");

	}

	public void sankDestroyer() {
		System.out.println("You Sank my Destroyer(p" + myPlayerNum + ")");

	}

	public void sankSubmarine() {
		System.out.println("You Sank my Submarine(p" + myPlayerNum + ")");
	}

	// Utility method to return the length of a ship, in case you need it
	// Do not modify this method
	public int getShipLength(int shipType) {
		switch (shipType) {
		case Ships.SHIP_CARRIER:
			return 5;
		case Ships.SHIP_BATTLESHIP:
			return 4;
		case Ships.SHIP_CRUISER:
			return 3;
		case Ships.SHIP_SUBMARINE:
			return 3;
		case Ships.SHIP_DESTROYER:
			return 2;
		default:
			throw new RuntimeException("Unhandled ship type!");
		}
	}


}
