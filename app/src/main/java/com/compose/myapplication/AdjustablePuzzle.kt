package com.compose.myapplication

import android.graphics.Bitmap
import android.graphics.Point
import android.util.Log
import android.view.MotionEvent
import java.util.Date

/**
 * The adjustable puzzle class scales the puzzle to different sizes in one class.
 *
 *
 * Created by Richard on 11/7/2015.
 */
class AdjustablePuzzle(private var puzzleSurface: PuzzleSurface) {

    // a result of the sides h x w
    private var pieces = 0
    private var xparts = 0
    private var yparts = 0
    var common: CommonVariables = CommonVariables.getInstance()

    fun getPreviousImageLoadedScaledDivided() {
        if (common.isLogging) Log.d(TAG, "getPreviousImageLoadedScaledDivided AdjustablePuzzleImpl")

        common.isPuzzleSplitCorrectly = false
        common.isPuzzleSolved = false
        val thread: Thread = object : Thread() {
            override fun run() {
                while (!common.isPuzzleSplitCorrectly) {
                    // get new index value and then remove index
                    common.index = common.currentPuzzleImagePosition
                    common.image = common.decodeSampledBitmapFromResource(
                        common.res,
                        common.data.artworks[common.currentPuzzleImagePosition].imageID,
                        common.screenW, common.screenH
                    )
                    common.image = Bitmap.createScaledBitmap(
                        common.image,
                        common.screenW, common.screenH, true
                    )
                    common.isPuzzleSplitCorrectly = divideBitmapFromPreviousPuzzle()
                    if (common.isPuzzleSplitCorrectly) {
                        common.isImageError = false
                        common.mySoundPool.playChimeSound()
                        common.isImageLoaded = true
                        checkToBeSolved()
                        updateAndDraw()
                    } else {
                        common.isImageError = true
                    }
                }
            }
        }
        thread.start()
    }

    /**
     * Called when shared preferences are invalid or a new puzzle is selected after the solve of one. Use a loading thread to set up the puzzle off the main thread.
     */
    fun getNewImageLoadedScaledDivided() {
        if (common.isLogging) Log.d(TAG, "getNewImageLoadedScaledDivided AdjustablePuzzleImpl")

        common.isPuzzleSplitCorrectly = false
        common.isPuzzleSolved = false

        val thread: Thread = object : Thread() {
            override fun run() {
                while (!common.isPuzzleSplitCorrectly) {

                    // fill with all valid numbers
                    if (common.imagesShown.isEmpty()) for (i in common.data.artworks.indices) common.imagesShown.add(
                        i
                    )

                    // get new index value from remaining images
                    common.index = common.rand.nextInt(common.imagesShown.size)

                    //edit to change to a direct image
                    //common.index = 141;

                    // get the value at that index for new imageID
                    common.currentPuzzleImagePosition = common.imagesShown[common.index]

                    // remove from list to prevent duplicates
                    common.imagesShown.removeAt(common.index)

                    // start decoding and scaling
                    common.image = common.decodeSampledBitmapFromResource(
                        common.res,
                        common.data.artworks[common.currentPuzzleImagePosition].imageID,
                        common.screenW, common.screenH
                    )
                    common.image = Bitmap.createScaledBitmap(
                        common.image,
                        common.screenW, common.screenH, true
                    )
                    common.isPuzzleSplitCorrectly = divideBitmap()
                    if (common.isPuzzleSplitCorrectly) {
                        resetTimer()
                        common.isImageError = false
                        common.mySoundPool.playChimeSound()
                        common.isImageLoaded = true
                        checkToBeSolved()
                        updateAndDraw()
                    } else {
                        common.isImageError = true
                    }
                }
            }
        }
        thread.start()
    }

    /**
     * Switch every index with a random index to make the puzzle random.
     */
    private fun switchEveryIndexWithRandomIndex() {

        // for every slot index
        for (i in common.slotOrder.indices) {
            // get a new index
            val switchIndex = common.rand.nextInt(common.slotOrder.size)
            // save original index
            val tempValue = common.slotOrder[i]
            // set new index into it
            common.slotOrder[i] = common.slotOrder[switchIndex]
            // set original to the new index
            common.slotOrder[switchIndex] = tempValue
        }
    }

    /**
     * Find the sides of the device and split the puzzle into sections evenly across each side.
     */
    private fun assignXandYtoBorderPointIndex() {
        common.evenlySplit = false
        while (!common.evenlySplit) {
            // get screen width and height to start splitting
            var w = 0
            if (null != common.image) w = common.image.width
            var h = 0
            if (common.image != null) h = common.image.height
            val pieceW = w / xparts
            val pieceH = h / yparts
            common.xs = IntArray(xparts)
            for (i in 0 until xparts) {
                common.xs[i] = pieceW * i
            }
            common.ys = IntArray(yparts)
            for (i in 0 until yparts) {
                common.ys[i] = pieceH * i
            }
            var acc = 0
            for (i in common.ys.indices) {
                val tempy = common.ys[i]
                for (j in common.xs.indices) {
                    val tempx = common.xs[j]
                    setBorderPoint(acc, tempx, tempy)
                    setBitmapToPiece(acc, tempx, tempy, pieceW, pieceH)
                    setPointsToSlotAndPiece(acc, tempx, tempy, pieceW, pieceH)
                    acc++
                }
            }
            common.evenlySplit = true
        }
    }

    /**
     * Sets the next point in the array to track pieces and pixel border
     *
     * @param i index of the new point in the points array
     * @param x coordinate
     * @param y coordinate
     */
    private fun setBorderPoint(i: Int, x: Int, y: Int) {
        // add point to array for x and y of each division intersection of pieces
        val newPoint = Point(x, y)
        common.points[i] = newPoint
    }

    /**
     * Used if the shared preferences are valid, the puzzle won't need to be divided again, simply assigned new points based on the previous puzzle.
     *
     * @return
     */
    fun divideBitmapFromPreviousPuzzle(): Boolean {
        common.initPrevDivideBitmap(pieces)
        common.piecesComplete = 0
        assignXandYtoBorderPointIndex()
        common.assignSlotOrder()
        return common.piecesComplete == pieces
    }

    /**
     * Divide the puzzle into separate pieces, if there is a piece that is put into its correct place by accident it will return false;
     *
     * @return
     */
    fun divideBitmap(): Boolean {
        common.initDivideBitmap(pieces)
        common.piecesComplete = 0
        assignXandYtoBorderPointIndex()
        var randomSlots: Boolean
        do {
            switchEveryIndexWithRandomIndex()
            randomSlots = common.assignSlotOrder()
        } while (!randomSlots)
        return common.piecesComplete == pieces
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
    private fun setPointsToSlotAndPiece(i: Int, x: Int, y: Int, bitmapW: Int, bitmapH: Int) {
        common.puzzlePieces[i].px = x
        common.puzzlePieces[i].px2 = x + bitmapW
        common.puzzlePieces[i].py = y
        common.puzzlePieces[i].py2 = y + bitmapH
        common.puzzleSlots[i].sx = x
        common.puzzleSlots[i].sx2 = x + bitmapW
        common.puzzleSlots[i].sy = y
        common.puzzleSlots[i].sy2 = y + bitmapH
        common.puzzleSlots[i].puzzlePiece = common.puzzlePieces[i]
        common.puzzleSlots[i].puzzlePiece.pieceNum = i
        common.puzzleSlots[i].slotNum = common.puzzleSlots[i].puzzlePiece.pieceNum
        common.piecesComplete++
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
    private fun setBitmapToPiece(i: Int, x: Int, y: Int, bitmapW: Int, bitmapH: Int) {
        if (common.puzzlePieces[i].bitmap != null) {
            common.puzzlePieces[i].bitmap!!.recycle()
        }
        common.puzzlePieces[i].bitmap = null
        common.puzzlePieces[i].bitmap = Bitmap.createBitmap(
            common.image, x, y,
            bitmapW, bitmapH
        )
    }

    /**
     * Make a call to the update and draw method in the puzzle surface.
     */
    fun updateAndDraw() {
        puzzleSurface.puzzleUpdateAndDraw?.updateAndDraw()
    }

    /**
     * The touch events should handle moving pieces, and knowing if the puzzle has just been solved.
     *
     * @param event
     * @return
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        // find the piece that was pressed down onto
        if (event.action == MotionEvent.ACTION_DOWN) {
            val downX = event.x.toInt()
            val downY = event.y.toInt()
            if (downX > common.screenW || downX < 0) return false
            if (downY > common.screenH || downY < 0) return false
            common.movingPiece = false

            // get x index
            var xIndex = 0
            for (i in common.xs.indices) {
                if (downX >= common.xs[i]) {
                    xIndex = i
                }
            }

            // get y index
            var yIndex = 0
            for (i in common.ys.indices) {
                if (downY >= common.ys[i]) {
                    yIndex = i
                }
            }

            //find the piece based on x and y matrix
            common.currSlotOnTouchDown = xIndex + yparts * yIndex
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            // the moving piece has its own coordinates
            val moveX = event.x.toInt()
            val moveY = event.y.toInt()
            var invalidMovePosition = false
            if (moveX > common.screenW || moveX < 0) invalidMovePosition = true
            if (moveY > common.screenH || moveY < 0) invalidMovePosition = true
            if (invalidMovePosition) {
                common.movingPiece = false
                common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.px =
                    common.puzzleSlots[common.currSlotOnTouchDown].sx
                common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.py =
                    common.puzzleSlots[common.currSlotOnTouchDown].sy
                updateAndDraw()
                return false
            }

            // get moving piece and center it on user touch point
            common.movingPiece = true
            if (common.currSlotOnTouchDown in 0 until pieces) {
                common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.px = (moveX
                        - (common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.bitmap
                    ?.width ?: 0) / 2)
                common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.py = (moveY
                        - (common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.bitmap
                    ?.height ?: 0) / 2)
            }
            updateAndDraw()
        } else if (event.action == MotionEvent.ACTION_UP) {
            //the up action means it may be time to switch pieces store the new up coords
            val upX = event.x.toInt()
            val upY = event.y.toInt()
            common.movingPiece = false
            var invalidSetPosition = false
            if (upX > common.screenW || upX < 0) invalidSetPosition = true
            if (upY > common.screenH || upY < 0) invalidSetPosition = true
            if (invalidSetPosition) {
                common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.px =
                    common.puzzleSlots[common.currSlotOnTouchDown].sx
                common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.py =
                    common.puzzleSlots[common.currSlotOnTouchDown].sy
                common.playSetSound()
                updateAndDraw()
                return false
            } else {
                // get x index
                var xIndex = 0
                for (i in common.xs.indices) {
                    if (upX >= common.xs[i]) {
                        xIndex = i
                    }
                }

                // get y index
                var yIndex = 0
                for (i in common.ys.indices) {
                    if (upY >= common.ys[i]) {
                        yIndex = i
                    }
                }
                common.currSlotOnTouchUp = xIndex + yparts * yIndex

                // check for new location to not be the original before setting
                if (common.currSlotOnTouchDown != common.currSlotOnTouchUp) {
                    common.sendPieceToNewSlot(
                        common.currSlotOnTouchDown,
                        common.currSlotOnTouchUp
                    )
                } else {
                    // simply return the moving piece to its original x and y
                    common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.px =
                        common.puzzleSlots[common.currSlotOnTouchDown].sx
                    common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.py =
                        common.puzzleSlots[common.currSlotOnTouchDown].sy
                }
                common.playSetSound()

                // check for all images to by in place
                common.inPlace = 0
                for (i in 0 until common.numberOfPieces) {
                    if (common.puzzleSlots[i].slotNum == common.puzzleSlots[i].puzzlePiece.pieceNum) {
                        common.inPlace++
                    }
                }

                // if all in place set as isPuzzleSolved
                if (common.inPlace == common.numberOfPieces) {
                    addTimeToTimer()
                    common.isPuzzleSolved = true

                    //increment stats
                    common.puzzlesSolved++
                    incrementPuzzleSolveByPuzzleSize()
                    compareRecordTime()

                    //notify stats fragment if alive
                    val main = puzzleSurface.context as MainActivity
                    main.updatePuzzleStats()
                    updateAndDraw()
                    return false
                } else {
                    updateAndDraw()
                }
            }
        }
        return true
    }

    /**
     * Used to increment the stats of solved puzzles
     */
    private fun incrementPuzzleSolveByPuzzleSize() {
        when (pieces) {
            4 -> common.fourPiecePuzzleSolvedCount++
            9 -> common.ninePiecePuzzleSolvedCount++
            16 -> common.sixteenPiecePuzzleSolvedCount++
            25 -> common.twentyfivePiecePuzzleSolvedCount++
            36 -> common.thirtysixPiecePuzzleSolvedCount++
            49 -> common.fourtyninePiecePuzzleSolvedCount++
        }
    }

    /**
     * Used to recycle all bitmaps used in pieces
     */
    fun recylceAll() {
        if (common.image != null) common.image.recycle()
        for (i in common.puzzlePieces.indices) {
            if (common.puzzlePieces[i] != null) {
                if (common.puzzlePieces[i].bitmap != null) {
                    common.puzzlePieces[i].bitmap!!.recycle()
                }
            }
        }
    }

    /**
     * Add time to the timer for the current puzzle.
     */
    private fun addTimeToTimer() {
        if (!common.isPuzzleSolved) {
            common.stopPuzzle = Date()
            common.currPuzzleTime += (common.stopPuzzle.time - common.startPuzzle.time)
        }
    }

    /**
     * Check if the current record solve time has been beaten, then replace it if so.
     */
    private fun compareRecordTime() {
        when (pieces) {
            4 -> if (common.fourRecordSolveTime > common.currPuzzleTime || common.fourRecordSolveTime == 0L) {
                common.fourRecordSolveTime = common.currPuzzleTime
            }

            9 -> if (common.nineRecordSolveTime > common.currPuzzleTime || common.nineRecordSolveTime == 0L) {
                common.nineRecordSolveTime = common.currPuzzleTime
            }

            16 -> if (common.sixteenRecordSolveTime > common.currPuzzleTime || common.sixteenRecordSolveTime == 0L) {
                common.sixteenRecordSolveTime = common.currPuzzleTime
            }

            25 -> if (common.twentyfiveRecordSolveTime > common.currPuzzleTime || common.twentyfiveRecordSolveTime == 0L) {
                common.twentyfiveRecordSolveTime = common.currPuzzleTime
            }

            36 -> if (common.thirtysixRecordsSolveTime > common.currPuzzleTime || common.thirtysixRecordsSolveTime == 0L) {
                common.thirtysixRecordsSolveTime = common.currPuzzleTime
            }

            49 -> if (common.fourtynineRecordsSolveTime > common.currPuzzleTime || common.fourtynineRecordsSolveTime == 0L) {
                common.fourtynineRecordsSolveTime = common.currPuzzleTime
            }
        }
    }

    /**
     * Reset the current puzzle solve timer.
     */
    fun resetTimer() {
        common.currPuzzleTime = 0
        common.startPuzzle = Date()
    }

    val solveTime: Double
        /**
         * Get the current puzzle time so that it has second notation.
         *
         * @return
         */
        get() = common.currPuzzleTime / 1000.0

    /**
     * On pause of the application update the current running time.
     */
    fun pause() {
        addTimeToTimer()
    }

    /**
     * On change of puzzle size update the parts and overall pieces
     *
     * @param sides
     */
    fun initPieces(sides: Int) {
        xparts = sides
        yparts = sides
        pieces = sides * sides
    }

    /**
     * If all pieces are in the correct slot the puzzle is solved.
     */
    fun checkToBeSolved() {
        common.inPlace = 0
        for (i in 0 until common.numberOfPieces) {
            if (common.puzzleSlots[i].slotNum == common.puzzleSlots[i].puzzlePiece.pieceNum) {
                common.inPlace++
            }
        }
        if (common.inPlace == common.numberOfPieces) {
            common.isPuzzleSolved = true
        }
    }

    companion object {
        //tag used for logging
        private const val TAG = "puzzleLog"
    }
}