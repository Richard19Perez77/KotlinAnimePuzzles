package com.compose.myapplication

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import java.util.Date
import java.util.Random

/**
 * A class to hold variables that will be used across classes and the more
 * commonly used Context object.
 *
 * @author Rick
 */
object CommonVariables {

    private const val TAG = "puzzleLog"

    @JvmField
    var data = Data()
    @JvmField
    var imagesSaved = 0
    var blogLinksTraversed = 0
    var isWindowInFocus = false
    @JvmField
    var musicSaved = 0
    var currentSoundPosition = 0
    var tapSound = 0
    var saveSound = 0
    var numberOfPieces = 0
    var currSlotOnTouchDown = 0
    var currSlotOnTouchUp = 0
    var inPlace = 0
    var screenH = 0
    var screenW = 0
    var currentPuzzleImagePosition = 0
    var chimeLoaded = false
    var evenlySplit = false
    var tapLoaded = false
    var movingPiece = false
    var resumePreviousPuzzle = false
    var playChimeSound = true
    var drawBorders = true
    var playTapSound = true
    var playMusic = true

    //count variables for the different size puzzles
    var fourPiecePuzzleSolvedCount = 0
    var ninePiecePuzzleSolvedCount = 0
    var sixteenPiecePuzzleSolvedCount = 0
    var twentyfivePiecePuzzleSolvedCount = 0
    var thirtysixPiecePuzzleSolvedCount = 0
    var fourtyninePiecePuzzleSolvedCount = 0

    // the value is the piece to go into it
    lateinit var slotOrder: IntArray
    var imagesShown = ArrayList<Int>()
    var rand = Random()
    var res: Resources? = null
    var image: Bitmap? = null
    lateinit var puzzlePieces: Array<PuzzlePiece?>
    lateinit var puzzleSlots: Array<PuzzleSlot?>
    var mySoundPool: MySoundPool? = null
    var dimensions = 0.0
    lateinit var points: Array<Point?>
    lateinit var ys: IntArray
    lateinit var xs: IntArray
    var puzzlesSolved = 0
    var isLogging = false
    var isImageLoaded = false
    var isPuzzleSplitCorrectly = false
    var isPuzzleSolved = false
    var index = 0
    var piecesComplete = 0
    var isImageError = false
    var startPuzzle = Date()
    var stopPuzzle = Date()
    var currPuzzleTime: Long = 0
    var fourRecordSolveTime: Long = 0
    var nineRecordSolveTime: Long = 0
    var sixteenRecordSolveTime: Long = 0
    var twentyfiveRecordSolveTime: Long = 0
    var thirtysixRecordsSolveTime: Long = 0
    var fourtynineRecordsSolveTime: Long = 0

    /**
     * Sets creates a slot order out of a string of slots for resuming the puzzle.
     *
     * @param string
     * @return
     */
    fun setSlots(string: String): Boolean {
        val stringSlots: Array<String> = string.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        slotOrder = IntArray(stringSlots.size)
        for (i in stringSlots.indices) {
            try {
                slotOrder[i] = stringSlots[i].toInt()
            } catch (nfe: NumberFormatException) {
                return false
            }
        }
        return true
    }

    /**
     * Using the slot string assign a new slot order.
     *
     * @return
     */
    fun assignSlotOrder(): Boolean {
        val newSlots = arrayOfNulls<PuzzleSlot>(numberOfPieces)
        for (i in 0 until numberOfPieces) {
            newSlots[i] = PuzzleSlot()
            newSlots[i]!!.sx = puzzleSlots[i]!!.sx
            newSlots[i]!!.sy = puzzleSlots[i]!!.sy
            newSlots[i]!!.sx2 = puzzleSlots[i]!!.sx2
            newSlots[i]!!.sy2 = puzzleSlots[i]!!.sy2
            newSlots[i]!!.slotNum = puzzleSlots[i]!!.slotNum
            newSlots[i]!!.puzzlePiece = puzzleSlots[slotOrder[i]]!!.puzzlePiece
            newSlots[i]!!.puzzlePiece.px = newSlots[i]!!.sx
            newSlots[i]!!.puzzlePiece.py = newSlots[i]!!.sy
        }
        for (newSlot in newSlots) {
            if (newSlot!!.slotNum != newSlot.puzzlePiece.pieceNum) {
                puzzleSlots = newSlots
                return true
            }
        }
        return false
    }

    /**
     * Android provided method for scaling large images.
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int, reqHeight: Int
    ): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee a final image with both dimensions larger than or equal
            // to the requested height and width.
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
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
    fun decodeSampledBitmapFromResource(
        res: Resources?, resId: Int,
        reqWidth: Int, reqHeight: Int
    ): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(res, resId, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(
            options, reqWidth,
            reqHeight
        )

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeResource(res, resId, options)
    }

    /**
     * A method to switch a piece in the puzzle.
     */
    fun sendPieceToNewSlot(a: Int, z: Int) {
        val temp: PuzzlePiece
        temp = puzzleSlots[currSlotOnTouchDown]!!.puzzlePiece
        puzzleSlots[a]!!.puzzlePiece = puzzleSlots[z]!!.puzzlePiece
        puzzleSlots[a]!!.puzzlePiece.px = puzzleSlots[a]!!.sx
        puzzleSlots[a]!!.puzzlePiece.py = puzzleSlots[a]!!.sy
        puzzleSlots[z]!!.puzzlePiece = temp
        puzzleSlots[z]!!.puzzlePiece.px = puzzleSlots[z]!!.sx
        puzzleSlots[z]!!.puzzlePiece.py = puzzleSlots[z]!!.sy
    }

    /**
     * Play sound for setting puzzle pieces
     */
    fun playSetSound() {
        mySoundPool!!.playSetSound()
    }

    /**
     * Create new array of slots and array of pieces of the puzzle.
     *
     * @param pieces
     */
    fun initPrevDivideBitmap(pieces: Int) {
        numberOfPieces = pieces
        points = arrayOfNulls(pieces)
        puzzlePieces = arrayOfNulls(pieces)
        for (i in 0 until numberOfPieces) {
            puzzlePieces[i] = PuzzlePiece()
        }
        puzzleSlots = arrayOfNulls(pieces)
        for (i in 0 until numberOfPieces) {
            puzzleSlots[i] = PuzzleSlot()
        }
    }

    /**
     * Creating a new puzzle means to create new slots, pieces and the numbering of the slot array.
     *
     * @param pieces
     */
    fun initDivideBitmap(pieces: Int) {
        numberOfPieces = pieces
        points = arrayOfNulls(pieces)

        // setup puzzle pieces with new pieces
        puzzlePieces = arrayOfNulls(pieces)
        for (i in 0 until numberOfPieces) {
            puzzlePieces[i] = PuzzlePiece()
        }

        // setup for new slots for the pieces
        puzzleSlots = arrayOfNulls(pieces)
        for (i in 0 until numberOfPieces) {
            puzzleSlots[i] = PuzzleSlot()
        }

        // default order for slots with perfect order
        slotOrder = IntArray(pieces)
        for (i in 0 until pieces) {
            slotOrder[i] = i
        }
    }
}