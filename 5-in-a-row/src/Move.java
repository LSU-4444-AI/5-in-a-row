
public class Move {
	private int row;
	private int col;
	private int player;
	
	public Move(int row, int col, int xOrO){
		this.row=row;
		this.col=col;
		this.player=xOrO;
	}
	
	public int getRow(){
		return row;
	}
	
	public int getCol(){
		return col;
	}
	
	public int getPlayer(){
		return player;
	}
}
