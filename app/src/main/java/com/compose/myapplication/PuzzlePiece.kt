package com.compose.myapplication

import android.graphics.Bitmap

/***
 * A class to hold the puzzle piece bitmap, as well as its coordinates and
 * number in the whole image.
 *
 * @author Rick
 */
class PuzzlePiece {
    @JvmField
    var px = 0
    @JvmField
    var py = 0
    @JvmField
    var px2 = 0
    @JvmField
    var py2 = 0
    @JvmField
    var bitmap: Bitmap? = null
    @JvmField
    var pieceNum = 0
}