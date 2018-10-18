import java.util.ArrayList;
import java.util.Random;

import aima.core.learning.neural.BackPropLearning;
import aima.core.learning.neural.FeedForwardNeuralNetwork;
import aima.core.learning.neural.NNConfig;

public abstract class NeuralBot implements Player {
	Board board;
    int xOrO;
    Random r;
    NNConfig config;
    FeedForwardNeuralNetwork nnet;
    
    
    public NeuralBot(Board board, int xOrO, int numInputs, int numOutputs, int numHiddenNodes, double weightLimitUp, double weightLimitDown, double learningRate, double momentum){
    	this.board=board;
    	this.xOrO=xOrO;
    	r=new Random();
    	this.config.setConfig("number_of_inputs", numInputs);
    	this.config.setConfig("number_of_outputs", numOutputs);
    	this.config.setConfig("number_of_hidden_neurons", numHiddenNodes);
    	this.config.setConfig("upper_limit_weights", weightLimitUp);
    	this.config.setConfig("lower_limit_weights", weightLimitDown);
    	this.nnet = new FeedForwardNeuralNetwork(this.config);
    	this.nnet.setTrainingScheme(new BackPropLearning(learningRate, momentum));
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
	
	/**Trains the neural network based on a data set.
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
