package com.compose.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;


/**
 * A class to hold variables that will be used across classes and the more
 * commonly used Context object.
 *
 * @author Rick
 */
public class CommonVariables {

    // /Setup up for commonly used variables that can be accessed when the class
    // is called
    private volatile static CommonVariables instance;

    public Data data = new Data();
    public int imagesSaved = 0;
    public int blogLinksTraversed = 0;
    public boolean isWindowInFocus = false;
    public int musicSaved = 0;
    public int currentSoundPosition, tapSound, saveSound, numberOfPieces,
            currSlotOnTouchDown, currSlotOnTouchUp, inPlace, screenH, screenW,
            currentPuzzleImagePosition;
    public boolean chimeLoaded, evenlySplit, tapLoaded, movingPiece,
            resumePreviousPuzzle,
            playChimeSound = true, drawBorders = true, playTapSound = true,
            playMusic = true;

    //count variables for the different size puzzles
    public int fourPiecePuzzleSolvedCount = 0, ninePiecePuzzleSolvedCount = 0, sixteenPiecePuzzleSolvedCount = 0, twentyfivePiecePuzzleSolvedCount = 0, thirtysixPiecePuzzleSolvedCount = 0, fourtyninePiecePuzzleSolvedCount = 0;

    // the value is the piece to go into it
    public int[] slotOrder;
    public ArrayList<Integer> imagesShown = new ArrayList<>();
    public Random rand = new Random();
    public Resources res;
    public Bitmap image;
    public PuzzlePiece[] puzzlePieces;
    public PuzzleSlot[] puzzleSlots;
    public Button mNextButton;
    public float volume;
    public MySoundPool mySoundPool;
    public double dimensions;
    public Point[] points;
    public int[] ys, xs;
    public int puzzlesSolved = 0;
    public boolean isLogging = false;
    public ImageButton devartButton, wordpressLinkButton;
    private static final String TAG = "puzzleLog";
    public boolean isImageLoaded;
    public boolean isPuzzleSplitCorrectly;
    public boolean isPuzzleSolved;
    public int index;
    public int piecesComplete;
    public boolean isImageError;
    public Date startPuzzle = new Date();
    public Date stopPuzzle = new Date();
    public long currPuzzleTime = 0;
    public long fourRecordSolveTime = 0;
    public boolean createNewPuzzle;
    public long nineRecordSolveTime = 0;
    public long sixteenRecordSolveTime = 0;
    public long twentyfiveRecordSolveTime = 0;
    public long thirtysixRecordsSolveTime = 0;
    public long fourtynineRecordsSolveTime = 0;
    public TextView textViewSolve;
    public CoordinatorLayout coordinatorLayout;
    public ImageButton saveMusicButton;
    public ImageButton saveImageButton;

    public static CommonVariables getInstance() {
        if (instance == null)
            synchronized (CommonVariables.class) {
                if (instance == null)
                    instance = new CommonVariables();
            }
        return instance;
    }

    private CommonVariables() {

    }

    /**
     * Sets creates a slot order out of a string of slots for resuming the puzzle.
     *
     * @param string
     * @return
     */
    public boolean setSlots(String string) {
        String[] stringSlots;
        stringSlots = string.split(",");
        slotOrder = new int[stringSlots.length];

        for (int i = 0; i < stringSlots.length; i++) {
            try {
                slotOrder[i] = Integer.parseInt(stringSlots[i]);
            } catch (NumberFormatException nfe) {
                return false;
            }
        }
        return true;
    }

    /**
     * Using the slot string assign a new slot order.
     *
     * @return
     */
    public boolean assignSlotOrder() {
        PuzzleSlot[] newSlots = new PuzzleSlot[numberOfPieces];
        for (int i = 0; i < numberOfPieces; i++) {
            newSlots[i] = new PuzzleSlot();
            newSlots[i].sx = puzzleSlots[i].sx;
            newSlots[i].sy = puzzleSlots[i].sy;
            newSlots[i].sx2 = puzzleSlots[i].sx2;
            newSlots[i].sy2 = puzzleSlots[i].sy2;
            newSlots[i].slotNum = puzzleSlots[i].slotNum;
            newSlots[i].puzzlePiece = puzzleSlots[slotOrder[i]].puzzlePiece;
            newSlots[i].puzzlePiece.px = newSlots[i].sx;
            newSlots[i].puzzlePiece.py = newSlots[i].sy;
        }

        for (PuzzleSlot newSlot : newSlots) {
            if (newSlot.slotNum != newSlot.puzzlePiece.pieceNum) {
                puzzleSlots = newSlots;
                return true;
            }
        }

        return false;
    }

    /**
     * Android provided method for scaling large images.
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public int calculateInSampleSize(BitmapFactory.Options options,
                                     int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee a final image with both dimensions larger than or equal
            // to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /**
     * Android provided method for scaling large images.
     *
     * @param res
     * @param resId
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                  int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * A method to switch a piece in the puzzle.
     */
    public void sendPieceToNewSlot(int a, int z) {
        PuzzlePiece temp;
        temp = puzzleSlots[currSlotOnTouchDown].puzzlePiece;
        puzzleSlots[a].puzzlePiece = puzzleSlots[z].puzzlePiece;
        puzzleSlots[a].puzzlePiece.px = puzzleSlots[a].sx;
        puzzleSlots[a].puzzlePiece.py = puzzleSlots[a].sy;
        puzzleSlots[z].puzzlePiece = temp;
        puzzleSlots[z].puzzlePiece.px = puzzleSlots[z].sx;
        puzzleSlots[z].puzzlePiece.py = puzzleSlots[z].sy;
    }

    /**
     * Play sound for setting puzzle pieces
     */
    public void playSetSound() {
        // mySoundPool.playSetSound();
    }

    /**
     * Create new array of slots and array of pieces of the puzzle.
     *
     * @param pieces
     */
    public void initPrevDivideBitmap(int pieces) {
        numberOfPieces = pieces;
        points = new Point[pieces];

        puzzlePieces = new PuzzlePiece[pieces];
        for (int i = 0; i < numberOfPieces; i++) {
            puzzlePieces[i] = new PuzzlePiece();
        }

        puzzleSlots = new PuzzleSlot[pieces];
        for (int i = 0; i < numberOfPieces; i++) {
            puzzleSlots[i] = new PuzzleSlot();
        }
    }

    /**
     * Creating a new puzzle means to create new slots, pieces and the numbering of the slot array.
     *
     * @param pieces
     */
    public void initDivideBitmap(int pieces) {
        numberOfPieces = pieces;
        points = new Point[pieces];

        // setup puzzle pieces with new pieces
        puzzlePieces = new PuzzlePiece[pieces];
        for (int i = 0; i < numberOfPieces; i++) {
            puzzlePieces[i] = new PuzzlePiece();
        }

        // setup for new slots for the pieces
        puzzleSlots = new PuzzleSlot[pieces];
        for (int i = 0; i < numberOfPieces; i++) {
            puzzleSlots[i] = new PuzzleSlot();
        }

        // default order for slots with perfect order
        slotOrder = new int[pieces];
        for (int i = 0; i < pieces; i++) {
            slotOrder[i] = i;
        }
    }

    /**
     * Run a thread on the UI to hide the UI views.
     */
    public void hideButtons(Context context) {
        if (isLogging) Log.d(TAG, "hideButtons CommonVariables");

        Activity act = (Activity) context;
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (mNextButton != null
                        && mNextButton.getVisibility() == View.VISIBLE) {
                    mNextButton.setVisibility(View.INVISIBLE);
                }
                if (devartButton != null
                        && devartButton.getVisibility() == View.VISIBLE) {
                    devartButton.setVisibility(View.INVISIBLE);
                }
                if (wordpressLinkButton != null
                        && wordpressLinkButton.getVisibility() == View.VISIBLE) {
                    wordpressLinkButton.setVisibility(View.INVISIBLE);
                }
                if (saveImageButton != null
                        && saveImageButton.getVisibility() == View.VISIBLE) {
                    saveImageButton.setVisibility(View.INVISIBLE);
                }
                if (saveMusicButton != null
                        && saveMusicButton.getVisibility() == View.VISIBLE) {
                    saveMusicButton.setVisibility(View.INVISIBLE);
                }
                if (textViewSolve != null
                        && textViewSolve.getVisibility() == View.VISIBLE) {
                    textViewSolve.setVisibility(View.INVISIBLE);
                }
            }
        });
    }


    /**
     * Run a thread on the UI to toggle the visibility of the UI views.
     */
    public void toggleUIOverlay(Context context) {
        if (isLogging)
            Log.d(TAG, "toggleUIOverlay CommonVariables");

        Activity act = (Activity) context;
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (mNextButton != null)
                    if (mNextButton.getVisibility() == View.VISIBLE)
                        mNextButton.setVisibility(View.INVISIBLE);
                    else {
                        mNextButton.setVisibility(View.VISIBLE);
                        mNextButton.bringToFront();
                    }

                if (devartButton != null)
                    if (devartButton.getVisibility() == View.VISIBLE)
                        devartButton
                                .setVisibility(View.INVISIBLE);
                    else {
                        devartButton
                                .setVisibility(View.VISIBLE);
                        devartButton.bringToFront();
                    }

                if (wordpressLinkButton != null)
                    if (wordpressLinkButton
                            .getVisibility() == View.VISIBLE)
                        wordpressLinkButton
                                .setVisibility(View.INVISIBLE);
                    else {
                        wordpressLinkButton
                                .setVisibility(View.VISIBLE);
                        wordpressLinkButton.bringToFront();
                    }

                if (saveMusicButton != null)
                    if (saveMusicButton
                            .getVisibility() == View.VISIBLE)
                        saveMusicButton
                                .setVisibility(View.INVISIBLE);
                    else {
                        saveMusicButton
                                .setVisibility(View.VISIBLE);
                        saveMusicButton.bringToFront();
                    }

                if (saveImageButton != null)
                    if (saveImageButton
                            .getVisibility() == View.VISIBLE)
                        saveImageButton
                                .setVisibility(View.INVISIBLE);
                    else {
                        saveImageButton
                                .setVisibility(View.VISIBLE);
                        saveImageButton.bringToFront();
                    }

                if (textViewSolve != null)
                    if (textViewSolve
                            .getVisibility() == View.VISIBLE)
                        textViewSolve
                                .setVisibility(View.INVISIBLE);
                    else {
                        textViewSolve
                                .setVisibility(View.VISIBLE);
                        textViewSolve.bringToFront();
                    }
            }
        });
    }


    /**
     * Run a thread on the UI to show the UI views.
     */
    public void showButtons(Context context) {
        if (isLogging) Log.d(TAG, "showButtons CommonVariables");

        Activity act = (Activity) context;
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (mNextButton != null
                        && mNextButton.getVisibility() == View.INVISIBLE) {
                    mNextButton.setVisibility(View.VISIBLE);
                    mNextButton.bringToFront();
                }

                if (devartButton != null
                        && devartButton.getVisibility() == View.INVISIBLE) {
                    devartButton.setVisibility(View.VISIBLE);
                    devartButton.bringToFront();
                }

                if (wordpressLinkButton != null
                        && wordpressLinkButton.getVisibility() == View.INVISIBLE) {
                    wordpressLinkButton.setVisibility(View.VISIBLE);
                    wordpressLinkButton.bringToFront();
                }

                if (saveImageButton != null
                        && saveImageButton.getVisibility() == View.INVISIBLE) {
                    saveImageButton.setVisibility(View.VISIBLE);
                    saveImageButton.bringToFront();
                }

                if (saveMusicButton != null
                        && saveMusicButton.getVisibility() == View.INVISIBLE) {
                    saveMusicButton.setVisibility(View.VISIBLE);
                    saveMusicButton.bringToFront();
                }
            }
        });
    }
}