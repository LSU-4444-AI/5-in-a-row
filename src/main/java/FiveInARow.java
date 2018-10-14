import java.util.ArrayList;

public class FiveInARow {
	public static void main(String[] args) {
		Board board = new Board(11);
		ArrayList<Player> players = new ArrayList<>();
		players.add(new QuickBot(board, -1));
		players.add(new QuickBot(board, 1));
		while (true) {
			for (Player p : players) {
				board.printBoard();
				p.nextMove();
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				int win=board.win();
				if(win!=0){
					if(win==Board.CROSS)
						System.out.println("X won!");
					else
						System.out.println("O won!");
					board.printBoard();
					return;
				}
				if(board.tie()){
					System.out.println("It's a tie!");
					board.printBoard();
					return;
				}
			}
		}
	}
}
