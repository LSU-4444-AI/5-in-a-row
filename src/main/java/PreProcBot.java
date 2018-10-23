import aima.core.util.math.Vector;

/**
 * NeuralBot with preprocessing.
 * @author Frej
 *
 */
public class PreProcBot extends NeuralBot{

	public PreProcBot(Board board, int xOrO) {
		//Look up good starting values for the numbers in the constructor
		super(board, xOrO, 2*board.getSide()*board.getSide(), 64, 1, -1, 0.3, 0.5);
	}

	@Override
	protected Vector state(Board board, int player) {
		RankedBoard rb=board.rankedBoard();
		// Turn the rankedboard in to a side*side*2 long vector
		//You will probably need to add a method for this in RankedBoard
		return null;
	}

}
