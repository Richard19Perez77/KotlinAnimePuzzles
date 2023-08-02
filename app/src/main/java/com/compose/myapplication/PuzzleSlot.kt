package com.compose.myapplication

/**
 * A class to hold the piece and coordinates of the puzzle piece frame as well
 * as its own slot number in the whole divided image.
 *
 * @author Rick
 */
class PuzzleSlot {
    @JvmField
	var sx = 0
    @JvmField
	var sy = 0
    @JvmField
	var sx2 = 0
    @JvmField
	var sy2 = 0
    @JvmField
	var puzzlePiece = PuzzlePiece()
    @JvmField
	var slotNum = 0
}