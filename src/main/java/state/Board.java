package state;

import java.util.ArrayList;

public class Board {
	public static final int CROSS = -1;
	public static final int RING = 1;
	protected int[][] board;
	private ArrayList<Move> moves; // Stack of the past moves

	/** Creates a new board with side*side number of cells */
	public Board(int side) {
		board = new int[side][side];
		moves = new ArrayList<Move>();
	}

	public boolean onBoard(int row, int col) {
		if (row >= 0 && row < board.length && col >= 0 && col < board.length) {
			return true;
		} else {
			return false;
		}
	}

	/** Resets the board */
	public void clear() {
		board = new int[board.length][board.length];
	}

	public boolean validMove(Move move) {
		return onBoard(move.getRow(), move.getCol()) && (move.getPlayer() == 1 || move.getPlayer() == -1);
	}

	/** Returns a copy of the board.
	 */
	public Board copy() {
		Board copy = new Board(board.length);
		for (int row = 0; row < board.length; row++) {
			for (int col = 0; col < board.length; col++) {
				copy.set(board[row][col], row, col);
			}
		}
		return copy;
	}
	
	public void printBoard() {
		int boardSize = board[0].length * 2;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < boardSize + 2; i++) {
			sb.append("-");
		}
		System.out.print(sb.toString().concat("\n"));
		for (int[] row : board) {
		    System.out.print("|");
			for (int cell : row) {
				if(cell==CROSS){
					System.out.print("X ");
				} else if(cell==RING){
					System.out.print("O ");
				} else {
					System.out.print("  ");
				}
			}
			System.out.print("|");
			System.out.println();
		}
		for (int i = 0; i < boardSize + 2; i++) {
			System.out.print("-");
		}
		System.out.println();
	}

	/**
	 * Changes the value in cell (row,col) to xOrO if the cell is empty
	 */
	public boolean set(int xOrO, int row, int col) {
		if (!onBoard(row, col) || !(xOrO == -1 || xOrO == 1)) {
			return false;
		}
		board[row][col] = xOrO;
		moves.add(new Move(row, col , xOrO));
		return true;
	}

	public boolean set(Move move) {
		if (validMove(move)) {
			board[move.getRow()][move.getCol()] = move.getPlayer();
			moves.add(move);
			return true;
		}
		return false;
	}

	/** Undos the last move */
	public void undo() {
		if (!moves.isEmpty()) {
			board[moves.get(moves.size() - 1).getRow()][moves.get(moves.size() - 1).getCol()] = 0;
			moves.remove(moves.size() - 1);
		}
	}

	/** Returns true if there is n in row of cross or ring */
	public boolean inRow(int n, int xOrO) {
		return inRowHo(n, xOrO) || inRowVe(n, xOrO) || inRowDi1(n, xOrO)
				|| inRowDi2(n, xOrO);

	}
	
	public boolean tie(){
		for(int xOrO=-1;xOrO<2; xOrO+=2){
			for (int[] rc : new int[][] { { 0, 1 }, { 1, 0 }, { 1, 1 }, { 1, -1 } }) {
				int r = rc[0];
				int c = rc[1];
                for(int r0=0; r0<getSide();r0++){
                    loop: for(int c0=0;c0<getSide();c0++){
                        for(int d=0;d<5;d++){
                            int row=r0+r*d;
                            int col=c0+c*d;
                            if (!onBoard(row,col)||get(row, col) == xOrO) {
                                continue loop;
                            }
                        }
                        return false;
                    }
                }
            }
        }
		return true;
	}

	private boolean inRowHo(int n, int xOrO) {
		for (int col = 0; col < board.length; col++) {
			int sum = 0;
			for (int row = 0; row < board.length; row++) {
				if (board[row][col] == xOrO) {
					sum++;
				} else {
					sum = 0;
				}
				if (sum == n) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean inRowVe(int n, int xOrO) {
		for (int row = 0; row < board.length; row++) {
			int sum = 0;
			for (int col = 0; col < board.length; col++) {
				if (board[row][col] == xOrO) {
					sum++;
				} else {
					sum = 0;
				}
				if (sum == n) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean inRowDi1(int n, int xOrO) {
		int row = 0;
		int col = 0;
		for (int r0 = 0; r0 < board.length; r0++) {
			row = r0;
			col = 0;
			int sum = 0;
			while (row < board.length && col < board.length) {
				if (board[row][col] == xOrO) {
					sum++;
				} else {
					sum = 0;
				}
				if (sum == n) {
					return true;
				}
				row++;
				col++;
			}
		}
		for (int c0 = 1; c0 < board.length; c0++) {
			int sum = 0;
			row = 0;
			col = c0;
			while (row < board.length && col < board.length) {
				if (board[row][col] == xOrO) {
					sum++;
				} else {
					sum = 0;
				}
				if (sum == n) {
					return true;
				}
				row++;
				col++;
			}

		}
		return false;

	}

	private boolean inRowDi2(int n, int xOrO) {
		int row = 0;
		int col = 0;
		for (int r0 = 0; r0 < board.length; r0++) {
			int sum = 0;
			row = r0;
			col = 0;
			while (row >= 0 && col < board.length) {
				if (board[row][col] == xOrO) {
					sum++;
				} else {
					sum = 0;
				}
				if (sum == n) {
					return true;
				}
				row--;
				col++;
			}

		}
		for (int c0 = 1; c0 < board.length; c0++) {
			int sum = 0;
			row = board.length - 1;
			col = c0;
			while (row >= 0 && col < board.length) {
				if (board[row][col] == xOrO) {
					sum++;
				} else {
					sum = 0;
				}
				if (sum == n) {
					return true;
				}
				row--;
				col++;
			}

		}
		return false;
	}

	/** Returns -1 if X has won, 1 if ring has won and 0 if nobody has won */
	public int win() {
		if (inRow(5, CROSS)) {
			return CROSS;
		}
		if (inRow(5, RING)) {
			return RING;
		}
		return 0;
	}

	/** 
	 * 
	 */
	public int getSide() {
		return board.length;
	}

	/** Returns true if the board is empty*/
	public boolean isEmpty() {
		for (int row = 0; row < board.length; row++) {
			for (int col = 0; col < board.length; col++) {
				if (board[row][col] != 0) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Returns the value of cell (row,col)
	 */
	public int get(int row, int col) {
		return board[row][col];
	}
	
	public RankedBoard rankedBoard(){
        RankedBoard rb=new RankedBoard(getSide());
        for (int row = 0; row < getSide(); row++) {
			for (int col = 0; col < getSide(); col++) {
                if (get(row, col) != 0){
                    rb.set(new Move(row, col,get(row, col)));
                }
            }
        }
        return rb;
    }
}
