package agent;

import java.util.ArrayList;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

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
		
		super(board, xOrO, "PreProc.zip");
	}

	@Override
	protected INDArray state(Board board, int player, int r, int c) {
		int side = board.getSide();
		RankedBoard rb=board.rankedBoard();
		rb.set(player, r, c);
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
		return input;
	}

}