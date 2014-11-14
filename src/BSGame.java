// This is the class that defines the game.  There is nothing here to see for this
// lab, move along.  If you do want to take a look at how this works, feel free.  There
// is nothing magical in here.

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class BSGame extends JFrame implements ActionListener, MouseListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 3116829490987145587L;

	private char board[][][] = new char[4][10][10];
	private Ships ships = new Ships();

	private final Font standard = new Font("Dialog", Font.PLAIN, 12);
	private final Font stencil = new Font("stencil", Font.BOLD, 32);
	private final Font status = new Font("Dialog", Font.BOLD, 18);

	public static final int MODE_SERVER = 0;
	public static final int MODE_ONE_PLAYER = 1;
	public static final int MODE_TWO_PLAYER_LOCAL = 2;
	public static final int MODE_TWO_PLAYER_REMOTE = 3;
	public static final int MODE_TWO_COMPUTER_LOCAL = 4;

	public static final int GAME_SERVER = 0;
	public static final int GAME_PLAYER1 = 1;
	public static final int GAME_PLAYER2 = 2;
	private int GAME_LAST_PLAYER = 0;

	public static final char PEG_EMPTY = ' ';
	public static final char PEG_MISS = 'M';
	public static final char PEG_HIT = 'H';
	public static final char PEG_SHIP = 'S';

	public static final int BOARD_P1SHIPS = 0;
	public static final int BOARD_P1MOVES = 1;
	public static final int BOARD_P2SHIPS = 2;
	public static final int BOARD_P2MOVES = 3;

	private static final int APP_WIDTH = 800;
	private static final int APP_HEIGHT = 600;
	
	private static final int GAMES_TO_PLAY = 1000;

	private int gameType;
	private boolean boardsAreVisible = true;
	private Graphics g;
	private JButton b1;
	private JButton displayButton;
	private JButton b3;
	private JButton b4;
	private JButton multiButton;
	private JButton singleStep;

	private int leftBoard;
	private int rightBoard;
	boolean p1ShipsSunk[] = new boolean[5];
	boolean p2ShipsSunk[] = new boolean[5];

	PlayerMDM p1;

	Player p2;
	private boolean done = false;
	private boolean d1, d2;
	
	private int p1Wins, p2Wins;
	private int gameCount;
	private boolean multGame = false;
	private boolean ssDone = true;
	
	private int ssTurn;
	
	JPanel buttons = new JPanel();

	public BSGame(int type) {
	    boardsAreVisible = true;
	    leftBoard = BOARD_P1SHIPS;
	    rightBoard = BOARD_P1MOVES;
	    p1Wins = 0;
	    p2Wins = 0;


	    initializeGame();
		gameType = type;


		Container cp = getContentPane();
	    setSize(APP_WIDTH, APP_HEIGHT);
	    setLocation(100,100);
	    setResizable(false);

	    switch (type) {
	    	case GAME_SERVER:
	    	    setTitle("Java Battleship Server");
	    	    break;

	    	case GAME_PLAYER1:
	    	    setTitle("Java Battleship Player 1");
	    		break;

	    	case GAME_PLAYER2:
	    	    setTitle("Java Battleship Player 2");
	    		break;

	    	default:
	    		break;
	    }

	    cp.setBackground(Color.black);
	    cp.setLayout(new BorderLayout());
	    cp.addMouseListener(this);

	    buttons.setLayout(new FlowLayout());
	    buttons.setBackground(Color.gray);

	    b1 = new JButton("Single Play");
	    b1.setActionCommand("Play");
	    b1.addActionListener(this);

	    displayButton = new JButton("Hide Display");
	    displayButton.addActionListener(this);
	    displayButton.setActionCommand("Display");
	    b3 = new JButton("Show Player 1 Board");
	    b3.setActionCommand("p1");
	    b3.addActionListener(this);

	    b4 = new JButton("Show Player 2 Board");
	    b4.setActionCommand("p2");
	    b4.addActionListener(this);
	    
	    singleStep = new JButton("Single Step");
	    singleStep.setActionCommand("ss");
	    singleStep.addActionListener(this);

	    multiButton = new JButton("Play " + GAMES_TO_PLAY + " Games");
	    multiButton.setActionCommand("Multi");
	    multiButton.addActionListener(this);
	    buttons.add(b1);
	    buttons.add(multiButton);
	    buttons.add(displayButton);
	    buttons.add(b3);
	    buttons.add(b4);
	    buttons.add(singleStep);
	    cp.add(buttons, BorderLayout.AFTER_LAST_LINE);

	    addWindowListener(new WindowDestroyer());

	    repaint();
	    setVisible(true);
	    g = getGraphics();



	}

	public void initializeGame() {
		done = d1 = d2 = false;
		ssDone = true;
		
//	    p1 = new MyPlayer(BSGame.GAME_PLAYER1); // Insert your player here!
	    p1 = new PlayerMDM(BSGame.GAME_PLAYER1);


//	    p2 = new MyPlayer(BSGame.GAME_PLAYER2);  // Insert your player here
	    p2 = new Player1(BSGame.GAME_PLAYER2);



		p1.numMoves = 0;
		p2.numMoves = 0;
		ssTurn = 0;
		
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				board[BOARD_P1SHIPS][i][j] = board[BOARD_P1MOVES][i][j] =
						board[BOARD_P2SHIPS][i][j] = board[BOARD_P2MOVES][i][j] = PEG_EMPTY;

			}
		}

		// No ships sunk yet
		for (int i = 0; i < 5; i++) {
			p1ShipsSunk[i] = false;
			p2ShipsSunk[i] = false;
		}

	}

	public boolean putShip(int bd, int type, int row, int col, int dir) {
		return ships.putShip(board, bd, type, row, col, dir);
	}

	public boolean shipSunk(int shipBd, int pegBd, int type) {
		return ships.shipSunk(board, shipBd, pegBd, type);
	}

	public void playGame(int mode, PlayerMDM p1, Player p2) {
		done = false;
		this.p1 = p1;
		this.p2 = p2;
		// Register the players
		p1.registerBoards(board[BSGame.BOARD_P1MOVES],
		          board[BSGame.BOARD_P1SHIPS]);
		p1.registerGame(this);

		p2.registerBoards(board[BSGame.BOARD_P2MOVES],
		          board[BSGame.BOARD_P2SHIPS]);
		p2.registerGame(this);

		// Start the game
		p1.addShips();
		p2.addShips();


		int turn = 0;
		while(!done) {
			if (turn == 0) {
				turn = 1;
				p1.makeMove();
			} else {
				turn = 0;
				p2.makeMove();
			}

			// Did that move cause us to end?
			d1 = gameOverDude(BOARD_P1SHIPS, BOARD_P2MOVES);
			d2 = gameOverDude(BOARD_P2SHIPS, BOARD_P1MOVES);
			if (d1 || d2) {
				done = true;
				if (d1) {
					
					g.setFont(status);
					g.drawString("Player 2 wins using " + p2.numMoves + " moves", 20, 100);
					repaint();
					System.out.println("Player 2 wins using " + p2.numMoves + " moves");
				} else {
					g.setFont(status);
					g.drawString("Player 1 wins using " + p1.numMoves + " moves", 20, 100);
					repaint();
					System.out.println("Player 1 wins using " + p1.numMoves + " moves");
				}
			}

			repaint();


		}
		// Alternate turns
		// check for winner
		// Ask to replay

	}

	public void singleStep(PlayerMDM p1, Player p2) {
		this.p1 = p1;
		this.p2 = p2;
		if (ssDone) {
			ssDone = false;
			// Register the players
			p1.registerBoards(board[BSGame.BOARD_P1MOVES],
			          board[BSGame.BOARD_P1SHIPS]);
			p1.registerGame(this);

			p2.registerBoards(board[BSGame.BOARD_P2MOVES],
			          board[BSGame.BOARD_P2SHIPS]);
			p2.registerGame(this);

			initializeGame();

			// Start the game
			p1.addShips();
			p2.addShips();
			ssDone = false;
		}
		if (ssTurn == 0) {
			ssTurn = 1;
			p1.makeMove();
		} else {
			ssTurn = 0;
			p2.makeMove();
		}

		// Did that move cause us to end?
		d1 = gameOverDude(BOARD_P1SHIPS, BOARD_P2MOVES);
		d2 = gameOverDude(BOARD_P2SHIPS, BOARD_P1MOVES);
		if (d1 || d2) {
			done = true;
			ssDone = true;
			if (d1) {

				g.setFont(status);
				g.drawString("Player 2 wins using " + p2.numMoves + " moves", 20, 100);
				repaint();
				System.out.println("Player 2 wins using " + p2.numMoves + " moves");
			} else {
				g.setFont(status);
				g.drawString("Player 1 wins using " + p1.numMoves + " moves", 20, 100);
				repaint();
				System.out.println("Player 1 wins using " + p1.numMoves + " moves");
			}
		}

		repaint();

	}

	public void paint(Graphics g){
		this.paintComponents(g);
		this.g = g;
		g.setColor(Color.gray);
		g.fillRect(0, 0, APP_WIDTH, APP_HEIGHT-40);

		// If the boards are visible, draw them.
		if (boardsAreVisible) {
			drawBoard(g, leftBoard, 0, 0, 0);
			drawBoard(g, rightBoard, 1, 400, 0);
		}

		// Draw the Game Labels
		g.setFont(stencil);
		g.drawString("Battleship", APP_WIDTH/2 - 100, 100);
		switch(gameType) {
		case GAME_SERVER:
			g.drawString("Team 1", 125, 190);
			g.drawString("Team 2", 525, 190);
			break;
		case GAME_PLAYER1:
		case GAME_PLAYER2:
			g.drawString("My Ships", 125, 190);
			g.drawString("My Moves", 525, 190);
			break;
		}

		if (done) {
			if (multGame) {
				g.setFont(status);
				g.drawString("Player 1 won " + p1Wins + ", Player 2 won " + p2Wins, 20, 70);
			} else {
				if (d1) {
					g.setFont(status);
					g.drawString("Player 2 wins using " + p2.numMoves + " moves", 20, 70);
					//System.out.println("Player 2 wins using " + p2.numMoves + " moves");
				} else {
					g.setFont(status);
					g.drawString("Player 1 wins using " + p1.numMoves + " moves", 20, 70);
					//System.out.println("Player 1 wins using " + p1.numMoves + " moves");
				}
			}
		}
		//g.setFont(status);
		//g.drawString("Mode: 2 Player", 20, 100);

	  }

	private static final int SQUARE_SIZE = 30;
	private static final int X_OFFSET = 35;
	private static final int Y_OFFSET = 200;
	private static final int BOARD_SIZE = SQUARE_SIZE*11;

	// Draw a board offset from x,y
	private void drawBoard(Graphics g, int bd, int loc, int x, int y) {

		// Set up the game background
		//cp.setBackground(Color.gray);  // This reduces flicker when changing the display
		g.setFont(standard);
		g.setColor(Color.black);
		g.fillRect(x+X_OFFSET, y+Y_OFFSET, BOARD_SIZE, BOARD_SIZE);
		g.setColor(Color.green);
		// Draw the board
		for (int i = 0; i < 12; i++) {
			g.drawLine(x+X_OFFSET, y+Y_OFFSET + (SQUARE_SIZE*i), x + X_OFFSET + BOARD_SIZE,
					   y+Y_OFFSET+SQUARE_SIZE*i);
			g.drawLine(x+X_OFFSET+(SQUARE_SIZE*i), y+Y_OFFSET, x+X_OFFSET+(SQUARE_SIZE*i),
					   y+Y_OFFSET+BOARD_SIZE);
			if ((i > 0) && (i < 11)) {
				g.setColor(Color.white);
				g.drawString(String.valueOf((char)('A'+ i - 1)), x+X_OFFSET+12+(SQUARE_SIZE * i),
						     y + Y_OFFSET + 18);
				g.drawString(String.valueOf(i), x+X_OFFSET+12, y+Y_OFFSET+18+(SQUARE_SIZE * i));
			}
			g.setColor(Color.green);
		}

		g.setColor(Color.green);

		for (int j = 0; j < 10; j++) {
			for (int i = 0; i < 10; i++ ) {
				if (board[bd][j][i] == PEG_EMPTY)
					g.drawString("o", x+X_OFFSET+12+(SQUARE_SIZE*(i+1)), y + Y_OFFSET + 18+(SQUARE_SIZE * (j+1)));
				else if (board[bd][j][i] == PEG_SHIP)
					drawMove(loc, j+1, i+1, Color.gray);
				else if (board[bd][j][i] == PEG_MISS)
					drawMove(loc, j+1, i+1, Color.white);
				else if (board[bd][j][i] == PEG_HIT)
					drawMove(loc, j+1, i+1, Color.red);
			}
		}
	}

	private void drawMove(int loc, int row, int col, Color color) {
		//System.out.println("Making move");
		Color oldColor = g.getColor();
		g.setFont(standard);
		g.setColor(color);
		g.fillOval((loc*400)+X_OFFSET+10+(SQUARE_SIZE*col),
				Y_OFFSET + 8 +(SQUARE_SIZE * row), 12, 12);
		g.setColor(oldColor);
	}


	// Return a value from a move board (hit, miss, empty
	char getMoveBoardValue(int moveBd, int row, int col) {
		
		if ((moveBd != BSGame.BOARD_P1MOVES) && 
				(moveBd != BSGame.BOARD_P2MOVES)) {
			// You can't look at ships boards
			return PEG_EMPTY;
		} else {
			return board[moveBd][row-1][col-1];
		}
	}

	// Routine to make a move.  It returns true if the move was
	// successful, and false if not.  Success means you put a peg in an empty
	// hole or the opponent's ship.
	public boolean makeMove(int shipBd, int moveBd, int row, int col) {
		// Don't allow a move off the board
		if ((row < 1) || (row > 10))
			return false;

		if ((col < 1) || (col > 10))
			return false;

		// You can't put a move anywhere but an empty or ship location
		if ((board[shipBd][row-1][col-1] != PEG_EMPTY) &&
			(board[shipBd][row-1][col-1] != PEG_SHIP))
			return false;
		
		// If there is already a peg on your move board, this is an
		// invalid move
		if (board[moveBd][row-1][col-1] != PEG_EMPTY)
			return false;
			
		// don't allow the same player to play twice in a row
		//System.out.println("moveBd = " + moveBd);
		if (GAME_LAST_PLAYER == moveBd){

			if ( (p1.startingTurn == true) && (moveBd == BSGame.BOARD_P2MOVES)){
				System.out.println("Player 1 was dumbfounded! (failed to make a move during turn)");
				//throw new RuntimeException("A player was dumbfounded and could not decide where to play!");
				p1.startingTurn = false;
			} 
			if ( (p2.startingTurn == true) && (moveBd == BSGame.BOARD_P1MOVES)){
				System.out.println("Player 2 was dumbfounded! (failed to make a move during turn)");
				p2.startingTurn = false;
				//throw new RuntimeException("A player was dumbfounded and could not decide where to play!");
			} else{
				System.out.println("someone is cheating!");
				//throw new RuntimeException("You already made a move! Cheat much?");
			}
		} else {
			GAME_LAST_PLAYER = moveBd;
			// this move will be successful so set the state
			p1.startingTurn = false;
			p2.startingTurn = false;
		}

		// if there is a ship there, this is a hit
		// otherwise it is a miss
		if (board[shipBd][row-1][col-1] == PEG_SHIP) {
			board[moveBd][row-1][col-1] = PEG_HIT;
		} else {
			board[moveBd][row-1][col-1] = PEG_MISS;
		}
		repaint();

		// Did this move sink a ship?
		for (int type = 1; type <= Ships.SHIP_COUNT; type++) {
			if (ships.shipSunk(board, shipBd, moveBd, type)) {
				if (shipBd == BOARD_P1SHIPS) {
					if (p1ShipsSunk[type-1] == false) {
						p1ShipsSunk[type-1] = true;
						// Notify the person making the move if it resulted in a ship sinking
						//System.out.println("Calling player 2 for ship sank");
						switch(type) {
						case Ships.SHIP_CARRIER:
							p2.sankCarrier();
							break;
						case Ships.SHIP_BATTLESHIP:
							p2.sankBattleShip();
							break;
						case Ships.SHIP_DESTROYER:
							p2.sankDestroyer();
							break;
						case Ships.SHIP_CRUISER:
							p2.sankCruiser();
							break;
						case Ships.SHIP_SUBMARINE:
							p2.sankSubmarine();
							break;
						}

					}
				} else {
					if (p2ShipsSunk[type-1] == false) {
						p2ShipsSunk[type-1] = true;
						// Notify the person making the move if it resulted in a ship sinking
						//System.out.println("Calling player 1 for ship sank");

						switch(type) {
						case Ships.SHIP_CARRIER:
							p1.sankCarrier();
							break;
						case Ships.SHIP_BATTLESHIP:
							p1.sankBattleShip();
							break;
						case Ships.SHIP_DESTROYER:
							p1.sankDestroyer();
							break;
						case Ships.SHIP_CRUISER:
							p1.sankCruiser();
							break;
						case Ships.SHIP_SUBMARINE:
							p1.sankSubmarine();
							break;
						}

					}

				}

				//System.out.println("Move sank ship " + type);
			}
		}

		return true;
	}



	public boolean gameOverDude(int shipBd, int moveBd) {
		int count = 0;
		for (int type = 1; type <= Ships.SHIP_COUNT; type++) {
			if (ships.shipSunk(board, shipBd, moveBd, type)) {
				count++;
				//System.out.println("Move sank ship " + type);
			}
		}
		// five sunk means game over
		if (count == 5)
			return true;
		else
			return false;

	}
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		if (arg0.getActionCommand().equals("Display")) {
			if (boardsAreVisible) {
				boardsAreVisible = false;
				displayButton.setText("Show Display");
			} else {
				boardsAreVisible = true;
				displayButton.setText("Hide Display");
			}
			repaint();
		} else if (arg0.getActionCommand().equals("p1")) {
			leftBoard = BOARD_P1SHIPS;
			rightBoard = BOARD_P1MOVES;
			repaint();
		} else if (arg0.getActionCommand().equals("p2")) {
			leftBoard = BOARD_P2SHIPS;
			rightBoard = BOARD_P2MOVES;
			repaint();

		} else if (arg0.getActionCommand().equals("Play")) {
			System.out.println("Playing game");
			initializeGame();
			multGame = false;
			this.playGame(BSGame.MODE_TWO_COMPUTER_LOCAL, p1, p2);

		} else if (arg0.getActionCommand().equals("Multi")) {
			this.gameCount = GAMES_TO_PLAY;
			this.multGame = true;
			p1Wins = p2Wins = 0;
			while (gameCount > 0) {
				initializeGame();
				this.playGame(BSGame.MODE_TWO_COMPUTER_LOCAL, p1, p2);
				if (d1) p2Wins++;
				if (d2) p1Wins++;
				gameCount--;
			}
			System.out.println("Player 1 won" + p1Wins + ", Player 2 won " + p2Wins);
		} else if (arg0.getActionCommand().equals("ss")) {
			singleStep(p1, p2);
		}
	}

	private int mouseX = 0;
	private int mouseY = 0;
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

		mouseX = e.getX();
		mouseY = e.getY();

		int row;
		int col;

		if (mouseY < 200 ) {
			System.out.println("Too High");
		}

		row = (mouseY - Y_OFFSET + SQUARE_SIZE) / SQUARE_SIZE;
		col = (mouseX - X_OFFSET - (400)) / SQUARE_SIZE;
		System.out.println("In row " + row + ", and col " + col);
		this.makeMove(BSGame.BOARD_P1SHIPS, BSGame.BOARD_P1MOVES, row, col);

	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		System.out.println("x = " + e.getX() + ", y = " + e.getY());

	}

}
