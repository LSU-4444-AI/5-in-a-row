package agent;

import java.util.ArrayList;
import java.util.Random;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

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
    String saveLocation;
    String dataLocation;
    String netType;
    final int epochs=10;
    final int nbrOfPracticeGames=5000;
    final int nbrOfRegimentGames=5000; 	//Total number of games to be played
    final int naiveGameCount=1000;			//Number of games trained for the naive trained model
    final int medGameCount=10000;			//Number of games trained for the medium trained model
    final int autosaveSpacing=100;			//Number of games between each autosave
    final double learningRate=0.1;
    boolean printRankings=false;
    boolean printBoard=false;
    TrainingData data;
    
    
    public NeuralBot(Board board, int xOrO, String netType, boolean twoHiddenLayers, int hiddenNodes){
    	this.board=board;
    	this.xOrO=xOrO;
    	r=new Random();
    	int inputSize = 2 * board.getSide() * board.getSide();
    	final int outputSize = 3;
    	
		PrintStream outFile=null;
		try {
			outFile = new PrintStream("./output.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.setOut(outFile);


		this.dataLocation = netType + ".data";
    	this.saveLocation = netType + ".zip";
    	this.netType = netType;
    	
    	this.data = new TrainingData();
    	File dataFile = new File(dataLocation);
    	if(dataFile.exists()){
    		try {
				data.load(dataFile);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	File saveFile = new File(saveLocation);
    	if(saveFile.exists()){
    		try {
				nnet = ModelSerializer.restoreMultiLayerNetwork(saveFile);
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
    	File saveFile = new File(this.saveLocation);
    	save(saveFile);
    }
    
    /**Saves data to file
     * @throws FileNotFoundException 
     * 
     * 
     */
    private void saveData() throws FileNotFoundException{
    	this.data.save(this.dataLocation);
    }
    
    /**Saves network to file
     * 
     * 
     */
    private void save(File location){
    	try {
    		ModelSerializer.writeModel(nnet, location, true);
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
					//print(ranking);
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
		pWin=nextOut.getDouble(0);
		pTie=nextOut.getDouble(1);
		optimality=1;
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

	protected ArrayList<INDArray> rotations(INDArray input) {
		ArrayList<INDArray> rotations=new ArrayList<>();
		for(int i=0; i<4; i++){
			rotations.add(input);
			rotations.add(ref(input));
			input=rot(input);
		}
		return rotations;
	}
	
	

	private INDArray rot(INDArray input) {
		int side=board.getSide();
		INDArray rotated= input.dup();
		for(int row=0;row<side;row++){
			for(int col=0;col<side;col++){
				rotated.putScalar(row*side+side-1-col,input.getDouble(col*side+row));
				rotated.putScalar(row*side+side-1-col+side*side,input.getDouble(col*side+row+side*side));
			}
		}
		return rotated;
	}

	private INDArray ref(INDArray input) {
		int side=board.getSide();
		INDArray ref= input.dup();
		for(int row=0;row<side;row++){
			for(int col=0;col<side;col++){
				ref.putScalar(row*side+col,input.getDouble(col*side+row));
				ref.putScalar(row*side+col+side*side,input.getDouble(col*side+row+side*side));
			}
		}
		return ref;
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
				choices.add(nextMoveForTraining(rb, xOrO));
				if(printRankings){
					print(choices.get(choices.size()-1).output);
				}
				if(choices.get(choices.size()-1).win){
					win=true;
					print("Win. Length: "+count);
					break;
				} 
			} else {
				choices.add(heuristicNextMove(rb, -xOrO));
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

	private Choice heuristicNextMove(RankedBoard rb, int xOrO) {
		ArrayList<Move> bestMoves = rb.bestMovesFor(xOrO);
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
					INDArray in=state(copy, xOrO);
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
		//print(input.reshape(2*11,11));
		return new Choice(input, output, pTieLoss, pLoss,win);
	}

	private Choice nextMoveForTraining(RankedBoard rb, int xOrO) {
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
		ArrayList<INDArray> bestOutputs = new ArrayList<>();
		ArrayList<INDArray> bestInputs = new ArrayList<>();
		
		for (int row = 0; row < side; row++) {
			for (int col = 0; col < side; col++) {
				if (rb.get(row, col) == 0) {
					double ranking = optimality(outputs.get(i).getDouble(0), outputs.get(i).getDouble(1), pTieLoss, pLoss);
					if (max < ranking) {
						max = ranking;
						bestMoves = new ArrayList<>();
						bestInputs=new ArrayList<>();
						bestOutputs=new ArrayList<>();
						bestMoves.add(new Move(row, col, xOrO));
						bestInputs.add(inputs.get(i));
						bestOutputs.add(outputs.get(i));
					} else if (max == ranking) {
						bestMoves.add(new Move(row, col, xOrO));
						bestInputs.add(inputs.get(i));
						bestOutputs.add(outputs.get(i));
					}
					i++;
				}
			}
		}
		int n=r.nextInt(bestMoves.size());
		boolean win = rb.winningMove(bestMoves.get(n));
		rb.set(bestMoves.get(n));
		return new Choice(bestInputs.get(n), bestOutputs.get(n), pTieLoss, pLoss,win);
	}

	/**Trains the neural network based on a data set.
	 * 
	 */
	private void train(DataSet newRankings){
		for(int i=0;i<epochs;i++){
			nnet.fit(newRankings);
		}
	}
	
	
	/**	Trains the neural network and saves special models for the 1000, 10000, and 100000th iteration.
	 *  Variation of the practice() routine.
	 */
	public void practiceRegiment(){
		boolean starting=false;
		for( int i=data.gameNum + 1; i<=nbrOfRegimentGames;i++){
			DataSet out=playRegimentGame(starting);
			train(out);
			starting=!starting;
			if(i % autosaveSpacing == 0){
				save();
				//saveData();
			}
			switch(i){
			case naiveGameCount:
				save(new File(this.netType + "Naive.zip"));
				break;
			case medGameCount:
				save(new File(this.netType + "Medium.zip"));
				break;
			}
		}
		String filepath = this.netType + "Fully.zip";
		save(new File(filepath));
	}
	
	/** Variation of playPracticeGame() which is used in the practiceRegiment() routine. 
	 * 
	 * @param playersTurn
	 * @return
	 */
	private DataSet playRegimentGame(boolean playersTurn) {
		int side = 0;
		int length = 0;
		int wini = 0;
		if(playersTurn)
			side = 1;
		else
			side = -1;
		
		RankedBoard rb= new RankedBoard(board.getSide());
		ArrayList<Choice> choices= new ArrayList<>();
		boolean win=false;
		int count=1;
		
		while (true) {
			if(printBoard)
				rb.printBoard();
			if(playersTurn){
				choices.add(nextMoveForTraining(rb, xOrO));
				if(printRankings){
					print(choices.get(choices.size()-1).output);
				}
				if(choices.get(choices.size()-1).win){
					win=true;
					length = count;
					wini = 1;
					break;
				} 
			} else {
				choices.add(heuristicNextMove(rb, -xOrO));
				if(printRankings){
					print(choices.get(choices.size()-1).output);
				}
				if(choices.get(choices.size()-1).win){
					win=true;
					length = count;
					wini = -1;
					break;
				} 
			}
			if(rb.tie()){
				length = count;
				wini = 0;
				break;
			}
			playersTurn=!playersTurn;
			count++;
		}
		this.data.add(new TrainingDataPoint(wini, side, length));
		return generateData(choices, win);
	}
	
	/** Structure to return values from playRegimentGame() without restructuring
	 * 
	 */
	
	
	
	/** Structure to contain a single game's data
	 * 
	 */
	private class TrainingDataPoint{
		int win;
		int side;
		int length;
		
		TrainingDataPoint(){
			this.win = 0;
			this.side = 0;
			this.length = 0;
		}
		
		TrainingDataPoint(int win, int side, int length){
			this.win = win;
			this.side = side;
			this.length = length;
		}
		
		public void save(int gameNum, PrintStream outFile){
			outFile.printf("%i %i %i %i\n", gameNum, win, side, length);
		}
	}
	
	
	/** Structure to contain training data
	 * 
	 */
	private class TrainingData{
		ArrayList<TrainingDataPoint> dat;
		int gameNum;
		
		TrainingData(){
			this.dat = new ArrayList<TrainingDataPoint>();
			gameNum = 0;
		}
		
		public int getNum(){
			return gameNum;
		}
		
		public void add(TrainingDataPoint d){
			this.dat.add(d);
		}
		
		//Loads data
		public void load(File savedData) throws Exception{
			if(savedData.exists()){
				BufferedReader br = new BufferedReader(new FileReader(savedData)); 
				while(br.readLine() != null)
					gameNum++;
				br.close();
			}
		}
		
		//Saves(appends) the training data into a file
		public void save(String dataFilepath) throws FileNotFoundException{
	    	PrintStream outFile = new PrintStream(dataLocation);
	    	for(TrainingDataPoint d : dat){
	    		gameNum++;
	    		d.save(gameNum, outFile);
	    	}
	    	dat.clear();
		}
		
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
	
	private static void print(Object o){
		System.out.println(o);
	}
	
	public static void main(String[] args){
		INDArray s=Nd4j.zeros(8);
		s.putScalar(0,1);
		s.putScalar(1,2);
		s.putScalar(2,3);
		s.putScalar(3,4);
		s.putScalar(4,1);
		s.putScalar(5,2);
		s.putScalar(6,3);
		s.putScalar(7,4);
		ZeroBot bot=new ZeroBot(new Board(2),1);
		for(INDArray state: bot.rotations(s)){
			print(state.reshape(4,2));
			print("");
		}
	}
	
}
