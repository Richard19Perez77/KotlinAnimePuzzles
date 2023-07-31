package com.compose.myapplication;

import android.graphics.Bitmap;


/***
 * A class to hold the puzzle piece bitmap, as well as its coordinates and
 * number in the whole image.
 *
 * @author Rick
 */
public class PuzzlePiece {
    public int px, py, px2, py2;
    public Bitmap bitmap;
    public int pieceNum;
}