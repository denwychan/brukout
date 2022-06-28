import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Implementation of the classic Breakout arcade game as
 * 'Brukout' inspired by its Caribbean dance aesthetics
 * @author Denise Chan
 */

public class Breakout extends GraphicsProgram {

	/** Width and height of application window in pixels */
	public static final int APPLICATION_WIDTH = 400;
	public static final int APPLICATION_HEIGHT = 600;

	/** Dimensions of game board (same as the application window) */
	private static final int WIDTH = APPLICATION_WIDTH;
	private static final int HEIGHT = APPLICATION_HEIGHT;

	/** Dimensions of the paddle */
	private static final int PADDLE_WIDTH = 60;
	private static final int PADDLE_HEIGHT = 10;

	/** Offset of the paddle up from the bottom */
	private static final int PADDLE_Y_OFFSET = 30;

	/** Number of bricks per row */
	private static final int NBRICKS_PER_ROW = 10;

	/** Number of rows of bricks */
	private static final int NBRICK_ROWS = 10;

	/** Separation between bricks */
	private static final int BRICK_SEP = 4;

	/** Width of a brick */
	private static final int BRICK_WIDTH =
	  (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

	/** Height of a brick */
	private static final int BRICK_HEIGHT  = 8;

	/** Radius of the ball in pixels */
	private static final int BALL_RADIUS = 10;

	/** Offset of the top brick row from the top */
	private static final int BRICK_Y_OFFSET = 70;

	/** Number of turns */
	private static final int NTURNS = 3;
	
	/** Animation delay or pause time between ball moves */
	private static final int DELAY = 10; 
	
	/** Animation delay or pause time for countdown */
	private static final int COUNTDOWN_DELAY = 500;
	
	/** Animation delay or pause time for messages */
	private static final int MESSAGE_DELAY = 3000; 	

	/** Width of the text boxes */
	private static final double TEXT_BOX_WIDTH = 180;
		
	/** Height of the text boxes */
	private static final double TEXT_BOX_HEIGHT = 50;
	
	/* Private instance variables*/
	
	// The paddle being moved
	private GRect paddle;
	
	// The ball being bounced
	private GOval ball;
	
	// The velocity of the ball in y direction
	private double vy;
	
	// The velocity of the ball in y direction
	private double vx;
	
	// Factor by which the velocity is increased
	private double vf = 1.02;
	
	// The velocity of the ball of the ball in x direction
	private RandomGenerator rgen = RandomGenerator.getInstance();
	
	// The score accrued over the game
	private int score = 0; 
	
	/** Runs the 'Brukout' program. **/
	public void run() {
		//Set up game
		addMouseListeners(); 
		setUpBricks();
		setUpPaddle();
		setUpBall();

		playGame();
		
		//Finish game
		displayFinalScore();
		addTwerkGirl();
	}

	// Set up rows of multi-coloured bricks at the top to be removed by the ball
	private void setUpBricks() {
		// Set y position of row
		int y = BRICK_Y_OFFSET; 
		
		
		for (int j = 0; j < NBRICK_ROWS; j++) {
			int x = BRICK_SEP/ 2; 
			for (int i = 0; i < NBRICKS_PER_ROW; i++) {	
				// Create new brick
				GRect brick = new GRect (x , y, BRICK_WIDTH, BRICK_HEIGHT);
				add (brick);
				// Set the colour of the brick
				switch (j/2) {
					case 0:
						setBrickOrPaddleColour(brick, Color.red); 
						break;
					case 1: 
						setBrickOrPaddleColour(brick, Color.orange); 
						break;
					case 2: 
						setBrickOrPaddleColour(brick, Color.yellow); 
						break;
					case 3:
						setBrickOrPaddleColour(brick, Color.green); 
						break;
					case 4:
						setBrickOrPaddleColour(brick, Color.cyan); 
						break;
					default:
						setBrickOrPaddleColour(brick, Color.white);
				}
				// Add new brick
				x += (BRICK_WIDTH + BRICK_SEP);
			}
			// Go to next row
			y += BRICK_HEIGHT + BRICK_SEP;
		}
	}
	
	// Set the colour of the bricks and the paddle
	private void setBrickOrPaddleColour(GRect brickOrPaddle, Color col) {
		brickOrPaddle.setColor(col);
		brickOrPaddle.setFillColor(col);
		brickOrPaddle.setFilled(true);
	}
	
	// Set up the paddle to be moved by the player
	private void setUpPaddle() {
		double x = (APPLICATION_WIDTH - PADDLE_WIDTH)/ 2.0; 
		paddle = new GRect(x, (APPLICATION_HEIGHT- PADDLE_HEIGHT - PADDLE_Y_OFFSET), PADDLE_WIDTH, PADDLE_HEIGHT);
		add(paddle); 
		setBrickOrPaddleColour(paddle, Color.black);
	}
	
	// Set up the ball which is bounced by the paddle
	private void setUpBall() {
		ball = new GOval (BALL_RADIUS * 2,  BALL_RADIUS * 2);
		ball.move((APPLICATION_WIDTH/2 - BALL_RADIUS), (APPLICATION_HEIGHT/2 - BALL_RADIUS));
		add (ball);
		setBallColour(ball, Color.red);
	}
	
	// Set the colour of the ball
	private void setBallColour(GOval ball, Color col) {
		ball.setColor(col);
		ball.setFillColor(col);
		ball.setFilled(true);
	}
	
	// Move paddle when the player moves the mouse
	public void mouseMoved(MouseEvent e) {
		if(e.getX() < (APPLICATION_WIDTH - PADDLE_WIDTH) && paddle != null) {
			paddle.move(e.getX() - paddle.getX() , 0);
		}
	}
	
	// Set the initial velocities of the ball in x and y direction
	private void initBallVelocities() {
		vy = 3.0; 
		vx = rgen.nextDouble(1.0, 3.0); 
		if (rgen.nextBoolean(0.5)) vx = -vx;
	}
	
	// Play the game consisting of n turns specified above
	private void playGame() {
		for (int i = 1; i <= NTURNS; i++) {
			// Count down to the ball being bounced
			countDown(i);
			initBallVelocities();
			
			// Player plays a turn
			playTurn();
			
			// If the player loses the turn, display the 'game over' message and resets the ball
			if (gameOver()) {
			addRemoveMessageBox("Oops, game over! Score: " + score);
			}
			remove(ball);
			setUpBall();
			
			// If the player wins the game, display the 'you win' message
			if (maxScore()) {
			addRemoveMessageBox("Nice, You win! Score: " + score);
			break;
			}
		}
	}
	
	/* 
	 * Play a turn in the game by moving the ball around to be bounced by the paddle
	 * and removing the bricks when the ball collides with them
	 */
	private void playTurn() {
		//While the player hasn't lost the game
		while (!gameOver()) {
			//Move the ball
			ball.move(vx,vy);
			pause(DELAY);
			
			checkforWallCollision();
			GObject collider = getCollidingObject();
			//Collision with paddle always causes the ball to bounce up to prevent paddle-sticking bug
			if (collider == paddle) {
				vy = - Math.abs(vy) * vf;
				vx = vx * vf;
			}
			//Remove the collider if it is a brick
			if (collider != null && collider != paddle) {
				vy = -vy; 
				remove(collider);
				score++; 
			}
			// Stop the game if the player wins the game by removing all the bricks
			if (maxScore()){
				break;
			}
		}
	}
	 
	 // Game-over when the ball collides with bottom wall
	private boolean gameOver() {
		 return (ball.getY() > (HEIGHT - (2 * BALL_RADIUS)));
	 }
	
	// Maximum score to finish the game when all bricks have been removed
	private boolean maxScore() {
		return (score == NBRICKS_PER_ROW * NBRICK_ROWS);
	}
	
	 // Check for collisions with walls other than the bottom wall and update the ball's velocity
	 private void checkforWallCollision() {
		 // Check for collisions with top wall and change the velocity in the y direction
		 if ((ball.getY() < 0)) {
			 vy = -vy; 
		 }
		// Check for collisions with left and right walls and change the velocity in the x direction
		 if ((ball.getX() > WIDTH - 2 * BALL_RADIUS) || (ball.getX() < 0)) {
			 vx = -vx; 
		 }
	 }
	 
	 // Get colliding object touching the ball itself
	 private GObject getCollidingObject() {
		 double ballDimension = 2 * BALL_RADIUS; 
		 double x = ball.getX();
		 double y = ball.getY();

		 // Check the corners of the ball to find the collider object for removal
	     GObject collider = getElementAt(x, y);
	     if (collider == null) {
	    	 collider = getElementAt(x + ballDimension, y);
	     }
	     if (collider == null) {
	    	 collider = getElementAt(x, y + ballDimension);
	     }
	     if (collider == null) {
	    	 collider = getElementAt(x + ballDimension, y + ballDimension);
	     }
	     return collider;
	 }
	
	//Add a new text box with custom text
	private GCompound addTextBox(String text) {
		// Initialise the text box, box and label
		GCompound textBox = new GCompound();
		GRect box = new GRect (TEXT_BOX_WIDTH, TEXT_BOX_HEIGHT);
		GLabel label = new GLabel(text);
		
		// Put the box in the centre of the screen
		double boxX = (WIDTH - box.getWidth())/ 2; 
	 	double boxY = (HEIGHT - box.getHeight())/2;
	 	box.setLocation(boxX, boxY);
	 	// Set the box's colour to black
 	 	box.setColor(Color.black);
 	 	box.setFilled(true);
 	 	box.setFillColor(Color.black);
	 	
	 	// Put the label in the centre of the screen
	 	double labelX = (WIDTH - label.getWidth())/ 2; 
	 	double labelY = (HEIGHT + label.getAscent())/2;
	 	label.setLocation(labelX, labelY);
	 	// Set the label's colour to white
	 	label.setColor(Color.WHITE);
	 	
	 	// Combine the box and label to make a text box
	 	textBox.add(box);
	 	textBox.add(label);
	 	add(textBox);
	 	return textBox;
	}

	//Add and remove text box for messages to appear on screen for a specified time
	private void addRemoveMessageBox(String text) {
		GCompound textBox = addTextBox(text);
		pause(MESSAGE_DELAY);
		remove(textBox);
	}
			
	//Add and remove text box for the count down to occur at the beginning of the game
	private GCompound addRemoveTextBox(String text) {
		GCompound textBox = addTextBox(text);
		pause(COUNTDOWN_DELAY);
		remove (textBox);
		return(textBox);
	}
	
	//Do the count down to the game for each turn
	private void countDown(int i) {
		addRemoveTextBox ("Round " + i);
		addRemoveTextBox ("Three");
		addRemoveTextBox ("Two");
		addRemoveTextBox ("One");
		addRemoveTextBox ("Go!!");
	}
	
	// Display final score at the end of the game
	private void displayFinalScore() {
		GCompound finalScore = addTextBox("Final score: " + score);
		finalScore.move(0, - HEIGHT/2.5);
	}
	
	//Add Twerk Girl so she can 'brukout' and dance
	private void addTwerkGirl() {
		// Initialise the images to create the dancing Twerk Girl
		GImage twerkUp = new GImage("TwerkUp.jpg");
		GImage twerkDown = new GImage("TwerkDown.jpg");
		
		// Place Twerk Girl centrally in the screen and scale her appropriately
		double twerkGirlScale = WIDTH/ 2.5/ twerkUp.getWidth();
		twerkUp.scale(twerkGirlScale);
		twerkDown.scale(twerkGirlScale);
		twerkUp.setLocation((WIDTH - twerkUp.getWidth())/2, (HEIGHT- twerkUp.getHeight())/2);
		twerkDown.setLocation((WIDTH - twerkDown.getWidth())/2, (HEIGHT- twerkDown.getHeight())/2);
		add(twerkUp);
		add(twerkDown);
		
		// Make Twerk Girl dance
		while (true) {
			pause(COUNTDOWN_DELAY);
			twerkDown.setVisible(false);
			pause(COUNTDOWN_DELAY);
			twerkDown.setVisible(true);
		}
	}
	
}
