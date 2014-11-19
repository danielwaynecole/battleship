
public class PlayerMDM extends Player{
	public static final int DIAGONALFIRINGSTRATEGY = 0;
	public static final int SHIPSINKFIRINGSTRATEGY = 1;
	public static final int COLSPERQUADRANT = 5;
	public static final int ROWSPERQUADRANT = 5;
	public static final int VERTICAL = 0;
	public static final int HORIZONTAL = 1;
	public static final int LEFT = 0;
	public static final int UP = 1;
	public static final int RIGHT = 2;
	public static final int DOWN = 3;
	public static final int UNKNOWN = -1;

	private boolean activeSinking = false;
	private boolean justSank = false;
	private int[] firstHit = new int[2];
	private int[] lastHit = new int[2];
	private int lastMoveResult = UNKNOWN;
	private int orientation = UNKNOWN;
	private int direction = LEFT;
	private int currentQuadrant = 0;
	private int strategy = DIAGONALFIRINGSTRATEGY; // can be overridden
	
	public PlayerMDM(int playerNum) {
		super(playerNum);
	}
	
	public void makeMove() {
		// Try making a move until successful
		int row = 0;
		int col = 0;
		int[] nextMove = this.getNextMove();
		row = nextMove[0] + 1;
		col = nextMove[1] + 1;
		boolean moveSuccess = game.makeMove(hisShips, myMoves, row, col);
		if(!moveSuccess)
			System.exit(0);
		this.lastMoveResult = game.getMoveBoardValue(myMoves, row, col);
		if(this.justSank){
			this.justSank = false;
		} else if(this.lastMoveResult == BSGame.PEG_HIT && !this.activeSinking && !this.justSank){
			lastHit[0] = nextMove[0];
			lastHit[1] = nextMove[1];
			firstHit[0] = nextMove[0];
			firstHit[1] = nextMove[1];
			this.activeSinking = true;
			this.strategy = SHIPSINKFIRINGSTRATEGY;
		} else if (this.lastMoveResult == BSGame.PEG_MISS && this.activeSinking){
			this.direction++;
		} else if (this.lastMoveResult == BSGame.PEG_HIT && this.activeSinking){
			if(this.direction == UP || this.direction == DOWN){
				this.orientation = VERTICAL;
			} else if(this.direction == LEFT || this.direction == RIGHT){
				this.orientation = HORIZONTAL;
			}
			lastHit[0] = nextMove[0];
			lastHit[1] = nextMove[1];
		}
		numMoves++;
		System.out.println("Player " + myPlayerNum + " num Moves = " + numMoves);
	}
	
	private int[] getNextMove(){
		int[] moves;
		switch(this.strategy){
		case DIAGONALFIRINGSTRATEGY:
			moves = this.getDiagonalFiringNextShot();
			break;
		case SHIPSINKFIRINGSTRATEGY:
			moves = this.getSinkShipNextShot();
			break;
		default:
			moves = this.getDiagonalFiringNextShot();
			break;
		}
		return moves;
	}
	
	private int[] getSinkShipNextShot(){
		int[] moves = new int[2];
		int left = lastHit[1] - 1;
		int right = lastHit[1] + 1;
		int down = lastHit[0] + 1;
		int up = lastHit[0] - 1;
		
		if(this.orientation == UNKNOWN){
			// try left
			if((this.direction == LEFT) && left >= 0 && left < 10 && game.getMoveBoardValue(myMoves, lastHit[0] + 1, left + 1) == BSGame.PEG_EMPTY){
				moves[0] = lastHit[0];
				moves[1] = left;
				return moves;
			}
			// try up
			if(this.direction == UP && up >= 0 && up < 10 && game.getMoveBoardValue(myMoves, up + 1, lastHit[1] + 1) == BSGame.PEG_EMPTY){
				moves[0] = up;
				moves[1] = lastHit[1];
				return moves;
			}
			// try right
			if(this.direction == RIGHT && right >= 0 && right < 10 && game.getMoveBoardValue(myMoves, lastHit[0] + 1, right + 1) == BSGame.PEG_EMPTY){
				moves[0] = lastHit[0];
				moves[1] = right;
				return moves;
			}
			
			// try down
			if(this.direction == DOWN && down >= 0 && down < 10 && game.getMoveBoardValue(myMoves, down + 1, lastHit[1] + 1) == BSGame.PEG_EMPTY){
				moves[0] = down;
				moves[1] = lastHit[1];
				return moves;
			}
		} else if(this.orientation == HORIZONTAL){
			if(this.direction == LEFT){
				if(this.lastMoveResult == BSGame.PEG_HIT && (lastHit[1] - 1 >= 0)
						&& game.getMoveBoardValue(myMoves, lastHit[0] + 1, lastHit[1]) == BSGame.PEG_EMPTY){
					moves[0] = lastHit[0];
					moves[1] = lastHit[1] - 1;
					return moves;
				} else {
					this.direction = RIGHT;
					moves[0] = firstHit[0];
					moves[1] = firstHit[1] + 1;
					return moves;
				}
			} else {
				if(this.lastMoveResult == BSGame.PEG_HIT && (lastHit[1] + 1 < 10)
						&& game.getMoveBoardValue(myMoves, lastHit[0] + 1, lastHit[1] + 2) == BSGame.PEG_EMPTY){
					moves[0] = lastHit[0];
					moves[1] = lastHit[1] + 1;
					return moves;
				} else {
					this.direction = LEFT;
					moves[0] = firstHit[0];
					moves[1] = firstHit[1] - 1;
					return moves;
				}
			}
		} else if(this.orientation == VERTICAL){
			if(this.direction == UP){
				if(this.lastMoveResult == BSGame.PEG_HIT && (lastHit[0] - 1 >= 0)
						&& game.getMoveBoardValue(myMoves, lastHit[0], lastHit[1] + 1) == BSGame.PEG_EMPTY){
					int rowMove = lastHit[0] - 1;
					int colMove = lastHit[1];
					System.out.println(rowMove + ":" + colMove + ":" + (game.getMoveBoardValue(myMoves, lastHit[0], lastHit[1] + 1) == BSGame.PEG_EMPTY));
					moves[0] = lastHit[0] - 1;
					moves[1] = lastHit[1];
					return moves;
				} else {
					this.direction = DOWN;
					moves[0] = firstHit[0] + 1;
					moves[1] = firstHit[1];
					return moves;
				}
			} else {
				if(this.lastMoveResult == BSGame.PEG_HIT && (lastHit[0] + 1 < 10)
						&& game.getMoveBoardValue(myMoves, lastHit[0] + 2, lastHit[1] + 1) == BSGame.PEG_EMPTY){
					moves[0] = lastHit[0] + 1;
					moves[1] = lastHit[1];
					return moves;
				} else {
					this.direction = UP;
					moves[0] = firstHit[0] - 1;
					moves[1] = firstHit[1];
					return moves;
				}
			}
		}
		return moves;
	}
	
	public void sankCarrier() {
		this.resetSinkRoutine();
		System.out.println("You Sank my Carrier(p" + myPlayerNum + ")");
	}

	public void sankBattleShip() {
		this.resetSinkRoutine();
		System.out.println("You Sank my Battleship(p" + myPlayerNum + ")");
	}

	public void sankCruiser() {
		this.resetSinkRoutine();
		System.out.println("You Sank my Cruiser(p" + myPlayerNum + ")");

	}

	public void sankDestroyer() {
		this.resetSinkRoutine();
		System.out.println("You Sank my Destroyer(p" + myPlayerNum + ")");

	}

	public void sankSubmarine() {
		this.resetSinkRoutine();
		System.out.println("You Sank my Submarine(p" + myPlayerNum + ")");
	}
	
	private void resetSinkRoutine(){
		this.activeSinking = false;
		this.justSank = true;
		this.strategy = DIAGONALFIRINGSTRATEGY;	
		this.orientation = UNKNOWN;
		this.direction = LEFT;
	}
	
	private int[] getDiagonalFiringNextShot(){
		int row = 0;
		int col = 0;
		int[] moves = new int[2];
		switch(this.currentQuadrant){
		case 0:
			while(!this.isOpen(row, col)){
				if(row <= 4 && col <= 4){
					row++;
					col++;
				} else {
					if(row == (COLSPERQUADRANT)){
						row = 0;
						col = 2;
					} else if(row == (COLSPERQUADRANT - 2)){
						row = 0;
						col = 4;
					} else if(row == (COLSPERQUADRANT - 4)){
						row = 2;
						col = 0;
					} if(col == (COLSPERQUADRANT + 1)){
						row = 4;
						col = 0;
					}
				}
			} 
			moves[0] = row;
			moves[1] = col;
			this.currentQuadrant++;
			break;
		case 1:
			row = 0;
			col = 9;
			while(!this.isOpen(row, col)){
				if(col >= 5 && row <= 4){
					row++;
					col--;
				} else {
					if(row == (COLSPERQUADRANT)){
						col = 7;
						row = 0;
					} else if(row == (COLSPERQUADRANT - 2)){
						col = 5;
						row = 0;
					} else if(row == (COLSPERQUADRANT - 4)){
						col = 9;
						row = 2;
					} if(row == (COLSPERQUADRANT)){
						col = 9;
						row = 4;
					}
				}
			}
			moves[0] = row;
			moves[1] = col;
			this.currentQuadrant++;	
			break;
			
		case 2:
			row = 9;
			col = 0;
			while(!this.isOpen(row, col)){
				if(row >= 5 && col <= 4){
					row--;
					col++;
				} else {
					if(col == (COLSPERQUADRANT)){
						col = 2;
						row = 9;
					} else if(col == (COLSPERQUADRANT - 2)){
						col = 4;
						row = 9;
					} else if(col == (COLSPERQUADRANT - 4)){
						col = 0;
						row = 7;
					} if(col == (COLSPERQUADRANT)){
						col = 4;
						row = 9;
					}
				}
			}
			moves[0] = row;
			moves[1] = col;
			this.currentQuadrant++;	
			break;
		case 3:
			row = 9;
			col = 9;
			while(!this.isOpen(row, col)){
				if(row >= 5 && col >= 5){
					row--;
					col--;
				} else {
					if(col == (COLSPERQUADRANT - 1)){
						row = 7;
						col = 9;
					} else if(col == (COLSPERQUADRANT - 3)){
						row = 5;
						col = 9;
					} if(col == (COLSPERQUADRANT - 1)){
						row = 9;
						col = 7;
					}
				}
			}
			moves[0] = row;
			moves[1] = col;
			this.currentQuadrant = 0;	
			break;
		}
		return moves;
	}
	
	private boolean isOpen(int row, int col){
		return game.getMoveBoardValue(myMoves, row + 1, col + 1) == BSGame.PEG_EMPTY;
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
