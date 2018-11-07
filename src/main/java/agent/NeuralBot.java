package agent;

import java.util.ArrayList;
import java.util.Random;
import java.io.File;
import java.io.IOException;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import state.Board;
import state.Move;
import state.RankedBoard;

public abstract class NeuralBot implements Player {
	Board board;
    int xOrO;
    Random r;
    MultiLayerNetwork nnet;
    File saveLocation;
    final int epochs=5;
    final int nbrOfPracticeGames=5000;
    final double learningRate=0.1;
    boolean printRankings=false;
    boolean printBoard=false;
    
    
    public NeuralBot(Board board, int xOrO, String filepath, boolean twoHiddenLayers){
    	this.board=board;
    	this.xOrO=xOrO;
    	r=new Random();
    	int inputSize = 2 * board.getSide() * board.getSide();
    	final int outputSize = 3;
    	final int hiddenNodes = 128;
    	
    	this.saveLocation = new File(filepath);
    	if(saveLocation.exists()){
    		try {
				nnet = ModelSerializer.restoreMultiLayerNetwork(this.saveLocation);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		nnet.setLearningRate(learningRate);
    	}
    	else {
    		if(twoHiddenLayers){
    		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
        		.weightInit(WeightInit.XAVIER)
        		.updater(new Nesterovs(learningRate, 0.5))
        		.list()
        		.layer(0, new DenseLayer.Builder().nIn(inputSize).nOut(hiddenNodes).activation(Activation.RELU).build())
        		.layer(1, new DenseLayer.Builder().nIn(hiddenNodes).nOut(hiddenNodes/2).activation(Activation.RELU).build())
        		.layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).activation(Activation.SOFTMAX).nIn(hiddenNodes/2).nOut(outputSize).build())
        		.backprop(true).pretrain(false).build();
    	
    		nnet = new MultiLayerNetwork(conf);
    		nnet.init();
    		} else {
    			MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
    	        		.weightInit(WeightInit.XAVIER)
    	        		.updater(new Nesterovs(learningRate, 0.5))
    	        		.list()
    	        		.layer(0, new DenseLayer.Builder().nIn(inputSize).nOut(hiddenNodes).activation(Activation.RELU).build())
    	        		.layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).activation(Activation.SOFTMAX).nIn(hiddenNodes).nOut(outputSize).build())
    	        		.backprop(true).pretrain(false).build();
    	    	
    	    		nnet = new MultiLayerNetwork(conf);
    	    		nnet.init();
    		}
    	}
    	
    }
    
    public void nextMove(){
    	/*
        if(board.isEmpty()){
            board.set(new Move(board.getSide()/2,board.getSide()/2, xOrO));
        }
        else{
        */
            ArrayList<Move> bestMoves=bestMoves();
            board.set(bestMoves.get(r.nextInt(bestMoves.size())));
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
					pTieLoss *= 1-out.getDouble(0);
					outputs.add(out);
					temp.undo();
				}
			}
		}
		
		double max = -Double.MAX_VALUE;
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
	public void practice(){
		print("Training started");
		boolean starting=false;
		for( int i=0; i<nbrOfPracticeGames;i++){
			print("Game: "+i);
			DataSet newRankings=playPracticeGame(starting);
			train(newRankings);
			starting=!starting;
		}
		save();
	}
	
	private DataSet generateData(ArrayList<Choice> choices, boolean win) {
		INDArray input= Nd4j.zeros(choices.size(),2*board.getSide()*board.getSide()); //Nd4j.zeros(choices.size(),2 * board.getSide() * board.getSide());
		INDArray output=Nd4j.zeros(choices.size(),3); //Nd4j.zeros(choices.size(),3);
		double pWin;
		double pTie;
		double optimality;
		INDArray nextOut=Nd4j.zeros(3);
		if(win){
			nextOut.putScalar(0,1);
		} else {
			nextOut.putScalar(1,1);
		}
		Choice last=choices.get(choices.size()-1);
		
		pWin=nextOut.getDouble(0);
		pTie=nextOut.getDouble(1);
		optimality=pWin + (pTie*last.pTieLoss/(1-last.output.getDouble(0))) + (1-pWin-pTie)*last.pLoss/(last.output.getDouble(2));
		if(printRankings){
			print("New Outputs");
		}
		for(int i=choices.size()-1;i>=0;i--){
			Choice c=choices.get(i);
			INDArray out=c.output;
			pWin=nextOut.getDouble(0);
			pTie=nextOut.getDouble(1);
			double pWinOld=out.getDouble(0);
			double pLooseOld=out.getDouble(2);
			out.muli(1-optimality);
			nextOut.muli(optimality);
			out.addi(nextOut);
			for(INDArray in:rotations(c.input)){
				input.putRow(i,in);
				output.putRow(i,out);
			}
			if(printRankings){
				print(out);
			}
			//Reversing the probability vector for opposite player
			nextOut=Nd4j.zeros(3);
			nextOut.putScalar(0, out.getDouble(2));
			nextOut.putScalar(1, out.getDouble(1));
			nextOut.putScalar(2, out.getDouble(0));
			optimality=pWin + (pTie*c.pTieLoss/(1-pWinOld)) + (1-pWin-pTie)*c.pLoss/pLooseOld;
		}
		
		
		return new DataSet(input,output);
	}

	private ArrayList<INDArray> rotations(INDArray input) {
		ArrayList<INDArray> rotations=new ArrayList<>();
		rotations.add(input);
		//TODO
		return rotations;
	}

	private DataSet playPracticeGame(boolean playersTurn) {
		RankedBoard rb= new RankedBoard(board.getSide());
		ArrayList<Choice> choices= new ArrayList<>();
		boolean win=false;
		int count=1;
		while (true) {
			if(printBoard)
				rb.printBoard();
			if(playersTurn){
				choices.add(nextMoveForTraining(rb));
				if(printRankings){
					print(choices.get(choices.size()-1).output);
				}
				if(choices.get(choices.size()-1).win){
					win=true;
					print("Win. Length: "+count);
					break;
				} 
			} else {
				choices.add(opponentsNextMove(rb));
				if(printRankings){
					print(choices.get(choices.size()-1).output);
				}
				if(choices.get(choices.size()-1).win){
					win=true;
					print("Lose. Length: "+count);
					break;
				} 
			}
			if(rb.tie()){
				print("Tie. Length: "+count);
				break;
			}
			playersTurn=!playersTurn;
			count++;
		}
		return generateData(choices, win);
	}

	private Choice opponentsNextMove(RankedBoard rb) {
		ArrayList<Move> bestMoves = rb.bestMovesFor(-xOrO);
		Move move = bestMoves.get(r.nextInt(bestMoves.size()));
		int side = rb.getSide();
		Board copy=rb.copy();
		double pLoss = 1;
		double pTieLoss = 1;
		INDArray output=null;
		INDArray input=null;
		for (int row = 0; row < side; row++) {
			for (int col = 0; col < side; col++) {
				if (rb.get(row, col) == 0) {
					copy.set(-this.xOrO, row, col);
					INDArray in=state(copy, -xOrO);
					INDArray out = nnet.output(in);
					pLoss *= out.getDouble(2);
					pTieLoss *= 1-out.getDouble(0);
					if(row==move.getRow()&&col==move.getCol()){
						output=out;
						input=in;
					}
					copy.undo();
				}
			}
		}
		boolean win=rb.winningMove(move);
		rb.set(move);
		return new Choice(input, output, pTieLoss, pLoss,win);
	}

	private Choice nextMoveForTraining(RankedBoard rb) {
		ArrayList<Move> bestMoves = new ArrayList<>();
		int side = rb.getSide();
		Board copy=rb.copy();
		double pLoss = 1;
		double pTieLoss = 1;
		ArrayList<INDArray> outputs = new ArrayList<>();
		ArrayList<INDArray> inputs = new ArrayList<>();
		for (int row = 0; row < side; row++) {
			for (int col = 0; col < side; col++) {
				if (rb.get(row, col) == 0) {
					copy.set(this.xOrO, row, col);
					/*
					if(temp.win()==xOrO){
						ArrayList<Move> winningMove = new ArrayList<Move>();
						winningMove.add(new Move(row, col, xOrO));
						return winningMove;
					}
					*/
					INDArray in=state(copy, xOrO);
					INDArray out = nnet.output(in);
					pLoss *= out.getDouble(2);
					pTieLoss *= 1-out.getDouble(0);
					outputs.add(out);
					inputs.add(in);
					copy.undo();
				}
			}
		}
		
		double max = -1;
		int i = 0;
		for (int row = 0; row < side; row++) {
			for (int col = 0; col < side; col++) {
				if (rb.get(row, col) == 0) {
					double ranking = optimality(outputs.get(i).getDouble(0), outputs.get(i).getDouble(1), pTieLoss, pLoss);
					if (max < ranking) {
						max = ranking;
						bestMoves = new ArrayList<>();
						bestMoves.add(new Move(row, col, xOrO));
					} else if (max == ranking) {
						bestMoves.add(new Move(row, col, xOrO));
					}
					i++;
				}
			}
		}
		int n=r.nextInt(bestMoves.size());
		boolean win = rb.winningMove(bestMoves.get(n));
		rb.set(bestMoves.get(n));
		return new Choice(inputs.get(n), outputs.get(n), pTieLoss, pLoss,win);
	}

	/**Trains the neural network based on a data set.
	 * 
	 */
	private void train(DataSet newRankings){
		for(int i=0;i<epochs;i++){
			nnet.fit(newRankings);
		}
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
	
	private static class Choice{
		INDArray input;
		INDArray output;
		double pTieLoss; 
		double pLoss;
		boolean win;
		Choice(INDArray input, INDArray output, double pTieLoss, double pLoss, boolean win){
			this.input=input;
			this.output=output;
			this.pTieLoss=pTieLoss;
			this.pLoss=pLoss;
			this.win=win;
		}
	}
	
	private void print(Object o){
		System.out.println(o);
	}
	
}
