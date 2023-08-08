package com.compose.myapplication

import android.graphics.Bitmap
import android.graphics.Point
import android.util.Log
import android.view.MotionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Random


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
    private var rand = Random()
    private lateinit var image: Bitmap
    private var TAG = ".AdjustablePuzzle"


    fun getPreviousImageLoadedScaledDivided() {
        if (CommonVariables.isLogging) Log.d(
            TAG,
            "getPreviousImageLoadedScaledDivided AdjustablePuzzleImpl"
        )

        val customScope = CoroutineScope(Dispatchers.Default)
        customScope.launch {
            // Your coroutine code

            CommonVariables.isPuzzleSplitCorrectly = false
            CommonVariables.isPuzzleSolved = false

            while (!CommonVariables.isPuzzleSplitCorrectly) {
                // get new index value and then remove index
                CommonVariables.index = CommonVariables.currentPuzzleImagePosition
                image = CommonVariables.decodeSampledBitmapFromResource(
                    puzzleSurface.context.resources,
                    CommonVariables.data.artworks[CommonVariables.currentPuzzleImagePosition].imageID,
                    CommonVariables.screenW, CommonVariables.screenH
                )
                image = Bitmap.createScaledBitmap(
                    image,
                    CommonVariables.screenW, CommonVariables.screenH, true
                )
                CommonVariables.isPuzzleSplitCorrectly = divideBitmapFromPreviousPuzzle()
                if (CommonVariables.isPuzzleSplitCorrectly) {
                    CommonVariables.isImageError = false
                    puzzleSurface.soundPool?.playChimeSound()
                    CommonVariables.isImageLoaded = true
                    checkToBeSolved()
                    updateAndDraw()
                } else {
                    CommonVariables.isImageError = true
                }
            }
        }
    }

    /**
     * Called when shared preferences are invalid or a new puzzle is selected after the solve of one. Use a loading thread to set up the puzzle off the main thread.
     */
    fun getNewImageLoadedScaledDivided() {
        if (CommonVariables.isLogging) Log.d(
            TAG,
            "getNewImageLoadedScaledDivided AdjustablePuzzleImpl"
        )

        CommonVariables.isPuzzleSplitCorrectly = false
        CommonVariables.isPuzzleSolved = false

        val customScope = CoroutineScope(Dispatchers.Default)
        customScope.launch {
            while (!CommonVariables.isPuzzleSplitCorrectly) {

                // fill with all valid numbers
                if (CommonVariables.imagesShown.isEmpty()) for (i in CommonVariables.data.artworks.indices) CommonVariables.imagesShown.add(
                    i
                )

                // get new index value from remaining images
                CommonVariables.index =
                    rand.nextInt(CommonVariables.imagesShown.size)

                //edit to change to a direct image
                //CommonVariables.index = 141;

                // get the value at that index for new imageID
                CommonVariables.currentPuzzleImagePosition =
                    CommonVariables.imagesShown[CommonVariables.index]

                // remove from list to prevent duplicates
                CommonVariables.imagesShown.removeAt(CommonVariables.index)

                // start decoding and scaling
                image = CommonVariables.decodeSampledBitmapFromResource(
                    puzzleSurface.context.resources,
                    CommonVariables.data.artworks[CommonVariables.currentPuzzleImagePosition].imageID,
                    CommonVariables.screenW, CommonVariables.screenH
                )
                image = Bitmap.createScaledBitmap(
                    image,
                    CommonVariables.screenW, CommonVariables.screenH, true
                )
                CommonVariables.isPuzzleSplitCorrectly = divideBitmap()
                if (CommonVariables.isPuzzleSplitCorrectly) {
                    resetTimer()
                    CommonVariables.isImageError = false
                    puzzleSurface.soundPool?.playChimeSound()
                    CommonVariables.isImageLoaded = true
                    checkToBeSolved()
                    updateAndDraw()
                } else {
                    CommonVariables.isImageError = true
                }
            }
        }
    }


    /**
     * Switch every index with a random index to make the puzzle random.
     */
    private fun switchEveryIndexWithRandomIndex() {

        // for every slot index
        for (i in CommonVariables.slotOrder.indices) {
            // get a new index
            val switchIndex = rand.nextInt(CommonVariables.slotOrder.size)
            // save original index
            val tempValue = CommonVariables.slotOrder[i]
            // set new index into it
            CommonVariables.slotOrder[i] = CommonVariables.slotOrder[switchIndex]
            // set original to the new index
            CommonVariables.slotOrder[switchIndex] = tempValue
        }
    }

    /**
     * Find the sides of the device and split the puzzle into sections evenly across each side.
     */
    private fun assignXandYtoBorderPointIndex() {
        // get screen width and height to start splitting
        val w: Int = image.width
        val h: Int = image.height
        val pieceW = w / xparts
        val pieceH = h / yparts
        CommonVariables.xs = IntArray(xparts)
        for (i in 0 until xparts) {
            CommonVariables.xs[i] = pieceW * i
        }
        CommonVariables.ys = IntArray(yparts)
        for (i in 0 until yparts) {
            CommonVariables.ys[i] = pieceH * i
        }
        var acc = 0
        for (i in CommonVariables.ys.indices) {
            val tempy = CommonVariables.ys[i]
            for (j in CommonVariables.xs.indices) {
                val tempx = CommonVariables.xs[j]
                setBorderPoint(acc, tempx, tempy)
                setBitmapToPiece(acc, tempx, tempy, pieceW, pieceH)
                setPointsToSlotAndPiece(acc, tempx, tempy, pieceW, pieceH)
                acc++
            }
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
        CommonVariables.points[i] = newPoint
    }

    /**
     * Used if the shared preferences are valid, the puzzle won't need to be divided again, simply assigned new points based on the previous puzzle.
     *
     * @return
     */
    private fun divideBitmapFromPreviousPuzzle(): Boolean {
        CommonVariables.initPrevDivideBitmap(pieces)
        CommonVariables.piecesComplete = 0
        assignXandYtoBorderPointIndex()
        CommonVariables.assignSlotOrder()
        return CommonVariables.piecesComplete == pieces
    }

    /**
     * Divide the puzzle into separate pieces, if there is a piece that is put into its correct place by accident it will return false;
     *
     * @return
     */
    private fun divideBitmap(): Boolean {
        CommonVariables.initDivideBitmap(pieces)
        CommonVariables.piecesComplete = 0
        assignXandYtoBorderPointIndex()
        var randomSlots: Boolean
        do {
            switchEveryIndexWithRandomIndex()
            randomSlots = CommonVariables.assignSlotOrder()
        } while (!randomSlots)
        return CommonVariables.piecesComplete == pieces
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
        CommonVariables.puzzlePieces[i]?.px = x
        CommonVariables.puzzlePieces[i]?.px2 = x + bitmapW
        CommonVariables.puzzlePieces[i]?.py = y
        CommonVariables.puzzlePieces[i]?.py2 = y + bitmapH
        CommonVariables.puzzleSlots[i]?.sx = x
        CommonVariables.puzzleSlots[i]?.sx2 = x + bitmapW
        CommonVariables.puzzleSlots[i]?.sy = y
        CommonVariables.puzzleSlots[i]?.sy2 = y + bitmapH
        CommonVariables.puzzleSlots[i]?.puzzlePiece = CommonVariables.puzzlePieces[i]!!
        CommonVariables.puzzleSlots[i]?.puzzlePiece?.pieceNum = i
        CommonVariables.puzzleSlots[i]?.slotNum =
            CommonVariables.puzzleSlots[i]?.puzzlePiece?.pieceNum!!
        CommonVariables.piecesComplete++
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
        if (CommonVariables.puzzlePieces[i]?.bitmap != null) {
            CommonVariables.puzzlePieces[i]?.bitmap!!.recycle()
        }
        CommonVariables.puzzlePieces[i]?.bitmap = null
        CommonVariables.puzzlePieces[i]?.bitmap = image.let {
            Bitmap.createBitmap(
                it, x, y,
                bitmapW, bitmapH
            )
        }
    }

    /**
     * Make a call to the update and draw method in the puzzle surface.
     */
    private fun updateAndDraw() {
        puzzleSurface.puzzleUpdateAndDraw.updateAndDraw()
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
            if (downX > CommonVariables.screenW || downX < 0) return false
            if (downY > CommonVariables.screenH || downY < 0) return false
            CommonVariables.movingPiece = false

            // get x index
            var xIndex = 0
            for (i in CommonVariables.xs.indices) {
                if (downX >= CommonVariables.xs[i]) {
                    xIndex = i
                }
            }

            // get y index
            var yIndex = 0
            for (i in CommonVariables.ys.indices) {
                if (downY >= CommonVariables.ys[i]) {
                    yIndex = i
                }
            }

            //find the piece based on x and y matrix
            CommonVariables.currSlotOnTouchDown = xIndex + yparts * yIndex
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            // the moving piece has its own coordinates
            val moveX = event.x.toInt()
            val moveY = event.y.toInt()
            var invalidMovePosition = false
            if (moveX > CommonVariables.screenW || moveX < 0) invalidMovePosition = true
            if (moveY > CommonVariables.screenH || moveY < 0) invalidMovePosition = true
            if (invalidMovePosition) {
                CommonVariables.movingPiece = false
                CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.puzzlePiece?.px =
                    CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.sx!!
                CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.puzzlePiece?.py =
                    CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.sy!!
                updateAndDraw()
                return false
            }

            // get moving piece and center it on user touch point
            CommonVariables.movingPiece = true
            if (CommonVariables.currSlotOnTouchDown in 0 until pieces) {
                CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.puzzlePiece?.px =
                    (moveX
                            - (CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.puzzlePiece?.bitmap
                        ?.width ?: 0) / 2)
                CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.puzzlePiece?.py =
                    (moveY
                            - (CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.puzzlePiece?.bitmap
                        ?.height ?: 0) / 2)
            }
            updateAndDraw()
        } else if (event.action == MotionEvent.ACTION_UP) {
            //the up action means it may be time to switch pieces store the new up coords
            val upX = event.x.toInt()
            val upY = event.y.toInt()
            CommonVariables.movingPiece = false
            var invalidSetPosition = false
            if (upX > CommonVariables.screenW || upX < 0) invalidSetPosition = true
            if (upY > CommonVariables.screenH || upY < 0) invalidSetPosition = true
            if (invalidSetPosition) {
                CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.puzzlePiece?.px =
                    CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.sx!!
                CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.puzzlePiece?.py =
                    CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.sy!!
                puzzleSurface.soundPool?.playSetSound()
                updateAndDraw()
                return false
            } else {
                // get x index
                var xIndex = 0
                for (i in CommonVariables.xs.indices) {
                    if (upX >= CommonVariables.xs[i]) {
                        xIndex = i
                    }
                }

                // get y index
                var yIndex = 0
                for (i in CommonVariables.ys.indices) {
                    if (upY >= CommonVariables.ys[i]) {
                        yIndex = i
                    }
                }
                CommonVariables.currSlotOnTouchUp = xIndex + yparts * yIndex

                // check for new location to not be the original before setting
                if (CommonVariables.currSlotOnTouchDown != CommonVariables.currSlotOnTouchUp) {
                    CommonVariables.sendPieceToNewSlot(
                        CommonVariables.currSlotOnTouchDown,
                        CommonVariables.currSlotOnTouchUp
                    )
                } else {
                    // simply return the moving piece to its original x and y
                    CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.puzzlePiece?.px =
                        CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.sx!!
                    CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.puzzlePiece?.py =
                        CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.sy!!
                }
                puzzleSurface.soundPool?.playSetSound()

                // check for all images to by in place
                var inPlace = 0
                for (i in 0 until CommonVariables.numberOfPieces) {
                    if (CommonVariables.puzzleSlots[i]?.slotNum == CommonVariables.puzzleSlots[i]?.puzzlePiece?.pieceNum) {
                        inPlace++
                    }
                }

                // if all in place set as isPuzzleSolved
                if (inPlace == CommonVariables.numberOfPieces) {
                    addTimeToTimer()
                    CommonVariables.isPuzzleSolved = true

                    //increment stats
                    CommonVariables.puzzlesSolved++
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
            4 -> CommonVariables.fourPiecePuzzleSolvedCount++
            9 -> CommonVariables.ninePiecePuzzleSolvedCount++
            16 -> CommonVariables.sixteenPiecePuzzleSolvedCount++
            25 -> CommonVariables.twentyfivePiecePuzzleSolvedCount++
            36 -> CommonVariables.thirtysixPiecePuzzleSolvedCount++
            49 -> CommonVariables.fourtyninePiecePuzzleSolvedCount++
        }
    }

    /**
     * Add time to the timer for the current puzzle.
     */
    private fun addTimeToTimer() {
        if (!CommonVariables.isPuzzleSolved) {
            CommonVariables.stopPuzzle = Date()
            CommonVariables.currPuzzleTime += (CommonVariables.stopPuzzle.time - CommonVariables.startPuzzle.time)
        }
    }

    /**
     * Check if the current record solve time has been beaten, then replace it if so.
     */
    private fun compareRecordTime() {
        when (pieces) {
            4 -> if (CommonVariables.fourRecordSolveTime > CommonVariables.currPuzzleTime || CommonVariables.fourRecordSolveTime == 0L) {
                CommonVariables.fourRecordSolveTime = CommonVariables.currPuzzleTime
            }

            9 -> if (CommonVariables.nineRecordSolveTime > CommonVariables.currPuzzleTime || CommonVariables.nineRecordSolveTime == 0L) {
                CommonVariables.nineRecordSolveTime = CommonVariables.currPuzzleTime
            }

            16 -> if (CommonVariables.sixteenRecordSolveTime > CommonVariables.currPuzzleTime || CommonVariables.sixteenRecordSolveTime == 0L) {
                CommonVariables.sixteenRecordSolveTime = CommonVariables.currPuzzleTime
            }

            25 -> if (CommonVariables.twentyfiveRecordSolveTime > CommonVariables.currPuzzleTime || CommonVariables.twentyfiveRecordSolveTime == 0L) {
                CommonVariables.twentyfiveRecordSolveTime = CommonVariables.currPuzzleTime
            }

            36 -> if (CommonVariables.thirtysixRecordsSolveTime > CommonVariables.currPuzzleTime || CommonVariables.thirtysixRecordsSolveTime == 0L) {
                CommonVariables.thirtysixRecordsSolveTime = CommonVariables.currPuzzleTime
            }

            49 -> if (CommonVariables.fourtynineRecordsSolveTime > CommonVariables.currPuzzleTime || CommonVariables.fourtynineRecordsSolveTime == 0L) {
                CommonVariables.fourtynineRecordsSolveTime = CommonVariables.currPuzzleTime
            }
        }
    }

    /**
     * Reset the current puzzle solve timer.
     */
    private fun resetTimer() {
        CommonVariables.currPuzzleTime = 0
        CommonVariables.startPuzzle = Date()
    }

    val solveTime: Double
        /**
         * Get the current puzzle time so that it has second notation.
         *
         * @return
         */
        get() = CommonVariables.currPuzzleTime / 1000.0

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
    private fun checkToBeSolved() {
        var inPlace = 0
        for (i in 0 until CommonVariables.numberOfPieces) {
            if (CommonVariables.puzzleSlots[i]?.slotNum == CommonVariables.puzzleSlots[i]?.puzzlePiece?.pieceNum) {
                inPlace++
            }
        }
        if (inPlace == CommonVariables.numberOfPieces) {
            CommonVariables.isPuzzleSolved = true
        }
    }
}