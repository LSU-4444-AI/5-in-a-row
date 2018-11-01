package agent;

import java.util.ArrayList;
import java.util.Random;
import java.io.File;
import java.io.IOException;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import state.Board;
import state.Move;

public abstract class NeuralBot implements Player {
	Board board;
    int xOrO;
    Random r;
    MultiLayerNetwork nnet;
    File saveLocation;
    
    
    public NeuralBot(Board board, int xOrO, String filepath){
    	this.board=board;
    	this.xOrO=xOrO;
    	r=new Random();
    	int inputSize = 2 * board.getSide() * board.getSide();
    	final int outputSize = 3;
    	final int hiddenNodes = 1024;
    	
    	this.saveLocation = new File(filepath);
    	if(saveLocation.exists()){
    		try {
				nnet = ModelSerializer.restoreMultiLayerNetwork(this.saveLocation);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	else {
    		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
        		.weightInit(WeightInit.XAVIER)
        		.updater(new Nesterovs(0.1, 0.9))
        		.list()
        		.layer(0, new DenseLayer.Builder().nIn(inputSize).nOut(hiddenNodes).activation(Activation.RELU).build())
        		.layer(1, new DenseLayer.Builder().nIn(hiddenNodes).nOut(hiddenNodes).activation(Activation.RELU).build())
        		.layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).activation(Activation.SOFTMAX).nIn(hiddenNodes).nOut(outputSize).build())
        		.backprop(true).pretrain(false).build();
    	
    		nnet = new MultiLayerNetwork(conf);
    		nnet.init();
    	}
    	
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
    
    /**Saves network to file
     * 
     * 
     */
    private void save(){
    	try {
			ModelSerializer.writeModel(nnet, saveLocation, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**Creates input vector for the neural network 
     * 
     * @param board
     * @param player
     * @return
     */
    protected abstract INDArray state(Board board, int player);
    
    
    /**Evaluates output vector [P(W), P(T), P(L)] for optimality
     * 
     * @param output
     * @return
     */
    private double optimality(double pW, double pT, double pTieLoss, double pLoss) {
    	//TODO Create actual evaluation metric
    	return pW + (pT*pTieLoss/(1-pW)) + pLoss;
    }

    /**List of the best moves
     * 
     * @return
     */
	private ArrayList<Move> bestMoves() {
		ArrayList<Move> bestMoves = new ArrayList<>();
		int side = board.getSide();
		Board temp = board.copy();
		double pLoss = 1;
		double pTieLoss = 1;
		ArrayList<INDArray> outputs = new ArrayList<>();
		for (int row = 0; row < side; row++) {
			for (int col = 0; col < side; col++) {
				if (board.get(row, col) == 0) {
					temp.set(this.xOrO, row, col);
					/*
					if(temp.win()==xOrO){
						ArrayList<Move> winningMove = new ArrayList<Move>();
						winningMove.add(new Move(row, col, xOrO));
						return winningMove;
					}
					*/
					INDArray out = nnet.output(state(temp, xOrO));
					pLoss *= out.getDouble(2);
					pTieLoss *= out.getDouble(0);
					outputs.add(out);
					temp.undo();
				}
			}
		}
		pTieLoss = 1 - pTieLoss;
		double max = -1;
		INDArray chosenOutput=null;
		int i = 0;
		for (int row = 0; row < side; row++) {
			for (int col = 0; col < side; col++) {
				if (board.get(row, col) == 0) {
					double ranking = optimality(outputs.get(i).getDouble(0), outputs.get(i).getDouble(1), pTieLoss, pLoss);
					print(ranking);
					if (max < ranking) {
						max = ranking;
						bestMoves = new ArrayList<>();
						bestMoves.add(new Move(row, col, xOrO));
						chosenOutput=outputs.get(i);
					} else if (max == ranking) {
						bestMoves.add(new Move(row, col, xOrO));
					}
					i++;
				}
			}
		}
		print("Probabilities: "+chosenOutput);
		return bestMoves;
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
	
	private void print(Object o){
		System.out.println(o);
	}
}
