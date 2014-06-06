package tetris;

import static org.junit.Assert.*;
import java.util.*;

import org.junit.*;

/*
  Unit test for Piece class -- starter shell.
 */
public class PieceTest {
	// You can create data to be used in the your
	// test cases like this. For each run of a test method,
	// a new PieceTest object is created and setUp() is called
	// automatically by JUnit.
	// For example, the code below sets up some
	// pyramid and s pieces in instance variables
	// that can be used in tests.
	private Piece pyr1, pyr2, pyr3, pyr4;
	private Piece s, sRotated;

	@Before
	public void setUp() throws Exception {
		
		pyr1 = new Piece(Piece.PYRAMID_STR);
		pyr2 = pyr1.computeNextRotation();
		pyr3 = pyr2.computeNextRotation();
		pyr4 = pyr3.computeNextRotation();
		
		s = new Piece(Piece.S1_STR);
		sRotated = s.computeNextRotation();
	}
	
	// Here are some sample tests to get you started
	
	@Test
	public void testSampleSize() {
		// Check size of pyr piece
		assertEquals(3, pyr1.getWidth());
		assertEquals(2, pyr1.getHeight());
		
		// Now try after rotation
		// Effectively we're testing size and rotation code here
		assertEquals(2, pyr2.getWidth());
		assertEquals(3, pyr2.getHeight());
		
		// Now try with some other piece, made a different way
		Piece l = new Piece(Piece.STICK_STR);
		assertEquals(1, l.getWidth());
		assertEquals(4, l.getHeight());
		
		Piece L1 = new Piece(Piece.L1_STR);
		assertEquals(2, L1.getWidth());
		assertEquals(3, L1.getHeight());
		
		Piece SQ = new Piece(Piece.SQUARE_STR);
		assertEquals(SQ.getHeight(), SQ.getWidth());
		Piece SQ2 = SQ.computeNextRotation();
		assertEquals(SQ.getWidth(), SQ2.getHeight());
	}
	
	
	// Test the skirt returned by a few pieces
	@Test
	public void testSampleSkirt() {
		// Note must use assertTrue(Arrays.equals(... as plain .equals does not work
		// right for arrays.
		assertTrue(Arrays.equals(new int[] {0, 0, 0}, pyr1.getSkirt()));
		assertTrue(Arrays.equals(new int[] {1, 0, 1}, pyr3.getSkirt()));
		
		assertTrue(Arrays.equals(new int[] {0, 0, 1}, s.getSkirt()));
		assertTrue(Arrays.equals(new int[] {1, 0}, sRotated.getSkirt()));
		
		Piece l = new Piece(Piece.STICK_STR);
		assertTrue(Arrays.equals(new int[] {0}, l.getSkirt()));
		assertTrue(Arrays.equals(new int[] {0, 0, 0, 0}, l.computeNextRotation().getSkirt()));
		
		Piece L1 = new Piece(Piece.L1_STR);
		assertTrue(Arrays.equals(new int[] {0, 0}, L1.getSkirt()));
		assertTrue(Arrays.equals(new int[] {2, 0}, L1.computeNextRotation().computeNextRotation().getSkirt()));
	}
	
	@Test
	public void testFastRotations() {
		assertTrue(pyr2.equals(pyr1.fastRotation()));
		assertTrue(pyr3.equals(pyr1.fastRotation().fastRotation()));
		assertTrue(pyr4.equals(pyr1.fastRotation().fastRotation().fastRotation()));
		
		assertTrue(sRotated.equals(s.fastRotation()));
		assertTrue(sRotated.computeNextRotation().equals(s.fastRotation().fastRotation()));
		
		Piece newPiece = new Piece(Piece.SQUARE_STR);
		Piece newPiece2 = newPiece.computeNextRotation();
		Piece newPiece3 = newPiece.computeNextRotation();
		
		assertTrue(newPiece.equals(newPiece2));
		assertTrue(newPiece.equals(newPiece2.fastRotation()));
		assertTrue(newPiece3.equals(newPiece2.fastRotation()));
		
		Piece stick = new Piece(Piece.STICK_STR);
		assertFalse(stick.computeNextRotation().equals(stick.fastRotation().fastRotation()));
		assertTrue(stick.equals(stick.fastRotation().fastRotation()));
	}
	
	@Test
	public void testAbnormalities() {
		Piece ab1 = new Piece("0 1  1 0  2 1  3 0");
		assertEquals(4, ab1.getWidth());
		assertEquals(2, ab1.getHeight());
		assertTrue(Arrays.equals(new int[] {1, 0, 1, 0}, ab1.getSkirt()));
		
		Piece ab2 = new Piece("0 0  0 1  0 2  1 1  2 1  3 0  3 1  3 2");
		assertEquals(4, ab2.getWidth());
		assertEquals(3, ab2.getHeight());
		assertTrue(Arrays.equals(new int[] {0, 1, 1, 0}, ab2.getSkirt()));
		Piece ab22 = ab2.computeNextRotation();
		assertEquals(3, ab22.getWidth());
		assertEquals(4, ab22.getHeight());
		assertTrue(Arrays.equals(new int[] {0, 0, 0}, ab22.getSkirt()));
	}
	
	
}
