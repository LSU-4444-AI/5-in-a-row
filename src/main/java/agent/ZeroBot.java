package agent;

import aima.core.util.math.Vector;
import state.Board;

/**
 * agent.NeuralBot without any preprocessing.
 * @author Frej
 *
 */
public class ZeroBot extends NeuralBot{

	public ZeroBot(Board board, int xOrO) {
		//Look up good starting values for the numbers in the constructor
		super(board, xOrO, 2*board.getSide()*board.getSide(), 64, 1, -1, 0.3, 0.5);
	}

	@Override
	protected Vector state(Board board, int player) {
		// Turn the board in to a side*side*2 long vector with values 0 and 1
		return null;
	}

}
