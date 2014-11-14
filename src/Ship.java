// A simple class that describes a ship on the board.  You should not need
// to be concerned with what goes on inside this class.

public class Ship {
	private int row;
	private int col;
	private int direction;
	private int type;
	
	public Ship() {
	}
	
	public int getRow() {
		return row;
	}
	public void setRow(int row) {
		this.row = row;
	}
	public int getCol() {
		return col;
	}
	public void setCol(int col) {
		this.col = col;
	}
	public int getDirection() {
		return direction;
	}
	public void setDirection(int direction) {
		this.direction = direction;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
}
