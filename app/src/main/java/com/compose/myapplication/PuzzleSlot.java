package com.compose.myapplication;

/**
 * A class to hold the piece and coordinates of the puzzle piece frame as well
 * as its own slot number in the whole divided image.
 * 
 * @author Rick
 * 
 */
public class PuzzleSlot {
	public int sx, sy, sx2, sy2;
	public PuzzlePiece puzzlePiece = new PuzzlePiece();
	public int slotNum;
}