
public class Move {
	private int row;
	private int col;
	private int crossOrRing;
	
	public Move(int row, int col, int crossOrRing){
		this.row=row;
		this.col=col;
		this.crossOrRing=crossOrRing;
	}
	
	public int getRow(){
		return row;
	}
	
	public int getCol(){
		return col;
	}
	
	public int getCrossOrRing(){
		return crossOrRing;
	}
}
