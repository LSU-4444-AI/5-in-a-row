
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import agent.Player;
import state.Board;
import state.Move;

public class AiTest extends JPanel implements Player
{
	int xOrO;
    JButton buttons[] = new JButton[121];
    Board board;
    Move move;
    
    public AiTest(Board board, int xOrO)
    {
      this.board = board;
      this.xOrO = xOrO;
      this.move = null;
      setLayout(new GridLayout(11,11));
      initializebuttons(board.getSide());
      update(board);
    }
    
    //Function for Player interface
	public void nextMove(){
		this.move = null;
		update();
		while(this.move == null)
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		update(this.move);
		board.set(move);
	}
    
    //Receives the Board and updates buttons
    public void update(Board board){
    	for(int i = 0; i < board.getSide(); i++)
    		for(int j = 0; j < board.getSide(); j++){
    			switch(board.get(i, j)){
    			case Board.CROSS:
    				buttons[board.getSide() * i + j].setText("X");
                    buttonClicked.setForeground(Color.RED);
    				break;
    			case Board.RING:
    				buttons[board.getSide() * i + j].setText("O");
                    buttonClicked.setForeground(Color.BLUE);
    				break;
    			default:
    				buttons[board.getSide() * i + j].setText("");
    				break;
    			}
    		}
    }
    
    //Receives single move and updates buttons
    public void update(Move move){
    	int i = move.getRow();
    	int j = move.getCol();
    	switch(board.get(i, j)){
		case Board.CROSS:
			buttons[board.getSide() * i + j].setText("X");
            buttonClicked.setForeground(Color.RED);
			break;
		case Board.RING:
			buttons[board.getSide() * i + j].setText("O");
            buttonClicked.setForeground(Color.BLUE);
			break;
		default:
			buttons[board.getSide() * i + j].setText("");
			break;
		}
    }
    
    public void update(){
    	update(this.board);
    }
    
    
    
    public void initializebuttons(int size)
    {
        for(int i = 0; i <= 120; i++)
        {
            buttons[i] = new MatrixButton("", size / i, size % i);
            buttons[i].setText("");
            buttons[i].addActionListener(new buttonListener());
            add(buttons[i]); //adds this button to JPanel (note: no need for JPanel.add(...)
                                //because this whole class is a JPanel already           
        }
    }
    
    public void resetButtons()
    {
    	board.clear();
        for(int i = 0; i <= 120; i++)
        {
            buttons[i].setText("");
        }
    }

   
// when a button is clicked, it generates an ActionEvent. Thus, each button needs an ActionListener. When it is clicked, it goes to this listener class that I have created and goes to the actionPerformed method. There (and in this class), we decide what we want to do.
    private class buttonListener implements ActionListener
    {
    	
    	
        public void actionPerformed(ActionEvent e) 
        {
            MatrixButton buttonClicked = (MatrixButton)e.getSource(); //get the particular button that was clicked
            if (buttonClicked.getText().equals(""))
            {
                if(xOrO == Board.CROSS){
                    buttonClicked.setText("X");
                    buttonClicked.setForeground(Color.RED);
                }
                else{
                    buttonClicked.setText("O");
                    buttonClicked.setForeground(Color.BLUE);
                }
                move = new Move(buttonClicked.getRow(), buttonClicked.getCol(), this.xOrO);
                //if(checkForWin() == true)
                //{
                //    JOptionPane.showConfirmDialog(null, "Game Over. Play again?");
                //    resetButtons();
                //}
            }
        }
    }
    
    public boolean checkForWin()
    {
        return false; 
            /**
            //   Reference: the button array is arranged like this as the board
            //     0 | 1 | 2
            //      3 | 4 | 5
            //      6 | 7 | 8
            
            //horizontal win check
            if( checkAdjacent(0,1) && checkAdjacent(1,2) ) //no need to put " == true" because the default check is for true
                return true;
            else if( checkAdjacent(3,4) && checkAdjacent(4,5) )
                return true;
            else if ( checkAdjacent(6,7) && checkAdjacent(7,8))
                return true;
            
            //vertical win check
            else if ( checkAdjacent(0,3) && checkAdjacent(3,6))
                return true;  
            else if ( checkAdjacent(1,4) && checkAdjacent(4,7))
                return true;
            else if ( checkAdjacent(2,5) && checkAdjacent(5,8))
                return true;
            
            //diagonal win check
            else if ( checkAdjacent(0,4) && checkAdjacent(4,8))
                return true;  
            else if ( checkAdjacent(2,4) && checkAdjacent(4,6))
                return true;
            else 
                return false;
            **/  
    }
    
    public boolean checkAdjacent(int a, int b)
    {
        if ( buttons[a].getText().equals(buttons[b].getText()) && !buttons[a].getText().equals("")) return true;
        else return false;
    }
    
    
    private class MatrixButton extends JButton
    {
    	private final int row;
        private final int col;

        public MatrixButton(String t, int col, int row) {
           	super(t);
           	this.row = row;
           	this.col = col;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
        	return col;
        }
            
        public int serialize(int size){
           	return row * size + col;
        }
           
        public int serialize(int size, int r, int c){
          	return r * size + c;
        }
    }
    
    
    
    
    public static void main(String[] args) 
    {
        JFrame window = new JFrame("5 IN A ROW!");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        window.setBounds(395,395,395,395);
        
        JPanel panel = new JPanel( new GridLayout(3, 3) );
        window.add(panel, BorderLayout.CENTER);
		Board b=new Board(11);
        final AiTest ai = new AiTest(b, 1);
        window.getContentPane().add(ai);
        JLabel txt = new JLabel("Player 1:  X          Player 2:  O", JLabel.CENTER);
 
        txt.setHorizontalTextPosition(JLabel.CENTER);
        txt.setFont(new Font("Ariel", Font.PLAIN, 18));
        window.add(txt, BorderLayout.NORTH);
        
        
        JButton reset = new JButton();
        
        reset.setText("Reset");
        reset.setPreferredSize(new Dimension(20, 30));
        reset.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            ai.resetButtons();
          }
        });
        window.add(reset, BorderLayout.SOUTH);
        
        window.setVisible(true);
         while (ai.checkForWin()==false)  
         {    
             //if (ai.turn>0)
                if (ai.xOrO == 1){           
                 txt.setText("Player 1:  X    [Player 1's Turn]     Player 2:  O");
                 txt.setForeground(Color.RED);
                }
                else {
                    txt.setText("Player 1:  X    [Player 2's Turn]     Player 2:  O");
                    txt.setForeground(Color.BLUE);
                }
        }
    }
}