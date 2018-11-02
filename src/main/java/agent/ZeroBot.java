package agent;

import java.util.ArrayList;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import state.Board;
import state.Move;

/**
 * agent.NeuralBot without any preprocessing.
 * @author Frej
 *
 */
public class ZeroBot extends NeuralBot{

	public ZeroBot(Board board, int xOrO) {
		//Look up good starting values for the numbers in the constructor
		super(board, xOrO, "Zero.zip");
	}

	@Override
	protected INDArray state(Board board, int player) {
		// Turn the board in to a side*side*2 long vector with values 0 and 1
		int side = board.getSide();
		INDArray input = Nd4j.zeros(side * side * 2);
		int i = 0;
		int notPlayer = -1 * player;
		for (int row = 0; row < side; row++) {
			for (int col = 0; col < side; col++) {
				if (board.get(row, col) == player)
					input.putScalar(i, 1);
				else
					input.putScalar(i, 0);
				i++;
			}
		}
		for (int row = 0; row < side; row++) {
			for (int col = 0; col < side; col++) {
				if (board.get(row, col) == notPlayer)
					input.putScalar(i, 1);
				else
					input.putScalar(i, 0);
				i++;
			}
		}
		return input;
	}
	
	public static void main(String[] args){
		Board b=new Board(11);
		ZeroBot bot= new ZeroBot(b,1);
		bot.practice();
	}

}
