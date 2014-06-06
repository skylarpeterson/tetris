// Board.java
package tetris;

import java.util.*;

/**
 CS108 Tetris Board.
 Represents a Tetris board -- essentially a 2-d grid
 of booleans. Supports tetris pieces and row clearing.
 Has an "undo" feature that allows clients to add and remove pieces efficiently.
 Does not do any drawing or have any idea of pixels. Instead,
 just represents the abstract 2-d board.
*/
public class Board	{
	// Some ivars are stubbed out for you:
	private int width;
	private int height;
	private boolean[][] grid;
	private boolean[][] backupGrid;
	private boolean DEBUG = true;
	boolean committed;
	
	private int[] widths;
	private int[] backupWidths;
	private int[] heights;
	private int[] backupHeights;
	
	
	// Here a few trivial methods are provided:
	
	/**
	 Creates an empty board of the given width and height
	 measured in blocks.
	*/
	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		grid = new boolean[width][height];
		backupGrid = new boolean[width][height];
		committed = true;
		
		this.widths = new int [height];
		this.backupWidths = new int [height];
		this.heights = new int [width];
		this.backupHeights = new int [width];
	}
	
	
	/**
	 Returns the width of the board in blocks.
	*/
	public int getWidth() {
		return width;
	}
	
	
	/**
	 Returns the height of the board in blocks.
	*/
	public int getHeight() {
		return height;
	}
	
	
	/**
	 Returns the max column height present in the board.
	 For an empty board this is 0.
	*/
	public int getMaxHeight() {	 
		int maxHeight = 0;
		for(int i = 0; i < this.width; i++){
			if(heights[i] > maxHeight) maxHeight = heights[i];
		}
		return maxHeight;
	}
	
	
	/**
	 Checks the board for internal consistency -- used
	 for debugging.
	*/
	public void sanityCheck() {
		if (DEBUG) {
			int maxHeight = 0;
			
			for(int r = 0; r < grid[0].length; r++){
				int rowCount = 0;
				for(int c = 0; c < grid.length; c++){
					if(grid[c][r]) rowCount++;
				}
				if(getRowWidth(r) != rowCount) throw new RuntimeException("row width problem");
			}
			
			for(int c = 0; c < grid.length; c++){
				int colHeight = 0;
				for(int r = 0; r < grid[0].length; r++){
					if(grid[c][r])colHeight = r + 1;
				}
				if(getColumnHeight(c) != colHeight) throw new RuntimeException("col height problem");
				if(colHeight > maxHeight) maxHeight = colHeight;
			}
			
			if(maxHeight != getMaxHeight()) throw new RuntimeException("max height problem");
		}
	}
	
	/**
	 Given a piece and an x, returns the y
	 value where the piece would come to rest
	 if it were dropped straight down at that x.
	 
	 <p>
	 Implementation: use the skirt and the col heights
	 to compute this fast -- O(skirt length).
	*/
	public int dropHeight(Piece piece, int x) {
		
		if(x < 0 || x + piece.getWidth() > this.width) return -1;
		
		int yValue = this.heights[x];
		int[] skirt = piece.getSkirt();
		int originalOffset = skirt[0];
		for(int i = 1; i < piece.getWidth(); i++) {
			int colHeight = this.heights[x + i];
			int currentOffset = skirt[i];
			if(yValue - originalOffset + currentOffset < colHeight) yValue = colHeight;
		}
		
		if(yValue + piece.getHeight() > this.height) return -1;
		return yValue;
	}
	
	
	/**
	 Returns the height of the given column --
	 i.e. the y value of the highest block + 1.
	 The height is 0 if the column contains no blocks.
	*/
	public int getColumnHeight(int x) {
		return heights[x];
	}
	
	
	/**
	 Returns the number of filled blocks in
	 the given row.
	*/
	public int getRowWidth(int y) {
		 return widths[y];
	}
	
	
	/**
	 Returns true if the given block is filled in the board.
	 Blocks outside of the valid width/height area
	 always return true.
	*/
	public boolean getGrid(int x, int y) {
		if(x < 0 || x >= this.width || y < 0 || y >= this.height) return true;
		return this.grid[x][y];
	}
	
	
	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;
	
	/**
	 Attempts to add the body of a piece to the board.
	 Copies the piece blocks into the board grid.
	 Returns PLACE_OK for a regular placement, or PLACE_ROW_FILLED
	 for a regular placement that causes at least one row to be filled.
	 
	 <p>Error cases:
	 A placement may fail in two ways. First, if part of the piece may falls out
	 of bounds of the board, PLACE_OUT_BOUNDS is returned.
	 Or the placement may collide with existing blocks in the grid
	 in which case PLACE_BAD is returned.
	 In both error cases, the board may be left in an invalid
	 state. The client can use undo(), to recover the valid, pre-place state.
	*/
	public int place(Piece piece, int x, int y) {
		// flag !committed problem
		if (!committed) throw new RuntimeException("place commit problem");
			
		System.arraycopy(heights, 0, backupHeights, 0, heights.length);
		System.arraycopy(widths, 0, backupWidths, 0, widths.length);
		for(int w = 0; w < width; w++){
			System.arraycopy(grid[w], 0, backupGrid[w], 0, grid[w].length);
		}
		
		int result = PLACE_OK;
		int rowsFilled = 0;
		
		if(x < 0 || y < 0) return PLACE_OUT_BOUNDS;
		if(x + piece.getWidth() > this.width) return PLACE_OUT_BOUNDS;
		if(y + piece.getHeight() > this.height) return PLACE_OUT_BOUNDS;
		
		TPoint[] body = piece.getBody();
		for(int i = 0; i < body.length; i++){
			int pieceX = body[i].x;
			int pieceY = body[i].y;
			if(this.grid[x + pieceX][y + pieceY]) return PLACE_BAD;
			this.widths[y + pieceY]++;
			if(this.widths[y + pieceY] == this.width) rowsFilled++;
			if(this.heights[x + pieceX] < y + pieceY + 1) this.heights[x + pieceX] = y + pieceY + 1;
			this.grid[x+pieceX][y+pieceY] = true;
		}
		
		sanityCheck();
		committed = false;
		if(rowsFilled > 0) return PLACE_ROW_FILLED;
		return result;
	}
	
	
	/**
	 Deletes rows that are filled all the way across, moving
	 things above down. Returns the number of rows cleared.
	*/
	
	public int clearRows() {
		
		if(committed) {
			System.arraycopy(heights, 0, backupHeights, 0, heights.length);
			System.arraycopy(widths, 0, backupWidths, 0, widths.length);
			for(int w = 0; w < width; w++){
				System.arraycopy(grid[w], 0, backupGrid[w], 0, grid[w].length);
			}
		}
		int rowsCleared = 0;
		int to = firstFull();
		if (to >= 0) {
			rowsCleared++;
			int from = to + 1;
			while(from != height){
				if(widths[from] != width){
					int newWidth = 0;
					for(int i = 0; i < width; i ++){
						grid[i][to] = grid[i][from];
						if(grid[i][to]) newWidth++;
					}
					widths[to] = newWidth;
					to++;
				} else {
					rowsCleared++;
				}
				from++;
			}			
			for(int j = to; j < height; j++){
				for(int k = 0; k < width; k++){
					grid[k][j] = false;
				}
				widths[j] = 0;
			}
			adjustHeights();
		}
		sanityCheck();
		committed = false;
		return rowsCleared;
	}
	
	private int firstFull() {
		for(int i = 0; i < height; i++){
			if(widths[i] == width) return i;
		}
		return -1;
	}
	
	private void adjustHeights(){
		for(int i = 0; i < width; i++){
			heights[i] = 0;
			for(int j = 0; j < height; j++) {
				if(grid[i][j]) {
					heights[i] = j + 1;
				}
			}
		}
	}
	

	/**
	 Reverts the board to its state before up to one place
	 and one clearRows();
	 If the conditions for undo() are not met, such as
	 calling undo() twice in a row, then the second undo() does nothing.
	 See the overview docs.
	*/
	public void undo() {
		if(committed) return;
		System.arraycopy(backupHeights, 0, heights, 0, heights.length);
		System.arraycopy(backupWidths, 0, widths, 0, widths.length);
		for(int w = 0; w < width; w++){
			System.arraycopy(backupGrid[w], 0, grid[w], 0, grid[w].length);
		}
		committed = true;
	}
	
	
	/**
	 Puts the board in the committed state.
	*/
	public void commit() {
		committed = true;
	}


	
	/*
	 Renders the board state as a big String, suitable for printing.
	 This is the sort of print-obj-state utility that can help see complex
	 state change over time.
	 (provided debugging utility) 
	 */
	public String toString() {
		StringBuilder buff = new StringBuilder();
		for (int y = height-1; y>=0; y--) {
			buff.append('|');
			for (int x=0; x<width; x++) {
				if (getGrid(x,y)) buff.append('+');
				else buff.append(' ');
			}
			buff.append("|\n");
		}
		for (int x=0; x<width+2; x++) buff.append('-');
		return(buff.toString());
	}
}


