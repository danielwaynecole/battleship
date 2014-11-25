import java.util.Random;

public class PlayerMDM extends Player{
	public static final int EVERYOTHERFIRINGSTRATEGY = 0;
	public static final int DIAGONALFIRINGSTRATEGY = 1;
	public static final int SHIPSINKFIRINGSTRATEGY = 2;
	public static final int COLSPERQUADRANT = 5;
	public static final int ROWSPERQUADRANT = 5;
	public static final int VERTICAL = 0;
	public static final int HORIZONTAL = 1;
	public static final int LEFT = 0;
	public static final int UP = 1;
	public static final int RIGHT = 2;
	public static final int DOWN = 3;
	public static final int UNKNOWN = -1;
	public static final int NORTHWEST = 0;
	public static final int NORTHEAST = 1;
	public static final int SOUTHWEST = 2;
	public static final int SOUTHEAST = 3;

	private boolean activeSinking = false;
	private boolean justSank = false;
	private int startCorner;
	private int firingPlane;
	private int[] firstHit = new int[2];
	private int[] lastHit = new int[2];
	private Hit[] sunk = new Hit[20];
	private int sunkIndex = 0;
	private int currentHitIndex = 0;
	private Hit[] currentHits = new Hit[20];
	private int[] lastMove = new int[2];
	private int lastMoveResult = UNKNOWN;
	private int orientation = UNKNOWN;
	private int direction = LEFT;
	private int currentQuadrant = 0;
	private boolean[] quadrantFull = new boolean[] {false, false, false, false};
	private int strategy = EVERYOTHERFIRINGSTRATEGY; // can be overridden
	private int xcoord;
	private int ycoord;
	private boolean shipDir;
	private char shipBoard[][] = new char[10][10];
	private int shipLength;
	
	private class Hit{
		int row;
		int col;
		
		public Hit(int row, int col) {
			this.row = row;
			this.col = col;
		}
	}
	public PlayerMDM(int playerNum) {
		super(playerNum);
		for(int i=0; i<10; i++){
			for(int j=0; j<10; j++) {
				shipBoard[i][j] = 'e';
			}
		}
		for(int i = 0; i < quadrantFull.length; i++){
			quadrantFull[i] = false;
		}
		if(strategy == EVERYOTHERFIRINGSTRATEGY){
			startCorner = randInt(0, 3);
			firingPlane = randInt(0, 1);
		}
	}
	
	public void makeMove() {
		System.out.println("start my move");
		// Try making a move until successful
		int row = 0;
		int col = 0;
		int[] nextMove = this.getNextMove();
		row = nextMove[0] + 1;
		col = nextMove[1] + 1;
		// in case we did something crazy
		if(game.getMoveBoardValue(myMoves,row,col) != BSGame.PEG_EMPTY){
			nextMove = this.getNextOpen();
			row = nextMove[0] + 1;
			col = nextMove[1] + 1;
			resetSinkRoutine(0);
		}
		lastMove[0] = nextMove[0];
		lastMove[1] = nextMove[1];
		System.out.println("making move");
		boolean moveSuccess = game.makeMove(hisShips, myMoves, row, col);
		if(!moveSuccess){
			System.out.println("Houston, we have a problem. Invalid move. (" + row + "," + col + ").");
			System.exit(0);
		}
		this.lastMoveResult = game.getMoveBoardValue(myMoves, row, col);
		System.out.println("my last move result: " + (char)this.lastMoveResult);
		if(this.justSank){
			this.justSank = false;
		} else if(this.lastMoveResult == BSGame.PEG_HIT && !this.activeSinking && !this.justSank){
			lastHit[0] = nextMove[0];
			lastHit[1] = nextMove[1];
			firstHit[0] = nextMove[0];
			firstHit[1] = nextMove[1];
			System.out.println("hit at " + lastHit[0] + ":" + lastHit[1]);
			this.activeSinking = true;
			this.strategy = SHIPSINKFIRINGSTRATEGY;
			currentHits[currentHitIndex] = new Hit(nextMove[0], nextMove[1]);
			currentHitIndex++;
		} else if (this.lastMoveResult == BSGame.PEG_MISS && this.activeSinking && this.orientation == UNKNOWN){
			this.direction++;
		} else if (this.lastMoveResult == BSGame.PEG_HIT && this.activeSinking){
			currentHits[currentHitIndex] = new Hit(nextMove[0], nextMove[1]);
			currentHitIndex++;
			if(this.direction == UP || this.direction == DOWN){
				this.orientation = VERTICAL;
			} else if(this.direction == LEFT || this.direction == RIGHT){
				this.orientation = HORIZONTAL;
			}
			lastHit[0] = nextMove[0];
			lastHit[1] = nextMove[1];
		}
		numMoves++;
		System.out.println("Player " + myPlayerNum + " (" + nextMove[0] + "," + nextMove[1] + ") num Moves = " + numMoves);
	}
	
	
	public static int randInt(int min, int max) {

	    // Usually this can be a field rather than a method variable
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	
	private int[] getNextMove(){
		int[] moves;
		if(activeSinking){
			moves = this.getSinkShipNextShot();
		} else {
			switch(this.strategy){
			case DIAGONALFIRINGSTRATEGY:
				moves = this.getDiagonalFiringNextShot();
				break;
			case EVERYOTHERFIRINGSTRATEGY:
				moves = this.getEveryOtherFiringNextShot();
				break;
			default:
				moves = this.getEveryOtherFiringNextShot();
				break;
			}
		}
		return moves;
	}
	
	private int[] getSinkShipNextShot(){
		System.out.println("sink 'em, Danno:"+this.orientation+":"+this.direction);
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
				System.out.println("try left: " + moves[0] + "|" + moves[1]);
				return moves;
			} else {
				this.direction = RIGHT;
			}
			// try right
			if(this.direction == RIGHT && right >= 0 && right < 10 && game.getMoveBoardValue(myMoves, lastHit[0] + 1, right + 1) == BSGame.PEG_EMPTY){
				moves[0] = lastHit[0];
				moves[1] = right;
				System.out.println("try right: " + moves[0] + "|" + moves[1]);

				return moves;
			} else {
				this.direction = UP;
			}
			// try up
			if(this.direction == UP && up >= 0 && up < 10 && game.getMoveBoardValue(myMoves, up + 1, lastHit[1] + 1) == BSGame.PEG_EMPTY){
				moves[0] = up;
				moves[1] = lastHit[1];
				System.out.println("try up: " + moves[0] + "|" + moves[1]);
				return moves;
			} else {
				this.direction = DOWN;
			}
			// try down
			if(this.direction == DOWN && down >= 0 && down < 10 && game.getMoveBoardValue(myMoves, down + 1, lastHit[1] + 1) == BSGame.PEG_EMPTY){
				moves[0] = down;
				moves[1] = lastHit[1];
				System.out.println("try down: " + moves[0] + "|" + moves[1]);
				return moves;
			}
		} else if(this.orientation == HORIZONTAL){
			if(this.direction == LEFT){
				if((lastHit[1] - 1 >= 0) && this.lastMoveResult == BSGame.PEG_HIT 
						&& game.getMoveBoardValue(myMoves, lastHit[0] + 1, lastHit[1]) == BSGame.PEG_EMPTY){
					moves[0] = lastHit[0];
					moves[1] = lastHit[1] - 1;
					System.out.println("horizontal left: " + moves[0] + ":" + moves[1]);
					return moves;
				} else {
					this.direction = RIGHT;
					moves[0] = firstHit[0];
					moves[1] = firstHit[1] + 1;
					if(moves[1] > 9 || !(game.getMoveBoardValue(myMoves, moves[0] + 1, moves[1] + 1) == BSGame.PEG_EMPTY)){
						this.orientation = UNKNOWN;
						this.direction = LEFT;
						this.lastHit[0] = firstHit[0];
						this.lastHit[1] = firstHit[1];
						moves = getSinkShipNextShot();
					} else {
						System.out.println("horizontal left switch direction to right: " + moves[0] + ":" + moves[1]);					
					}					
					return moves;
				}
			} else {
				if(this.lastMoveResult == BSGame.PEG_HIT && (lastHit[1] + 1 < 10)
						&& game.getMoveBoardValue(myMoves, lastHit[0] + 1, lastHit[1] + 2) == BSGame.PEG_EMPTY){
					moves[0] = lastHit[0];
					moves[1] = lastHit[1] + 1;
					System.out.println("horizontal right: " + moves[0] + ":" + moves[1]);
					return moves;
				} else {
					this.direction = LEFT;
					moves[0] = firstHit[0];
					moves[1] = firstHit[1] - 1;
					if(moves[1] < 0 || !(game.getMoveBoardValue(myMoves, moves[0] + 1, moves[1] + 1) == BSGame.PEG_EMPTY)){
						this.orientation = UNKNOWN;
						this.direction = LEFT;
						this.lastHit[0] = firstHit[0];
						this.lastHit[1] = firstHit[1];
						moves = getSinkShipNextShot();
						System.out.println("horizontal right switch direction to left: " + moves[0] + ":" + moves[1]);
					} else {
						System.out.println("horizontal right switch direction to left: " + moves[0] + ":" + moves[1]);
					}
					return moves;
				}
			}
		} else if(this.orientation == VERTICAL){
			if(this.direction == UP){
				if(this.lastMoveResult == BSGame.PEG_HIT && (lastHit[0] - 1 >= 0)
						&& game.getMoveBoardValue(myMoves, lastHit[0], lastHit[1] + 1) == BSGame.PEG_EMPTY){
					moves[0] = lastHit[0] - 1;
					moves[1] = lastHit[1];
					System.out.println("vertical up: " + moves[0] + ":" + moves[1]);
					return moves;
				} else {
					this.direction = DOWN;
					moves[0] = firstHit[0] + 1;
					moves[1] = firstHit[1];
					if(moves[0] > 9 || !(game.getMoveBoardValue(myMoves, moves[0] + 1, moves[1] + 1) == BSGame.PEG_EMPTY)){
						this.orientation = UNKNOWN;
						this.direction = LEFT;
						this.lastHit[0] = firstHit[0];
						this.lastHit[1] = firstHit[1];
						moves = getSinkShipNextShot();
					} else {
						System.out.println("vertical up switch to down: " + moves[0] + ":" + moves[1]);
					}
					return moves;
				}
			} else {
				System.out.println("down. next one empty: "+lastHit[0]+":"+lastHit[1]);
				if(this.lastMoveResult == BSGame.PEG_HIT && (lastHit[0] + 1 < 10)
						&& game.getMoveBoardValue(myMoves, lastHit[0] + 2, lastHit[1] + 1) == BSGame.PEG_EMPTY){
					moves[0] = lastHit[0] + 1;
					moves[1] = lastHit[1];
					System.out.println("vertical down: " + moves[0] + ":" + moves[1]);
					return moves;
				} else {
					this.direction = UP;
					moves[0] = firstHit[0] - 1;
					moves[1] = firstHit[1];
					if(moves[0] < 0 || !(game.getMoveBoardValue(myMoves, moves[0] + 1, moves[1] + 1) == BSGame.PEG_EMPTY)){
						this.orientation = UNKNOWN;
						this.direction = LEFT;
						this.lastHit[0] = firstHit[0];
						this.lastHit[1] = firstHit[1];
						moves = getSinkShipNextShot();
					} else {
						System.out.println("vertical down switch to up: " + moves[0] + ":" + moves[1]);
					}
					return moves;
				}
			}
		}
		return moves;
	}
	
	public void sankCarrier() {
		currentHits[currentHitIndex] = new Hit(lastMove[0], lastMove[1]);
		lastHit[0] = lastMove[0];
		lastHit[1] = lastMove[1];
		this.resetSinkRoutine(Ships.SHIP_CARRIER);
		System.out.println("You Sank my Carrier(p" + myPlayerNum + ")");
	}

	public void sankBattleShip() {
		currentHits[currentHitIndex] = new Hit(lastMove[0], lastMove[1]);
		lastHit[0] = lastMove[0];
		lastHit[1] = lastMove[1];
		this.resetSinkRoutine(Ships.SHIP_BATTLESHIP);
		System.out.println("You Sank my Battleship(p" + myPlayerNum + ")");
	}

	public void sankCruiser() {
		currentHits[currentHitIndex] = new Hit(lastMove[0], lastMove[1]);
		lastHit[0] = lastMove[0];
		lastHit[1] = lastMove[1];
		this.resetSinkRoutine(Ships.SHIP_CRUISER);
		System.out.println("You Sank my Cruiser(p" + myPlayerNum + ")");

	}

	public void sankDestroyer() {
		if(orientation == UNKNOWN){
			if(lastMove[0] == firstHit[0]){
				orientation = HORIZONTAL;
			} else if(lastMove[1] == firstHit[1]){
				orientation = VERTICAL;
			}
		}
		currentHits[currentHitIndex] = new Hit(lastMove[0], lastMove[1]);
		lastHit[0] = lastMove[0];
		lastHit[1] = lastMove[1];
		this.resetSinkRoutine(Ships.SHIP_DESTROYER);
		System.out.println("You Sank my Destroyer(p" + myPlayerNum + ")");

	}

	public void sankSubmarine() {
		currentHits[currentHitIndex] = new Hit(lastMove[0], lastMove[1]);
		lastHit[0] = lastMove[0];
		lastHit[1] = lastMove[1];
		this.resetSinkRoutine(3);
		System.out.println("You Sank my Submarine(p" + myPlayerNum + ")");
	}
	
	private void resetSinkRoutine(int length){
		// log ship sunk so we avoid these spots when looking at other hits in this firing sequence
		if(length > 0){
			if(orientation == VERTICAL){
				// last hit is above first hit
				if(firstHit[0] > lastHit[0]){
					System.out.println("sunk last hit (" + lastHit[0] + ") above first hit");
					for(int i = lastHit[0]; i < (lastHit[0] + length); i++){
						sunk[sunkIndex] = new Hit(i, lastHit[1]);
						sunkIndex++;
						System.out.println("sunk index: " + sunkIndex);
					}
				// last hit is below first hit
				} else {
					System.out.println("sunk last hit (" + lastHit[0] + ") below first hit");
					for(int i = lastHit[0]; i > (lastHit[0] - length); i--){
						sunk[sunkIndex] = new Hit(i, lastHit[1]);
						sunkIndex++;
						System.out.println("sunk index: " + sunkIndex);
					}
				}
			} else {
				// last hit is to the left of first hit
				if(firstHit[1] > lastHit[1]){
					System.out.println("sunk last hit (" + lastHit[1] + ") to left of first hit");
					for(int i = lastHit[1]; i < (lastHit[1] + length); i++){
						sunk[sunkIndex] = new Hit(lastHit[0], i);
						sunkIndex++;
						System.out.println("sunk index: " + sunkIndex);
					}
				// last hit is to the right of first hit
				} else {
					System.out.println("sunk last hit (" + lastHit[1] + ") to right of first hit");
					for(int i = lastHit[1]; i > (lastHit[1] - length); i--){
						sunk[sunkIndex] = new Hit(lastHit[0], i);
						sunkIndex++;
						System.out.println("sunk index: " + sunkIndex);
					}
				}
			}
		}
		boolean otherHitsToPursue = false;
		System.out.println("current hit index: " + currentHitIndex + ":" + (currentHits[currentHitIndex] == null));
		for(int i = currentHitIndex; i >= 0; i--){
			//System.out.println("check current hits: " + currentHits[i].row + ":" + currentHits[i].col);
			if(currentHits[i] != null && !checkSunkShips(currentHits[i].row, currentHits[i].col)){
				otherHitsToPursue = true;
				orientation = UNKNOWN;
				direction = LEFT;
				lastHit[0] = currentHits[i].row;
				lastHit[1] = currentHits[i].col;
				firstHit[0] = currentHits[i].row;
				firstHit[1] = currentHits[i].col;
				currentHits[i] = null;
				currentHitIndex = i;
				break;
			}
		}
		if(!otherHitsToPursue){
			System.out.println("No other hits to pursue.");
			resetCurrentHits();
			this.lastHit[0] = UNKNOWN;
			this.lastHit[1] = UNKNOWN;
			this.firstHit[0] = UNKNOWN;
			this.firstHit[1] = UNKNOWN;
			this.activeSinking = false;
			this.justSank = true;
			this.direction = LEFT;
			this.orientation = UNKNOWN;
		}
	}
	
	private boolean checkSunkShips(int row, int col){
		System.out.println("check ships sunk index: " + row + "|" + col);
		for(int i = 0; i < sunkIndex; i++){
			if(sunk[i] != null && (sunk[i].row == row && sunk[i].col == col)){
				return true;
			}
		}
		return false;
	}
	
	private void resetCurrentHits(){
		for(int i = 0; i < currentHits.length; i++){
			currentHits[i] = null;
		}
		currentHitIndex = 0;
	}
	
	private int[] getDiagonalFiringNextShot(){
		System.out.println("start diag firing routine");
		currentQuadrant = currentQuadrant % 4;
		int row = 0;
		int col = 0;
		int[] moves = new int[2];
		boolean allQuadrantsFull = true;
		for(int i = 0; i < 4; i++){
			if(!quadrantFull[i]){
				allQuadrantsFull = false;
			}
		}
		if(allQuadrantsFull){
			System.out.println("all quadrants full");
			// pick up stragglers
			for(int k = 0; k < 9; k++){
				if(isOpen(4, k)){
					moves[0] = 4;
					moves[1] = k;
					return moves;
				}
			}
			for(int l = 0; l < 9; l++){
				if(isOpen(l, 4)){
					moves[0] = l;
					moves[1] = 4;
					return moves;
				}
			}
			moves = getNextOpen();
			return moves;
			
		}
		
		int currentQuadrant = this.currentQuadrant;
		while(this.quadrantFull[currentQuadrant]){
			currentQuadrant = (currentQuadrant + 1) % 4;
		}
		
		switch(this.currentQuadrant){
		case 0:
			System.out.println("quadrant 1");
			while(!this.isOpen(row, col)){
				if(row <= 4 && col <= 4){
					row++;
					col++;
				} else {
					if(col == (COLSPERQUADRANT) && row == (ROWSPERQUADRANT)){
						System.out.println("first diag");
						row = 0;
						col = 2;
					} else if(row == (ROWSPERQUADRANT - 2) && col == (COLSPERQUADRANT)){
						System.out.println("second diag");
						row = 0;
						col = 4;
					} else if(row == (ROWSPERQUADRANT - 4) && col == (COLSPERQUADRANT)){
						System.out.println("third diag");
						row = 2;
						col = 0;
					} else if(row == (ROWSPERQUADRANT) && col == (COLSPERQUADRANT - 2)){
						row = 4;
						col = 0;
						this.quadrantFull[0] = true;
						System.out.println("Quadrant 1 full");
					} else {
						this.currentQuadrant++;
						moves =  getDiagonalFiringNextShot();
						row = moves[0];
						col = moves[1];
						break;
					}
				}
			} 
			moves[0] = row;
			moves[1] = col;
			this.currentQuadrant++;
			break;
		case 1:
			System.out.println("quadrant 2");
			row = 0;
			col = 9;
			while(!this.isOpen(row, col)){
				System.out.println("quadrant 2: " +row+":"+col+" not open");
				if(col >= 5 && row <= 4){
					row++;
					col--;
				} else {
					if(row == (ROWSPERQUADRANT) && col == (COLSPERQUADRANT - 1)){
						System.out.println("first diag");
						col = 7;
						row = 0;
					} else if(row == (ROWSPERQUADRANT - 2) && col == (COLSPERQUADRANT - 1)){
						System.out.println("second diag");
						col = 5;
						row = 0;
					} else if(row == (ROWSPERQUADRANT - 4) && col == (COLSPERQUADRANT - 1)){
						System.out.println("third diag");
						col = 9;
						row = 2;
					} else if(row == (ROWSPERQUADRANT) && col == (COLSPERQUADRANT + 1)){
						col = 9;
						row = 4;
						this.quadrantFull[1] = true;
						System.out.println("quadrant 2 full");
					} else {
						this.currentQuadrant++;
						moves =  getDiagonalFiringNextShot();
						row = moves[0];
						col = moves[1];
						break;
					}
				}
			}
			moves[0] = row;
			moves[1] = col;
			this.currentQuadrant++;	
			break;
			
		case 2:
			System.out.println("quadrant 2");
			row = 9;
			col = 0;
			while(!this.isOpen(row, col)){
				if(row >= 5 && col <= 4){
					row--;
					col++;
				} else {
					if(row == (ROWSPERQUADRANT - 1) && col == (COLSPERQUADRANT)){
						System.out.println("first diag");
						row = 7;
						col = 0;
					} else if(row == (ROWSPERQUADRANT - 1) && col == (COLSPERQUADRANT - 2)){
						System.out.println("second diag");
						row = 5;
						col = 0;
					} else if(row == (ROWSPERQUADRANT - 1) && col == (COLSPERQUADRANT - 4)){
						row = 9;
						col = 2;
					} else if(row == (ROWSPERQUADRANT + 1) && col == (COLSPERQUADRANT)){
						row = 9;
						col = 4;
						this.quadrantFull[2] = true;
						System.out.println("quadrant 3 full");
					} else {
						this.currentQuadrant++;
						moves =  getDiagonalFiringNextShot();
						row = moves[0];
						col = moves[1];
						break;
					}
				}
			}
			moves[0] = row;
			moves[1] = col;
			this.currentQuadrant++;	
			break;
		case 3:
			System.out.println("quadrant 3");
			row = 9;
			col = 9;
			while(!this.isOpen(row, col)){
				if(row >= 5 && col >= 5){
					row--;
					col--;
				} else {
					if(col == (COLSPERQUADRANT - 1) && row == (ROWSPERQUADRANT - 1)){
						System.out.println("first diag");
						row = 7;
						col = 9;
					} else if(col == (COLSPERQUADRANT + 1) && row == (ROWSPERQUADRANT - 1)){
						System.out.println("second diag");
						row = 5;
						col = 9;
					} else if(col == (COLSPERQUADRANT + 3) && row == (ROWSPERQUADRANT - 1)){
						System.out.println("third diag");
						row = 9;
						col = 7;
					} else if(col == (COLSPERQUADRANT - 1) && row == (ROWSPERQUADRANT + 1)){
						System.out.println("third diag");
						row = 9;
						col = 5;
						this.quadrantFull[3] = true;
						System.out.println("Quadrant 4 full");
					} else {
						this.currentQuadrant++;
						moves =  getDiagonalFiringNextShot();
						row = moves[0];
						col = moves[1];
						break;
					}
				}
			}
			moves[0] = row;
			moves[1] = col;
			this.currentQuadrant++;	
			break;
		}
		return moves;
	}
	
	private int[] getNextOpen(){
		int[] moves = new int[2];
		for(int i = 0; i < 10; i++){
			for(int j = 0; j < 10; j++){
				if(game.getMoveBoardValue(myMoves, i+1, j+1) == BSGame.PEG_EMPTY){
					moves[0] = i;
					moves[1] = j;
					return moves;
				}
			}
		}
		return moves;
	}
	
	private int[] getEveryOtherFiringNextShot(){
		int[] moves = new int[2];
		switch(firingPlane){
		case VERTICAL:
			switch(startCorner){
			case NORTHWEST:
				for(int i = 0; i < 10; i++){
					for(int j = 0; j < 10; j++){
						if(i % 2 == 0){
							if(j % 2 == 0){
								if(game.getMoveBoardValue(myMoves, i+1, j+1) == BSGame.PEG_EMPTY){
									moves[0] = i;
									moves[1] = j;
									return moves;
								}
							}
						} else if(i % 2 == 1) {
							if(j % 2 == 1){
								if(game.getMoveBoardValue(myMoves, i+1, j+1) == BSGame.PEG_EMPTY){
									moves[0] = i;
									moves[1] = j;
									return moves;
								}
							}
						}
					}
				}
				break;
			case NORTHEAST:
				for(int i = 9; i >= 0; i--){
					for(int j = 0; j < 10; j++){
						if(i % 2 == 0){
							if(j % 2 == 0){
								if(game.getMoveBoardValue(myMoves, i+1, j+1) == BSGame.PEG_EMPTY){
									moves[0] = i;
									moves[1] = j;
									return moves;
								}
							}
						} else if(i % 2 == 1) {
							if(j % 2 == 1){
								if(game.getMoveBoardValue(myMoves, i+1, j+1) == BSGame.PEG_EMPTY){
									moves[0] = i;
									moves[1] = j;
									return moves;
								}
							}
						}
					}
				}
				break;
			case SOUTHWEST:
				for(int i = 0; i < 10; i++){
					for(int j = 9; j >= 0; j--){
						if(i % 2 == 0){
							if(j % 2 == 0){
								if(game.getMoveBoardValue(myMoves, i+1, j+1) == BSGame.PEG_EMPTY){
									moves[0] = i;
									moves[1] = j;
									return moves;
								}
							}
						} else if(i % 2 == 1) {
							if(j % 2 == 1){
								if(game.getMoveBoardValue(myMoves, i+1, j+1) == BSGame.PEG_EMPTY){
									moves[0] = i;
									moves[1] = j;
									return moves;
								}
							}
						}
					}
				}
				break;
			case SOUTHEAST:
				for(int i = 9; i >= 0; i--){
					for(int j = 9; j >= 0; j--){
						if(i % 2 == 0){
							if(j % 2 == 0){
								if(game.getMoveBoardValue(myMoves, i+1, j+1) == BSGame.PEG_EMPTY){
									moves[0] = i;
									moves[1] = j;
									return moves;
								}
							}
						} else if(i % 2 == 1) {
							if(j % 2 == 1){
								if(game.getMoveBoardValue(myMoves, i+1, j+1) == BSGame.PEG_EMPTY){
									moves[0] = i;
									moves[1] = j;
									return moves;
								}
							}
						}
					}
				}
				break;
			}
		break;
		case HORIZONTAL:
			switch(startCorner){
			case NORTHWEST:
				for(int i = 0; i < 10; i++){
					for(int j = 0; j < 10; j++){
						if(i % 2 == 0){
							if(j % 2 == 0){
								if(game.getMoveBoardValue(myMoves, j+1, i+1) == BSGame.PEG_EMPTY){
									moves[0] = j;
									moves[1] = i;
									return moves;
								}
							}
						} else if(i % 2 == 1) {
							if(j % 2 == 1){
								if(game.getMoveBoardValue(myMoves, j+1, i+1) == BSGame.PEG_EMPTY){
									moves[0] = j;
									moves[1] = i;
									return moves;
								}
							}
						}
					}
				}
				break;
			case NORTHEAST:
				for(int i = 9; i >= 0; i--){
					for(int j = 0; j < 10; j++){
						if(i % 2 == 0){
							if(j % 2 == 0){
								if(game.getMoveBoardValue(myMoves, j+1, i+1) == BSGame.PEG_EMPTY){
									moves[0] = j;
									moves[1] = i;
									return moves;
								}
							}
						} else if(i % 2 == 1) {
							if(j % 2 == 1){
								if(game.getMoveBoardValue(myMoves, j+1, i+1) == BSGame.PEG_EMPTY){
									moves[0] = j;
									moves[1] = i;
									return moves;
								}
							}
						}
					}
				}
				break;
			case SOUTHWEST:
				for(int i = 0; i < 10; i++){
					for(int j = 9; j >= 0; j--){
						if(i % 2 == 0){
							if(j % 2 == 0){
								if(game.getMoveBoardValue(myMoves, j+1, i+1) == BSGame.PEG_EMPTY){
									moves[0] = j;
									moves[1] = i;
									return moves;
								}
							}
						} else if(i % 2 == 1) {
							if(j % 2 == 1){
								if(game.getMoveBoardValue(myMoves, j+1, i+1) == BSGame.PEG_EMPTY){
									moves[0] = j;
									moves[1] = i;
									return moves;
								}
							}
						}
					}
				}
				break;
			case SOUTHEAST:
				for(int i = 9; i >= 0; i--){
					for(int j = 9; j >= 0; j--){
						if(i % 2 == 0){
							if(j % 2 == 0){
								if(game.getMoveBoardValue(myMoves, j+1, i+1) == BSGame.PEG_EMPTY){
									moves[0] = j;
									moves[1] = i;
									return moves;
								}
							}
						} else if(i % 2 == 1) {
							if(j % 2 == 1){
								if(game.getMoveBoardValue(myMoves, j+1, i+1) == BSGame.PEG_EMPTY){
									moves[0] = j;
									moves[1] = i;
									return moves;
								}
							}
						}
					}
				}
				break;
			}
		break;
		}		
		return moves;
	}
	
	private boolean isOpen(int row, int col){
		return game.getMoveBoardValue(myMoves, row + 1, col + 1) == BSGame.PEG_EMPTY;
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
	
		
		//game.putShip(myShips, Ships.SHIP_CARRIER, 2, 2, Ships.SHIP_SOUTH);
		//game.putShip(myShips, Ships.SHIP_BATTLESHIP, 5, 5, Ships.SHIP_EAST);
		//game.putShip(myShips, Ships.SHIP_CRUISER, 6, 7, Ships.SHIP_EAST);
		//game.putShip(myShips, Ships.SHIP_DESTROYER, 8, 3, Ships.SHIP_EAST);
		//game.putShip(myShips, Ships.SHIP_SUBMARINE, 9, 9, Ships.SHIP_NORTH);

		return true;
	}
	
	public void placeShip(int ship) {	
		Random generator = new Random();
		boolean successful = false;		
		shipLength = ship;
		if(ship == 1) { shipLength = 3; }

			do {
				int orientation = generator.nextInt(2);
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
