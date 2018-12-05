package state;

import java.util.ArrayList;

/** A board where each cell is ranked. Ranking does not support undo. */
public class PreProcBoard extends Board {
	private RB rbX;
	private RB rbO;

	public PreProcBoard(int side) {
		super(side);
		rbX = new RB(CROSS, side);
		rbO = new RB(RING, side);
	}
	
	public PreProcBoard(Board b){
		super(b.getSide());
		rbX = new RB(CROSS, getSide());
		rbO = new RB(RING, getSide());
        for (int row = 0; row < getSide(); row++) {
			for (int col = 0; col < getSide(); col++) {
                if (b.get(row, col) != 0){
                    set(new Move(row, col,b.get(row, col)));
                }
            }
        }
	}

	public boolean set(Move move) {
		if(super.set(move)){
			rbX.updateRanking(move);
			rbO.updateRanking(move);
			return true;
		}
		return false;
	}
	
	public boolean set(int xOrO, int row, int col) {
		Move move = new Move(row, col,xOrO);
		if(super.set(move)){
			rbX.updateRanking(move);
			rbO.updateRanking(move);
			return true;
		}
		return false;
	}

	public int[][] rbX(){
		return this.rbX.rb();
	}
	
	public int[][] rbO(){
		return this.rbO.rb();
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
							
							if (value == xOrO)
								sum++;
							else if (value==-xOrO)
								continue loop;
							
							list.add(new int[] { row, col });
						}
						int ranking = exp(sum) - exp(sum - 1);
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
							
							if (value == xOrO)
								sum++;
							else if (value==-xOrO)
								continue loop;

							list.add(new int[] { row, col });
						}
						int ranking = exp(sum);
						for (int[] cell : list) {
							rb[cell[0]][cell[1]] -= ranking + 1;
						}
					}
				}
			}
		}

		/** 5^n, 0 for n<=0 */
		private int exp(int n) {
			if (n <= 0)
				return 0;
			int exp = 1;
			for (int i = 0; i < n; i++) {
				exp = exp * 5;
			}
			return exp;
		}
		
		public int[][] rb(){
			return this.rb;
		}
	}

	private static void print(String s) {
		System.out.print(s);
	}
	
	public static void main(String[] args){
		PreProcBoard pb=new PreProcBoard(11);
		pb.printRankings();
		pb.set(new Move(5,5,CROSS));
		pb.printRankings();
		pb.set(new Move(5,4,RING));
		pb.printRankings();
	}
}
