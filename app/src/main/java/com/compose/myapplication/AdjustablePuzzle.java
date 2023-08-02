package com.compose.myapplication;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;

import java.util.Date;


/**
 * The adjustable puzzle class scales the puzzle to different sizes in one class.
 * <p/>
 * Created by Richard on 11/7/2015.
 */
public class AdjustablePuzzle {

    // a result of the sides h x w
    int pieces, xparts, yparts;
    PuzzleSurface puzzleSurface;
    CommonVariables common = CommonVariables.getInstance();

    //tag used for logging
    private static final String TAG = "puzzleLog";

    //set the surface variable
    public AdjustablePuzzle(PuzzleSurface ps) {
        puzzleSurface = ps;
    }

    /**
     * Called when shared preferences are valid to resume a previous puzzle. Use a loading thread to set up the puzzle off the main thread.
     */
    public void getPreviousImageLoadedScaledDivided() {
        if (common.isLogging)
            Log.d(TAG, "getPreviousImageLoadedScaledDivided AdjustablePuzzleImpl");

        common.isPuzzleSplitCorrectly = false;
        common.isPuzzleSolved = false;

        Thread thread = new Thread() {
            @Override
            public void run() {
                while (!common.isPuzzleSplitCorrectly) {
                    // get new index value and then remove index
                    common.index = common.currentPuzzleImagePosition;

                    common.image = common.decodeSampledBitmapFromResource(
                            common.res,
                            common.data.artworks[common.currentPuzzleImagePosition].imageID,
                            common.screenW, common.screenH);

                    common.image = Bitmap.createScaledBitmap(common.image,
                            common.screenW, common.screenH, true);

                    common.isPuzzleSplitCorrectly = divideBitmapFromPreviousPuzzle();

                    if (common.isPuzzleSplitCorrectly) {
                        common.isImageError = false;
                        common.mySoundPool.playChimeSound();
                        common.isImageLoaded = true;
                        checkToBeSolved();
                        updateAndDraw();
                    } else {
                        common.isImageError = true;
                    }
                }
            }
        };
        thread.start();
    }

    /**
     * Called when shared preferences are invalid or a new puzzle is selected after the solve of one. Use a loading thread to set up the puzzle off the main thread.
     */
    public void getNewImageLoadedScaledDivided() {
        if (common.isLogging) Log.d(TAG, "getNewImageLoadedScaledDivided AdjustablePuzzleImpl");

        common.isPuzzleSplitCorrectly = false;
        common.isPuzzleSolved = false;

        Thread thread = new Thread() {
            @Override
            public void run() {

                while (!common.isPuzzleSplitCorrectly) {

                    // fill with all valid numbers
                    if (common.imagesShown.isEmpty())
                        for (int i = 0; i < common.data.artworks.length; i++)
                            common.imagesShown.add(i);

                    // get new index value from remaining images
                    common.index = common.rand.nextInt(common.imagesShown
                            .size());

                    //edit to change to a direct image
                    //common.index = 141;

                    // get the value at that index for new imageID
                    common.currentPuzzleImagePosition = common.imagesShown
                            .get(common.index);

                    // remove from list to prevent duplicates
                    common.imagesShown.remove(common.index);

                    // start decoding and scaling
                    common.image = common.decodeSampledBitmapFromResource(
                            common.res,
                            common.data.artworks[common.currentPuzzleImagePosition].imageID,
                            common.screenW, common.screenH);
                    common.image = Bitmap.createScaledBitmap(common.image,
                            common.screenW, common.screenH, true);

                    common.isPuzzleSplitCorrectly = divideBitmap();

                    if (common.isPuzzleSplitCorrectly) {
                        resetTimer();
                        common.isImageError = false;
                        common.mySoundPool.playChimeSound();
                        common.isImageLoaded = true;
                        checkToBeSolved();
                        updateAndDraw();
                    } else {
                        common.isImageError = true;
                    }
                }
            }
        };
        thread.start();
    }

    /**
     * Switch every index with a random index to make the puzzle random.
     */
    public void switchEveryIndexWithRandomIndex() {
        // for every slot index
        for (int i = 0; i < common.slotOrder.length; i++) {
            // get a new index
            int switchIndex = common.rand.nextInt(common.slotOrder.length);
            // save original index
            int tempValue = common.slotOrder[i];
            // set new index into it
            common.slotOrder[i] = common.slotOrder[switchIndex];
            // set original to the new index
            common.slotOrder[switchIndex] = tempValue;
        }
    }

    /**
     * Find the sides of the device and split the puzzle into sections evenly across each side.
     */
    public void assignXandYtoBorderPointIndex() {
        common.evenlySplit = false;

        while (!common.evenlySplit) {
            // get screen width and height to start splitting
            int w = 0;
            if (null != common.image) w = common.image.getWidth();
            int h = 0;
            if (common.image != null) h = common.image.getHeight();

            int pieceW = w / xparts;
            int pieceH = h / yparts;

            common.xs = new int[xparts];
            for (int i = 0; i < xparts; i++) {
                common.xs[i] = pieceW * i;
            }

            common.ys = new int[yparts];
            for (int i = 0; i < yparts; i++) {
                common.ys[i] = pieceH * i;
            }

            int acc = 0;
            for (int i = 0; i < common.ys.length; i++) {
                int tempy = common.ys[i];
                for (int j = 0; j < common.xs.length; j++) {
                    int tempx = common.xs[j];
                    setBorderPoint(acc, tempx, tempy);
                    setBitmapToPiece(acc, tempx, tempy, pieceW, pieceH);
                    setPointsToSlotAndPiece(acc, tempx, tempy, pieceW, pieceH);
                    acc++;
                }
            }
            common.evenlySplit = true;
        }
    }

    /**
     * Sets the next point in the array to track pieces and pixel border
     *
     * @param i index of the new point in the points array
     * @param x coordinate
     * @param y coordinate
     */
    private void setBorderPoint(int i, int x, int y) {
        // add point to array for x and y of each division intersection of
        // pieces
        Point newPoint = new Point(x, y);
        common.points[i] = newPoint;
    }

    /**
     * Used if the shared preferences are valid, the puzzle won't need to be divided again, simply assigned new points based on the previous puzzle.
     *
     * @return
     */
    public boolean divideBitmapFromPreviousPuzzle() {
        common.initPrevDivideBitmap(pieces);
        common.piecesComplete = 0;
        assignXandYtoBorderPointIndex();
        common.assignSlotOrder();

        return (common.piecesComplete == pieces);
    }

    /**
     * Divide the puzzle into separate pieces, if there is a piece that is put into its correct place by accident it will return false;
     *
     * @return
     */
    public boolean divideBitmap() {
        common.initDivideBitmap(pieces);
        common.piecesComplete = 0;
        assignXandYtoBorderPointIndex();

        boolean randomSlots;
        do {
            switchEveryIndexWithRandomIndex();
            randomSlots = common.assignSlotOrder();
        } while (!randomSlots);

        return (common.piecesComplete == pieces);
    }

    /**
     * For each piece that is set to a slot, set the new coordinates for the bitmap and update the slot number with the new piece number.
     *
     * @param i
     * @param x
     * @param y
     * @param bitmapW
     * @param bitmapH
     */
    private void setPointsToSlotAndPiece(int i, int x, int y, int bitmapW,
                                         int bitmapH) {
        common.puzzlePieces[i].px = x;
        common.puzzlePieces[i].px2 = x + bitmapW;

        common.puzzlePieces[i].py = y;
        common.puzzlePieces[i].py2 = y + bitmapH;

        common.puzzleSlots[i].sx = x;
        common.puzzleSlots[i].sx2 = x + bitmapW;

        common.puzzleSlots[i].sy = y;
        common.puzzleSlots[i].sy2 = y + bitmapH;

        common.puzzleSlots[i].puzzlePiece = common.puzzlePieces[i];
        common.puzzleSlots[i].slotNum = common.puzzleSlots[i].puzzlePiece.pieceNum = i;

        common.piecesComplete++;
    }

    /**
     * Recycle the old bitmap and set the new one to the new piece at the given index.
     *
     * @param i
     * @param x
     * @param y
     * @param bitmapW
     * @param bitmapH
     */
    private void setBitmapToPiece(int i, int x, int y, int bitmapW, int bitmapH) {
        if (common.puzzlePieces[i].bitmap != null) {
            common.puzzlePieces[i].bitmap.recycle();
        }

        common.puzzlePieces[i].bitmap = null;
        common.puzzlePieces[i].bitmap = Bitmap.createBitmap(common.image, x, y,
                bitmapW, bitmapH);
    }

    /**
     * Make a call to the update and draw method in the puzzle surface.
     */
    public void updateAndDraw() {
        puzzleSurface.puzzleUpdateAndDraw.updateAndDraw();
    }

    /**
     * The touch events should handle moving pieces, and knowing if the puzzle has just been solved.
     *
     * @param event
     * @return
     */
    public boolean onTouchEvent(MotionEvent event) {
        // find the piece that was pressed down onto
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int downX = (int) event.getX();
            int downY = (int) event.getY();

            if (downX > common.screenW || downX < 0)
                return false;
            if (downY > common.screenH || downY < 0)
                return false;

            common.movingPiece = false;

            // get x index
            int xIndex = 0;
            for (int i = 0; i < common.xs.length; i++) {
                if (downX >= common.xs[i]) {
                    xIndex = i;
                }
            }

            // get y index
            int yIndex = 0;
            for (int i = 0; i < common.ys.length; i++) {
                if (downY >= common.ys[i]) {
                    yIndex = i;
                }
            }

            //find the piece based on x and y matrix
            common.currSlotOnTouchDown = xIndex + (yparts * yIndex);

        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            // the moving piece has its own coordinates
            int moveX = (int) event.getX();
            int moveY = (int) event.getY();

            boolean invalidMovePosition = false;
            if (moveX > common.screenW || moveX < 0)
                invalidMovePosition = true;
            if (moveY > common.screenH || moveY < 0)
                invalidMovePosition = true;

            if (invalidMovePosition) {
                common.movingPiece = false;
                common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.px = common.puzzleSlots[common.currSlotOnTouchDown].sx;
                common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.py = common.puzzleSlots[common.currSlotOnTouchDown].sy;
                updateAndDraw();
                return false;
            }

            // get moving piece and center it on user touch point
            common.movingPiece = true;
            if (common.currSlotOnTouchDown >= 0
                    && common.currSlotOnTouchDown < pieces) {
                common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.px = moveX
                        - common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.bitmap
                        .getWidth() / 2;
                common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.py = moveY
                        - common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.bitmap
                        .getHeight() / 2;
            }

            updateAndDraw();

        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            //the up action means it may be time to switch pieces store the new up coords
            int upX = (int) event.getX();
            int upY = (int) event.getY();

            common.movingPiece = false;

            boolean invalidSetPosition = false;
            if (upX > common.screenW || upX < 0)
                invalidSetPosition = true;
            if (upY > common.screenH || upY < 0)
                invalidSetPosition = true;

            if (invalidSetPosition) {
                common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.px = common.puzzleSlots[common.currSlotOnTouchDown].sx;
                common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.py = common.puzzleSlots[common.currSlotOnTouchDown].sy;
                common.playSetSound();
                updateAndDraw();
                return false;
            } else {
                // get x index
                int xIndex = 0;
                for (int i = 0; i < common.xs.length; i++) {
                    if (upX >= common.xs[i]) {
                        xIndex = i;
                    }
                }

                // get y index
                int yIndex = 0;
                for (int i = 0; i < common.ys.length; i++) {
                    if (upY >= common.ys[i]) {
                        yIndex = i;
                    }
                }

                common.currSlotOnTouchUp = xIndex + (yparts * yIndex);

                // check for new location to not be the original before setting
                if (common.currSlotOnTouchDown != common.currSlotOnTouchUp) {
                    common.sendPieceToNewSlot(common.currSlotOnTouchDown,
                            common.currSlotOnTouchUp);
                } else {
                    // simply return the moving piece to its original x and y
                    common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.px = common.puzzleSlots[common.currSlotOnTouchDown].sx;
                    common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.py = common.puzzleSlots[common.currSlotOnTouchDown].sy;
                }

                common.playSetSound();

                // check for all images to by in place
                common.inPlace = 0;
                for (int i = 0; i < common.numberOfPieces; i++) {
                    if (common.puzzleSlots[i].slotNum == common.puzzleSlots[i].puzzlePiece.pieceNum) {
                        common.inPlace++;
                    }
                }

                // if all in place set as isPuzzleSolved
                if (common.inPlace == common.numberOfPieces) {
                    addTimeToTimer();
                    common.isPuzzleSolved = true;

                    //increment stats
                    common.puzzlesSolved++;
                    incrementPuzzleSolveByPuzzleSize();
                    compareRecordTime();

                    //notify stats fragment if alive
                    MainActivity main = (MainActivity) puzzleSurface.context;
                    main.updatePuzzleStats();

                    updateAndDraw();
                    return false;
                } else {
                    updateAndDraw();
                }
            }
        }
        return true;
    }

    /**
     * Used to increment the stats of solved puzzles
     */
    private void incrementPuzzleSolveByPuzzleSize() {
        switch (pieces) {
            case 4:
                common.fourPiecePuzzleSolvedCount++;
                break;
            case 9:
                common.ninePiecePuzzleSolvedCount++;
                break;
            case 16:
                common.sixteenPiecePuzzleSolvedCount++;
                break;
            case 25:
                common.twentyfivePiecePuzzleSolvedCount++;
                break;
            case 36:
                common.thirtysixPiecePuzzleSolvedCount++;
                break;
            case 49:
                common.fourtyninePiecePuzzleSolvedCount++;
                break;
        }
    }

    /**
     * Used to recycle all bitmaps used in pieces
     */
    public void recylceAll() {
        if (common.image != null)
            common.image.recycle();
        for (int i = 0; i < common.puzzlePieces.length; i++)
            if (common.puzzlePieces[i] != null)
                if (common.puzzlePieces[i].bitmap != null)
                    common.puzzlePieces[i].bitmap.recycle();
    }

    /**
     * Add time to the timer for the current puzzle.
     */
    public void addTimeToTimer() {
        if (!common.isPuzzleSolved) {
            common.stopPuzzle = new Date();
            common.currPuzzleTime += common.stopPuzzle.getTime()
                    - common.startPuzzle.getTime();
        }
    }

    /**
     * Check if the current record solve time has been beaten, then replace it if so.
     */
    public void compareRecordTime() {
        switch (pieces) {
            case 4:
                if (common.fourRecordSolveTime > common.currPuzzleTime || common.fourRecordSolveTime == 0) {
                    common.fourRecordSolveTime = common.currPuzzleTime;
                }
                break;
            case 9:
                if (common.nineRecordSolveTime > common.currPuzzleTime || common.nineRecordSolveTime == 0) {
                    common.nineRecordSolveTime = common.currPuzzleTime;
                }
                break;
            case 16:
                if (common.sixteenRecordSolveTime > common.currPuzzleTime || common.sixteenRecordSolveTime == 0) {
                    common.sixteenRecordSolveTime = common.currPuzzleTime;
                }
                break;
            case 25:
                if (common.twentyfiveRecordSolveTime > common.currPuzzleTime || common.twentyfiveRecordSolveTime == 0) {
                    common.twentyfiveRecordSolveTime = common.currPuzzleTime;
                }
                break;
            case 36:
                if (common.thirtysixRecordsSolveTime > common.currPuzzleTime || common.thirtysixRecordsSolveTime == 0) {
                    common.thirtysixRecordsSolveTime = common.currPuzzleTime;
                }
                break;
            case 49:
                if (common.fourtynineRecordsSolveTime > common.currPuzzleTime || common.fourtynineRecordsSolveTime == 0) {
                    common.fourtynineRecordsSolveTime = common.currPuzzleTime;
                }
                break;
        }
    }

    /**
     * Reset the current puzzle solve timer.
     */
    public void resetTimer() {
        common.currPuzzleTime = 0;
        common.startPuzzle = new Date();
    }

    /**
     * Get the current puzzle time so that it has second notation.
     *
     * @return
     */
    public double getSolveTime() {
        return common.currPuzzleTime / 1000.0;
    }

    /**
     * On pause of the application update the current running time.
     */
    public void pause() {
        addTimeToTimer();
    }

    /**
     * On change of puzzle size update the parts and overall pieces
     *
     * @param sides
     */
    public void initPieces(int sides) {
        xparts = sides;
        yparts = sides;
        pieces = sides * sides;
    }

    /**
     * If all pieces are in the correct slot the puzzle is solved.
     */
    public void checkToBeSolved() {
        common.inPlace = 0;
        for (int i = 0; i < common.numberOfPieces; i++) {
            if (common.puzzleSlots[i].slotNum == common.puzzleSlots[i].puzzlePiece.pieceNum) {
                common.inPlace++;
            }
        }

        if (common.inPlace == common.numberOfPieces) {
            common.isPuzzleSolved = true;
        }
    }
}
