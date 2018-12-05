package agent;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import state.Board;
import state.PreProcBoard;
import state.RankedBoard;

/**
 * agent.NeuralBot with preprocessing.
 * @author Frej
 *
 */
public class PreProcBot extends NeuralBot{

	public PreProcBot(Board board, int xOrO) {
		//Look up good starting values for the numbers in the constructor
		
		super(board, xOrO, "FixedPreProc_40_000_games", false, 128);
		
	}

	@Override
	protected INDArray state(Board board, int player) {
		int side = board.getSide();
		PreProcBoard pb=new PreProcBoard(board);
		int[][] playersRanking;
		int[][] opponentsRanking;
		switch (player) {
		case Board.CROSS:
			playersRanking = pb.rbX();
			opponentsRanking = pb.rbO();
			break;
		case Board.RING:
			playersRanking = pb.rbO();
			opponentsRanking = pb.rbX();
			break;
		default: return null;
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

		input.muli(1/1000.0);
		return input;
	}

	public static void main(String[] args){
		Board b=new Board(11);
		PreProcBot bot= new PreProcBot(b,1);
		System.out.println("Training PreProcBot");
		bot.practice();
	}
}
