package tetris;

import static org.junit.Assert.*;

import org.junit.*;

public class BoardTest {
	Board b;
	Piece pyr1, pyr2, pyr3, pyr4, s, sRotated;

	// This shows how to build things in setUp() to re-use
	// across tests.
	
	// In this case, setUp() makes shapes,
	// and also a 3X6 board, with pyr placed at the bottom,
	// ready to be used by tests.
	@Before
	public void setUp() throws Exception {
		b = new Board(3, 6);
		
		pyr1 = new Piece(Piece.PYRAMID_STR);
		pyr2 = pyr1.computeNextRotation();
		pyr3 = pyr2.computeNextRotation();
		pyr4 = pyr3.computeNextRotation();
		
		s = new Piece(Piece.S1_STR);
		sRotated = s.computeNextRotation();
		
		b.place(pyr1, 0, 0);
	}
	
	// Check the basic width/height/max after the one placement
	@Test
	public void testSample1() {
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(2, b.getColumnHeight(1));
		assertEquals(2, b.getMaxHeight());
		assertEquals(3, b.getRowWidth(0));
		assertEquals(1, b.getRowWidth(1));
		assertEquals(0, b.getRowWidth(2));
	}
	
	// Place sRotated into the board, then check some measures
	@Test
	public void testSample2() {
		b.commit();
		assertTrue(b.getGrid(0, 0));
		assertTrue(b.getGrid(1, 1));
		assertTrue(b.getGrid(-1, 0));
		assertTrue(b.getGrid(0, -1));
		assertTrue(b.getGrid(3, 0));
		assertTrue(b.getGrid(0, 6));
		assertTrue(b.getGrid(3, 6));
		assertFalse(b.getGrid(0, 1));
		assertFalse(b.getGrid(1, 2));
	}
	
	@Test
	public void testSample3() {
		b.commit();
		int result = b.place(sRotated, 1, 1);
		assertEquals(Board.PLACE_OK, result);
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(4, b.getColumnHeight(1));
		assertEquals(3, b.getColumnHeight(2));
		assertEquals(4, b.getMaxHeight());
	}
	
	@Test
	public void testSample4() {
		Board b2 = new Board(5,5);
		Piece weirdPiece = new Piece("0 0  0 1  0 2  0 3  0 4  1 0  2 0  2 1  3 0  3 1  4 0  4 1");
		b2.place(weirdPiece, 0, 0);
		b2.commit();
		b2.place(pyr4, 1, 1);
		assertEquals(5, b2.getRowWidth(0));
		assertEquals(5, b2.getRowWidth(1));
		assertEquals(3, b2.getRowWidth(2));
		assertEquals(5, b2.getColumnHeight(0));
		assertEquals(4, b2.getColumnHeight(1));
	}
	
	@Test
	public void testBadPlace1() {
		Board b2 = new Board(3, 6);
		Piece longPiece = new Piece("0 0  1 0  2 0  3 0");
		int result = b2.place(longPiece, 0, 0);
		assertEquals(Board.PLACE_OUT_BOUNDS, result);
	}
	
	@Test
	public void testBadPlace2() {
		Board b2 = new Board(3, 6);
		int result = b2.place(pyr1, 0, 0);
		assertEquals(Board.PLACE_ROW_FILLED, result);
		assertEquals(1, b2.getColumnHeight(0));
		assertEquals(2, b2.getColumnHeight(1));
		assertEquals(1, b2.getColumnHeight(2));
		assertEquals(2, b2.getMaxHeight());
		b2.commit();
		
		int result2 = b2.place(new Piece(Piece.S2_STR), 0, 0);
		assertEquals(Board.PLACE_BAD, result2);
		assertEquals(2, b2.getColumnHeight(0));
		assertEquals(2, b2.getColumnHeight(1));
		assertEquals(1, b2.getColumnHeight(2));
		assertEquals(2, b2.getMaxHeight());
		assertEquals(2, b2.getRowWidth(1));
	}
	
	@Test
	public void testClearRows1() {
		Board b2 = new Board(3, 6);
		b2.place(pyr1, 0, 0);
		int rowsCleared = b2.clearRows();
		assertEquals(1, rowsCleared);
		assertEquals(1, b2.getColumnHeight(1));
		assertEquals(1, b2.getRowWidth(0));
	}
	
	@Test
	public void testClearRows2() {
		Board b2 = new Board(4, 8);
		Piece longPiece = new Piece("0 0  1 0  2 0  3 0");
		b2.place(longPiece, 0, 0);
		b2.commit();
		b2.place(pyr1, 0, 1);
		b2.commit();
		b2.place(longPiece, 0, 3);
		int rowsCleared = b2.clearRows();
		assertEquals(2, rowsCleared);
		assertEquals(2, b2.getColumnHeight(1));
		assertEquals(3, b2.getRowWidth(0));
	}
	
	@Test
	public void testClearRows3() {
		Board b2 = new Board(4, 8);
		Piece longPiece = new Piece("0 0  1 0  2 0  3 0");
		b2.place(longPiece, 0, 0);
		b2.commit();
		b2.place(longPiece, 0, 1);
		b2.commit();
		b2.place(longPiece, 0, 2);
		b2.commit();
		b2.place(pyr1, 0, 3);
		int rowsCleared = b2.clearRows();
		assertEquals(3, rowsCleared);
		assertEquals(2, b2.getColumnHeight(1));
		assertEquals(3, b2.getRowWidth(0));
		assertEquals(1, b2.getRowWidth(1));
	}
	
	@Test
	public void testClearRows4() {
		Board b2 = new Board(4, 2);
		Piece longPiece = new Piece("0 0  1 0  2 0  3 0");
		b2.place(longPiece, 0, 0);
		b2.commit();
		b2.place(longPiece, 0, 1);
		int rowsCleared = b2.clearRows();
		assertEquals(2, rowsCleared);
		assertEquals(0, b2.getRowWidth(0));
		assertEquals(0, b2.getColumnHeight(0));
	}
	
	@Test
	public void testDropHeight1() {
		Board b2 = new Board(3, 6);
		b2.place(pyr1, 0, 0);
		assertEquals(2, b2.dropHeight(pyr1, 0));
	}
	
	@Test
	public void testDropHeight2() {
		Board b2 = new Board(10, 20);
		assertEquals(-1, b2.dropHeight(pyr1, 9));
		assertEquals(0, b2.dropHeight(s, 3));
		Piece sqr = new Piece(Piece.SQUARE_STR);
		b2.place(sqr, 2, 0);
		assertEquals(2, b2.dropHeight(pyr1, 2));
	}
	
	@Test
	public void testUndo1() {
		Board b2 = new Board(3, 6);
		b2.place(pyr1, 0, 0);
		b2.undo();
		assertEquals(0, b2.getRowWidth(0));
		assertEquals(0, b2.getColumnHeight(0));
	}
	
	@Test
	public void testUndo2() {
		Board b2 = new Board(10, 20);
		b2.place(pyr1, 0, 0);
		b2.commit();
		Piece sqr = new Piece(Piece.SQUARE_STR);
		b2.place(sqr, 3, 0);
		b2.commit();
		int badPlace = b2.place(sqr, 4, 0);
		assertEquals(Board.PLACE_BAD, badPlace);
		b2.undo();
		assertEquals(5, b2.getRowWidth(0));
		assertEquals(1, b2.getColumnHeight(0));
	}
	
}
