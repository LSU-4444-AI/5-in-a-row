import java.util.ArrayList;
import java.util.Random;

public abstract class NeuralBot implements Player {
	Board board;
    int xOrO;
    Random r;
    final int nodes;
    final int layers;
    
    public NeuralBot(Board board, int xOrO, int nodes, int layers){
    	this.board=board;
    	this.xOrO=xOrO;
    	r=new Random();
    	this.nodes=nodes;
    	this.layers=layers;
    }
    
    public void nextMove(){
        if(board.isEmpty()){
            board.set(new Move(board.getSide()/2,board.getSide()/2, xOrO));
        }
        else{
            ArrayList<Move> bestMoves=bestMoves();
            board.set(bestMoves.get(r.nextInt(bestMoves.size())));
        }
    }
    
    /**Creates input vector for the neural network
     * 
     * @param board
     * @param player
     * @return
     */
    protected abstract Object state(Board board, int player);

    /**List of the best moves
     * 
     * @return
     */
	private ArrayList<Move> bestMoves() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**The bot practicing against a player using the ranked board to find best moves.
	 * The practicing continues until it has beaten the opponent 10 times in a row.
	 * 
	 */
	private void practice(){
		// TODO Auto-generated method stub
	}
	
	/**Trains the neural network based on data from one practice game.
	 * 
	 */
	private void train(){
		// TODO Auto-generated method stub
	}
	
	/** New values for the ranking of a state based on probability theory and stuff.
	 * 
	 * @param oldRanking, the ranking for the state used in the game
	 * @param nextRanking, the new ranking of the following state
	 * @param optimality, the probability that the move is optimal
	 * @return
	 */
	private double newRanking(double oldRanking, double nextRanking, double optimality){
		return optimality*nextRanking+(1-optimality)*oldRanking;
	}
}
