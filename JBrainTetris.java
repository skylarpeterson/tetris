package tetris;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tetris.Brain.Move;

public class JBrainTetris extends JTetris {
	
	private DefaultBrain brain;
	private int moveCount;
	private int adversaryLevel;
	private Brain.Move move;
	
	protected JSlider adversary;
	protected JLabel adversaryLabel;
	protected JLabel brainLabel;
	protected JCheckBox brainMode;

	JBrainTetris(int pixels) {
		super(pixels);
		brain = new DefaultBrain();
		moveCount = count;
		move = new Brain.Move();
	}
	
	@Override
	public Piece pickNextPiece() {
		int randomInt = random.nextInt(99) + 1;
		if(adversaryLevel == 0 || randomInt >= adversaryLevel) {
			adversaryLabel.setText("Ok");
			return super.pickNextPiece();
		}
		else {
			adversaryLabel.setText("*Ok*");
			Piece[] pieces = Piece.getPieces();
			Brain.Move worst = new Brain.Move();
			for(int i = 0; i < pieces.length; i++){
				Brain.Move curr = brain.bestMove(board, pieces[i], board.getHeight(), null);
				if(curr.score > worst.score) worst = curr;
			}
			return worst.piece;
		}
	}
	
	private void computeGoal(){
		brain.bestMove(board, currentPiece, board.getHeight(), move);
	}
	
	private void updateCounters() {
		countLabel.setText("Pieces " + count);
		scoreLabel.setText("Score " + score);
		if(brainMode.isSelected()) brainLabel.setText("Brain: On");
		else brainLabel.setText("Brain: Off");
	}
	
	@Override
	public void tick(int verb){
		if (!gameOn) return;
		
		if (currentPiece != null) {
			board.undo();	// remove the piece from its old position
		}
		
		updateCounters();
		computeNewPosition(verb);
		
		// Sets the newXXX ivars
		if(brainMode.isSelected() && verb == JTetris.DOWN){
			if(count > moveCount) {
				board.undo();
				computeGoal();
				moveCount++;
			}
			if(!newPiece.equals(move.piece)) newPiece = newPiece.computeNextRotation();
			if(newX < move.x) newX++;
			else if (newX > move.x) newX--;
//			if(newY > move.y) newY--; 
		}
		
		// try out the new position (rolls back if it doesn't work)
		int result = setCurrent(newPiece, newX, newY);
		
		// if row clearing is going to happen, draw the
		// whole board so the green row shows up
		if (result ==  Board.PLACE_ROW_FILLED) {
			repaint();
		}
		

		boolean failed = (result >= Board.PLACE_OUT_BOUNDS);
		
		// if it didn't work, put it back the way it was
		if (failed) {
			if (currentPiece != null) board.place(currentPiece, currentX, currentY);
			repaintPiece(currentPiece, currentX, currentY);
		}
		
		/*
		 How to detect when a piece has landed:
		 if this move hits something on its DOWN verb,
		 and the previous verb was also DOWN (i.e. the player was not
		 still moving it),	then the previous position must be the correct
		 "landed" position, so we're done with the falling of this piece.
		*/
		if (failed && verb==DOWN && !moved) {	// it's landed
		
			int cleared = board.clearRows();
			if (cleared > 0) {
				// score goes up by 5, 10, 20, 40 for row clearing
				// clearing 4 gets you a beep!
				switch (cleared) {
					case 1: score += 5;	 break;
					case 2: score += 10;  break;
					case 3: score += 20;  break;
					case 4: score += 40; Toolkit.getDefaultToolkit().beep(); break;
					default: score += 50;  // could happen with non-standard pieces
				}
				updateCounters();
				repaint();	// repaint to show the result of the row clearing
			}
			
			
			// if the board is too tall, we've lost
			if (board.getMaxHeight() > board.getHeight() - TOP_SPACE) {
				stopGame();
			}
			// Otherwise add a new piece and keep playing
			else {
				addNewPiece();
			}
		}
		
		// Note if the player made a successful non-DOWN move --
		// used to detect if the piece has landed on the next tick()
		moved = (!failed && verb!=DOWN);
	}
	
	@Override
	public JComponent createControlPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		// COUNT
		countLabel = new JLabel("0");
		panel.add(countLabel);
		
		// SCORE
		scoreLabel = new JLabel("0");
		panel.add(scoreLabel);
		
		// TIME 
		timeLabel = new JLabel(" ");
		panel.add(timeLabel);
		
		// BRAIN
		brainLabel = new JLabel("Brain: Off");
		panel.add(brainLabel);
		
		adversaryLabel = new JLabel("Ok");
		panel.add(adversaryLabel);
		
		panel.add(Box.createVerticalStrut(12));
		
		// START button
		startButton = new JButton("Start");
		panel.add(startButton);
		startButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startGame();
			}
		});
		
		// STOP button
		stopButton = new JButton("Stop");
		panel.add(stopButton);
		stopButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopGame();
			}
		});
		
		enableButtons();
		
		JPanel row = new JPanel();
		
		// SPEED slider
		panel.add(Box.createVerticalStrut(12));
		row.add(new JLabel("Speed:"));
		speed = new JSlider(0, 200, 75);	// min, max, current
		speed.setPreferredSize(new Dimension(100, 15));
		
		
		updateTimer();
		row.add(speed);
		
		panel.add(row);
		speed.addChangeListener( new ChangeListener() {
			// when the slider changes, sync the timer to its value
			public void stateChanged(ChangeEvent e) {
				updateTimer();
			}
		});
		
		// ADVERSARY
		
		JPanel little = new JPanel();
		
		panel.add(Box.createVerticalStrut(12));
		little.add(new JLabel("Adversary"));
		adversary = new JSlider(0, 100, 0);
		adversary.setPreferredSize(new Dimension(100,15));
		little.add(adversary);
		
		panel.add(little);
		adversary.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateAdversary();
			}
		});
		
		testButton = new JCheckBox("Test sequence");
		panel.add(testButton);
		
		brainMode = new JCheckBox("Brain active");
		panel.add(brainMode);
		
		return panel;
	}
	
	private void enableButtons() {
		startButton.setEnabled(!gameOn);
		stopButton.setEnabled(gameOn);
	}
	
	private void updateAdversary(){
		adversaryLevel = adversary.getValue();
	}
	
	/**
	 * Creates and returns a frame around the given JTetris.
	 * The new frame is not visible.
	 */
	public static JFrame createFrame(JBrainTetris tetris) {		
		JFrame frame = new JFrame("Stanford Tetris");
		JComponent container = (JComponent)frame.getContentPane();
		container.setLayout(new BorderLayout());

		// Install the passed in JTetris in the center.
		container.add(tetris, BorderLayout.CENTER);
		
		// Create and install the panel of controls.
		JComponent controls = tetris.createControlPanel();
		container.add(controls, BorderLayout.EAST);
		
		// Add the quit button last so it's at the bottom
		controls.add(Box.createVerticalStrut(12));
		JButton quit = new JButton("Quit");
		controls.add(quit);
		quit.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		
		return frame;
	}
	
	/**
	 Creates a frame with a JTetris.
	*/
	public static void main(String[] args) {
		// Set GUI Look And Feel Boilerplate.
		// Do this incantation at the start of main() to tell Swing
		// to use the GUI LookAndFeel of the native platform. It's ok
		// to ignore the exception.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) { }
		
		JBrainTetris tetris = new JBrainTetris(16);
		JFrame frame = JBrainTetris.createFrame(tetris);
		frame.setVisible(true);
	}
}
