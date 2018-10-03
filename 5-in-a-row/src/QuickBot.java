import java.util.ArrayList;
import java.util.Random;

public class QuickBot implements Player {
	Board board;
    int xOrO;
    Random r;
    
    public QuickBot(Board board, int xOrO){
    	this.board=board;
    	this.xOrO=xOrO;
    	r=new Random();
    }
    
    public void nextMove(){
        if(board.isEmpty()){
            board.set(new Move(board.getSide()/3,board.getSide()/2, xOrO));
        }
        else{
            ArrayList<Move> bestMoves=board.rankedBoard().bestMovesFor(xOrO);
            board.set(bestMoves.get(r.nextInt(bestMoves.size())));
        }
    }
}
