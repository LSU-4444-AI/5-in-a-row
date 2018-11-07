package agent;

import java.util.ArrayList;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.transforms.Log;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

import state.Board;
import state.RankedBoard;

/**
 * agent.NeuralBot with preprocessing.
 * @author Frej
 *
 */
public class PreProcBot extends NeuralBot{

	public PreProcBot(Board board, int xOrO) {
		//Look up good starting values for the numbers in the constructor
		
		super(board, xOrO, "PreProc.zip", false);
	}

	@Override
	protected INDArray state(Board board, int player) {
		int side = board.getSide();
		RankedBoard rb=board.rankedBoard();
		int[][] playersRanking = new int[side][side];
		int[][] opponentsRanking = new int[side][side];
		switch (player) {
		case Board.CROSS:
			playersRanking = rb.rbX();
			opponentsRanking = rb.rbO();
			break;
		case Board.RING:
			playersRanking = rb.rbO();
			opponentsRanking = rb.rbX();
			break;
		}
		// Turn the rankedboard in to a side*side*2 long vector
		INDArray input = Nd4j.zeros(side * side * 2);
		int i = 0;
		for (int row = 0; row < side; row++) {
			for (int col = 0; col < side; col++) {
				input.putScalar(i, playersRanking[row][col]);
				i++;
			}
		}
		for (int row = 0; row < side; row++) {
			for (int col = 0; col < side; col++) {
				input.putScalar(i, opponentsRanking[row][col]);
				i++;
			}
		}

//		input.addi(0.2);
//		input=Transforms.log(input,2);	
//		input.muli(0.1);
		input.muli(1/100000.0);
		return input;
	}

	public static void main(String[] args){
		Board b=new Board(11);
		PreProcBot bot= new PreProcBot(b,1);
		System.out.println("Training PreProcBot");
		bot.practice();
	}
}
