package state;

import java.util.ArrayList;

/** A board where each cell is ranked. Ranking does not support undo. */
public class RankedBoard extends Board {
	private RB rbX;
	private RB rbO;

	RankedBoard(int side) {
		super(side);
		rbX = new RB(CROSS, side);
		rbO = new RB(RING, side);
	}

	public boolean set(Move move) {
		if(super.set(move)){
			rbX.updateRanking(move);
			rbO.updateRanking(move);
			return true;
		}
		return false;
	}

	/**
	 * Useful print for testing
	 */
	public void printRankings() {
		printBoard();
		print("X:\n");
		for (int[] row : rbX.rb) {
			for (int cell : row) {
				print(cell + "\t");
			}
			print("\n");
		}
		print("\n");
		print("O:\n");
		for (int[] row : rbO.rb) {
			for (int cell : row) {
				print(cell + "\t");
			}
			print("\n");
		}
		print("\n");
	}

	/**
	 * Returns the ranking of move
	 */
	public int ranking(Move move) {
		RB playersRanking;
		RB opponentsRanking;
		switch (move.getPlayer()) {
		case Board.CROSS:
			playersRanking = rbX;
			opponentsRanking = rbO;
			break;
		case Board.RING:
			playersRanking = rbO;
			opponentsRanking = rbX;
			break;
		default:
			return 0;
		}
		return (3 * playersRanking.rb[move.getRow()][move.getCol()]) / 2
				+ opponentsRanking.rb[move.getRow()][move.getCol()];
	}

	/**
	 * True if move is a winning move
	 */
	public boolean winningMove(Move move) {
		return offensiveRanking(move) > 90000;
	}

	/** Ranks the move by how offensive it is. */
	public int offensiveRanking(Move move) {
		RB playersRanking;
		switch (move.getPlayer()) {
		case Board.CROSS:
			playersRanking = rbX;
			break;
		case Board.RING:
			playersRanking = rbO;
			break;
		default:
			return 0;
		}
		return playersRanking.rb[move.getRow()][move.getCol()];
	}
	
	public RankedBoard copy(){
        RankedBoard rb=new RankedBoard(getSide());
        rb.board=board.clone();
        rb.rbO=rbO; //Wrong, fix later
        rb.rbX=rbX; //Wrong, fix later
        return rb;
    }

	/**
	 * Selects good moves for player XorO
	 */
	public ArrayList<Move> goodMoves(int player, int tol, int limit) {
		RB playersRanking;
		RB opponentsRanking;
		switch (player) {
		case Board.CROSS:
			playersRanking = rbX;
			opponentsRanking = rbO;
			break;
		case Board.RING:
			playersRanking = rbO;
			opponentsRanking = rbX;
			break;
		default:
			return new ArrayList<>();
		}
		ArrayList<Move> bestMoves = bestMovesFor(player);
		Move best = bestMoves.get(0);
		int r = best.getRow();
		int c = best.getCol();
		int max = (3 * playersRanking.rb[r][c]) / 2 + opponentsRanking.rb[r][c];
		ArrayList<Move> goodMoves = new ArrayList<>();
		for (int row = 0; row < getSide(); row++) {
			for (int col = 0; col < getSide(); col++) {
				if (row == r && col == c) {
					continue;
				}
				int ranking = (3 * playersRanking.rb[row][col]) / 2 + opponentsRanking.rb[row][col];
				if (max < ranking * tol - limit || ranking == max) {
					goodMoves.add(new Move(row, col, player));
				}
			}
		}
		return goodMoves;
	}

	/** List of the best moves for player */
	public ArrayList<Move> bestMovesFor(int player) {
		RB playersRanking;
		RB opponentsRanking;
		switch (player) {
		case Board.CROSS:
			playersRanking = rbX;
			opponentsRanking = rbO;
			break;
		case Board.RING:
			playersRanking = rbO;
			opponentsRanking = rbX;
			break;
		default:
			return new ArrayList<>();
		}
		ArrayList<Move> bestMoves = new ArrayList<>();
		int max = 1;
		int side = board.length;
		for (int row = 0; row < side; row++) {
			for (int col = 0; col < side; col++) {
				if (playersRanking.rb[row][col] > 90000) {
					ArrayList<Move> winningMove = new ArrayList<Move>();
					winningMove.add(new Move(row, col, player));
					return winningMove;
				}
				int ranking = (3 * playersRanking.rb[row][col]) / 2 + opponentsRanking.rb[row][col];
				if (max < ranking) {
					max = ranking;
					bestMoves = new ArrayList<>();
					bestMoves.add(new Move(row, col, player));
				} else if (max == ranking) {
					bestMoves.add(new Move(row, col, player));
				}
			}
		}
		if (bestMoves.isEmpty()) {
			for (int row = 0; row < side; row++) {
				for (int col = 0; col < side; col++) {
					if (board[row][col] == 0) {
						bestMoves.add(new Move(row, col, player));
					}
				}
			}
		}
		return bestMoves;
	}
	
	@Override
	public void clear() {
		super.clear();
		rbX = new RB(CROSS, getSide());
		rbO = new RB(RING, getSide());
	}

	private class RB {
		int[][] rb;
		int xOrO;

		/**
		 * Creates RB for empty ranked board.
		 */
		public RB(int crossOrRing, int side) {
			xOrO = crossOrRing;
			rb = new int[side][side];
		}

		/**
		 * Updates the rankings of the cells nearby move.
		 */
		private void updateRanking(Move move) {
			rb[move.getRow()][move.getCol()] = 0;
			int[][] directions = new int[][] { { 0, 1 }, { 1, 0 }, { 1, 1 }, { 1, -1 } };
			if (move.getPlayer() == xOrO) {
				for (int[] rc : directions) {
					int r = rc[0];
					int c = rc[1];
					loop: for (int d0 = -4; d0 < 1; d0++) {
						int sum = 0;
						ArrayList<int[]> list = new ArrayList<>();
						for (int d = 0; d < 5; d++) {
							int row = move.getRow() + r * (d0 + d);
							int col = move.getCol() + c * (d0 + d);
							if (!onBoard(row, col)) {
								continue loop;
							}
							int value = get(row, col);
							if (value == 0)
								list.add(new int[] { row, col });
							else if (value == xOrO)
								sum++;
							else
								continue loop;
						}
						int ranking;

						if (sum > 3) {
							ranking = 100001 - exp(sum - 1);
						} else {
							ranking = exp(sum) - exp(sum - 1);
						}
						for (int[] cell : list) {
							rb[cell[0]][cell[1]] += ranking;
						}
					}
				}
			} else {
				for (int[] rc : directions) {
					int r = rc[0];
					int c = rc[1];
					loop: for (int d0 = -4; d0 < 1; d0++) {
						int sum = 0;
						ArrayList<int[]> list = new ArrayList<>();
						for (int d = 0; d < 5; d++) {
							int row = move.getRow() + r * (d0 + d);
							int col = move.getCol() + c * (d0 + d);
							if (!onBoard(row, col)) {
								continue loop;
							}

							if (d0 + d == 0) {
								continue; // Will not count the new move.
							}
							int value = get(row, col);
							if (value == 0)
								list.add(new int[] { row, col });
							else if (value == xOrO)
								sum++;
							else
								continue loop;
						}
						if (sum > 0) {
							int ranking;
							if (sum > 3) {
								ranking = 100001 + exp(sum);
							} else {
								ranking = exp(sum);
							}
							for (int[] cell : list) {
								rb[cell[0]][cell[1]] -= ranking;
							}
						}
					}
				}
			}
		}

		/** 5^n, 0 for n<= */
		private int exp(int n) {
			if (n <= 0)
				return 0;
			int exp = 1;
			for (int i = 0; i < n; i++) {
				exp = exp * 5;
			}
			return exp;
		}
	}

	private static void print(String s) {
		System.out.print(s);
	}
	
	public static void main(String[] args){
		RankedBoard rb=new RankedBoard(11);
		rb.printRankings();
		rb.set(new Move(5,5,CROSS));
		rb.printRankings();
		rb.set(new Move(5,4,RING));
		rb.printRankings();
	}
}
