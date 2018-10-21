import aima.core.util.math.Vector;

/**
 * NeuralBot with any preprocessing.
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
		//Concatinate the ranked boards for the players 
		return null;
	}

}
