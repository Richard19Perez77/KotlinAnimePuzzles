package com.compose.myapplication

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.EditText
import android.widget.Toast

/**
 * A class to hold the surface of the puzzle and the thread for updating physics
 * and drawing.
 *
 * @author Rick
 */
class PuzzleSurface(c : Context, attrs: AttributeSet?) : SurfaceView(
    c, attrs
), SurfaceHolder.Callback {

    var soundPool: MySoundPool? = null
    var borderPaintA: Paint
    var borderPaintB: Paint
    var transPaint: Paint
    var fullPaint: Paint
    var puzzle: AdjustablePuzzle? = null
    var puzzleUpdateAndDraw: PuzzleUpdateAndDraw?
    var myMediaPlayer: MyMediaPlayer? = null
    private var ps: PuzzleSurface = this
    private var defaultPuzzleSize = "2"
    lateinit var fragment: FirstFragment

    /**
     * Sets the flag for window focus meaning its able to be interacted with.
     *
     * @param hasWindowFocus
     */
    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        if (CommonVariables.isLogging) Log.d(
            TAG,
            "onWindowFocusChanged PuzzleSurface hasWindowFocus:$hasWindowFocus"
        )
        if (!hasWindowFocus) {
            CommonVariables.isWindowInFocus = false
        } else {
            CommonVariables.isWindowInFocus = true
            puzzleUpdateAndDraw?.updateAndDraw()
        }
    }

    /**
     * On surface create this will be called once with the new screen sizes.
     *
     * @param holder
     * @param format
     * @param width
     * @param height
     */
    override fun surfaceChanged(
        holder: SurfaceHolder, format: Int, width: Int,
        height: Int
    ) {
        if (CommonVariables.isLogging) Log.d(TAG, "surfaceChanged PuzzleSurface $width $height")
        puzzleUpdateAndDraw!!.surfaceChanged(width, height)
        if (CommonVariables.resumePreviousPuzzle) {
            resumePuzzle()
        } else {
            createPuzzle()
        }
    }

    /**
     * Keep a reference to our surface holder.
     *
     * @param holder
     */
    override fun surfaceCreated(holder: SurfaceHolder) {
        if (CommonVariables.isLogging) Log.d(TAG, "surfaceCreated PuzzleSurface")
        puzzleUpdateAndDraw = PuzzleUpdateAndDraw(holder)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if (CommonVariables.isLogging) Log.d(TAG, "surfaceDestroyed PuzzleSurface")
    }

    override fun performClick(): Boolean {
        if (CommonVariables.isLogging) Log.d(TAG, "performClick PuzzleSurface")
        super.performClick()
        return true
    }

    /**
     * Perform Click is called on UP press to perform accessibility type
     * actions.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        synchronized(puzzleUpdateAndDraw!!.surfaceHolder) {
            if (event.action == MotionEvent.ACTION_UP) {
                performClick()
            }
            if (CommonVariables.isWindowInFocus && CommonVariables.isImageLoaded) {
                return if (CommonVariables.isPuzzleSolved) {
                    fragment.toggleUIOverlay()
                    false
                } else {
                    puzzle!!.onTouchEvent(event)
                }
            }
            return super.onTouchEvent(event)
        }
    }

    /**
     * Create a new puzzle based on the sides passed to it.
     *
     * @param sides
     */
    private fun createNewSizedPuzzle(sides: Int) {
        if (CommonVariables.isLogging) Log.d(TAG, "createNewSizedPuzzle PuzzleSurface")
        CommonVariables.isImageLoaded = false
        puzzle = AdjustablePuzzle(ps)
        puzzle!!.initPieces(sides)
        puzzle!!.getNewImageLoadedScaledDivided()
        fragment.hideButtons()
    }

    /**
     * Create a default 3 x 3 puzzle, used if shared prefs fail or first app use.
     */
    private fun createPuzzle() {
        if (CommonVariables.isLogging) Log.d(TAG, "createPuzzle PuzzleSurface")
        CommonVariables.isImageLoaded = false
        puzzle = AdjustablePuzzle(ps)
        puzzle!!.initPieces(3)
        puzzle!!.getNewImageLoadedScaledDivided()
        fragment.hideButtons()
    }

    /**
     * Load the previous puzzle from shared preferences.
     */
    private fun resumePuzzle() {
        if (CommonVariables.isLogging) Log.d(TAG, "resumePuzzle PuzzleSurface")
        CommonVariables.isImageLoaded = false
        puzzle = AdjustablePuzzle(ps)
        val sides = CommonVariables.dimensions.toInt()
        puzzle!!.initPieces(sides)
        puzzle!!.getPreviousImageLoadedScaledDivided()
        fragment.hideButtons()
    }

    val slotString: String
        /**
         * Parse the saved slot order from a saved slot String array.
         *
         * @return
         */
        get() {
            var s = ""
            for (i in CommonVariables.puzzleSlots.indices) {
                if (CommonVariables.puzzleSlots[i] != null) {
                    s = if (i == 0) {
                        "" + (CommonVariables.puzzleSlots[i]?.puzzlePiece?.pieceNum)
                    } else {
                        s + "," + (CommonVariables.puzzleSlots[i]?.puzzlePiece?.pieceNum)
                    }
                }
            }
            return s
        }
    private var dialog: AlertDialog? = null

    /**
     * On create of a new puzzle ask for the new size wanted, range valid 2 - 7 inclusive.
     */
    fun newPuzzle() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle("Create New Puzzle")
        builder.setMessage("Enter number of sides 2 - 7")
        val inputH = EditText(context)
        inputH.inputType = InputType.TYPE_CLASS_NUMBER
        inputH.setText(defaultPuzzleSize)
        builder.setView(inputH)
        builder.setPositiveButton("Create"
        ) { _: DialogInterface?, _: Int ->
            try {
                val s: String = inputH.text.toString()
                s.replace("[^0-9]".toRegex(), "")
                val sides: Int = s.toInt()
                if (sides > 7 || sides < 2) {
                    showToast("2 to 7 dimension limit")
                } else {
                    createNewSizedPuzzle(sides)
                }
            } catch (nfe: NumberFormatException) {
                showToast("Unable to parse number entered.")
            }
        }
        builder.setNegativeButton("Cancel") { _, _ ->
            // Canceled.
        }
        dialog = builder.show()
    }

    private lateinit var toast: Toast

    init {
        // set context for access in other classes

        // register our interest in hearing about changes to our surface
        val holder = holder
        holder.addCallback(this)
        borderPaintA = Paint()
        borderPaintA.style = Paint.Style.STROKE
        borderPaintA.strokeWidth = STROKE_VALUE.toFloat()
        borderPaintA.color = Color.LTGRAY
        borderPaintA.alpha = TRANS_VALUE
        borderPaintB = Paint()
        borderPaintB.style = Paint.Style.STROKE
        borderPaintB.strokeWidth = STROKE_VALUE.toFloat()
        borderPaintB.color = Color.DKGRAY
        borderPaintB.alpha = TRANS_VALUE
        transPaint = Paint()
        transPaint.alpha = TRANS_VALUE
        transPaint.style = Paint.Style.FILL
        fullPaint = Paint()
        puzzleUpdateAndDraw = PuzzleUpdateAndDraw(holder)
    }

    private fun showToast(message: String?) {
        // Create and show toast for save photo
        toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast.show()
    }

    /**
     * Set flag for music on/off.
     */
    fun toggleMusic() {
        if (CommonVariables.playMusic) {
            CommonVariables.playMusic = false
            showToast("Music Off")
        } else {
            CommonVariables.playMusic = true
            showToast("Music On")
        }
        myMediaPlayer!!.togglePause()
    }

    /**
     * Set flag for drawing the border on/off.
     */
    fun toggleBorder() {
        if (CommonVariables.drawBorders) {
            CommonVariables.drawBorders = false
            showToast("Borders Off")
        } else {
            CommonVariables.drawBorders = true
            showToast("Borders On")
        }
    }

    /**
     * Set flag for win chime on/off.
     */
    fun toggleWinSound() {
        if (CommonVariables.playChimeSound) {
            CommonVariables.playChimeSound = false
            showToast("Win Effect Off")
        } else {
            CommonVariables.playChimeSound = true
            showToast("Win Effect On")
        }
    }

    /**
     * Pause the puzzle and draw class.
     */
    fun onPause() {
        if (CommonVariables.isLogging) Log.d(TAG, "onPause PuzzleSurface")
        if (puzzleUpdateAndDraw != null) {
            puzzleUpdateAndDraw!!.pause()
        }
    }

    /**
     * Set flag for toggle sound on/off.
     */
    fun toggleSetSound() {
        if (CommonVariables.playTapSound) {
            CommonVariables.playTapSound = false
            showToast("Set Effect Off")
        } else {
            CommonVariables.playTapSound = true
            showToast("Set Effect On")
        }
    }

    /**
     * A class to hold the SurfaceHolder and perform the draw operations.
     */
    inner class PuzzleUpdateAndDraw(val surfaceHolder: SurfaceHolder) {

        /**
         * Lock the canvas before drawing then unlock to perform the draw.
         */
        fun updateAndDraw() {
            if (CommonVariables.isLogging) Log.d(TAG, "updateAndDraw PuzzleSurface")
            fragment.updatePhysics()
            var c: Canvas? = null
            try {
                c = surfaceHolder.lockCanvas(null)
                if (c != null) {
                    synchronized(surfaceHolder) { doDraw(c) }
                }
            } finally {
                if (c != null) {
                    surfaceHolder.unlockCanvasAndPost(c)
                }
            }
        }

        /**
         * Draw may include a shadow piece to replace a moving piece.
         *
         * @param canvas
         */
        private fun doDraw(canvas: Canvas?) {
            if (CommonVariables.isLogging) Log.d(TAG, "doDraw PuzzleSurface")
            if (canvas != null) {
                canvas.drawColor(Color.BLACK)
                if (CommonVariables.isImageLoaded) {
                    if (CommonVariables.movingPiece) {
                        drawImageWithMovingPiece(canvas)
                    } else {
                        drawImage(canvas)
                    }
                } else {
                    // the imageID is still loading or in error
                    if (CommonVariables.isImageError) {
                        canvas.drawColor(Color.RED)
                    } else {
                        canvas.drawColor(Color.BLUE)
                    }
                }
            }
        }

        /**
         * Draw each piece at the slot it currently is placed into.
         *
         * @param canvas
         */
        private fun drawImage(canvas: Canvas) {
            if (CommonVariables.isLogging) Log.d(TAG, "drawImage PuzzleSurface")
            for (i in 0 until CommonVariables.numberOfPieces) {
                if (!CommonVariables.puzzleSlots[i]?.puzzlePiece?.bitmap?.isRecycled!!) {

                    // draw pieces
                    CommonVariables.puzzleSlots[i]?.puzzlePiece?.px?.let {
                        CommonVariables.puzzleSlots[i]?.puzzlePiece?.py?.let { it1 ->
                            canvas.drawBitmap(
                                CommonVariables.puzzleSlots[i]?.puzzlePiece?.bitmap!!,
                                it.toFloat(),
                                it1.toFloat(), null
                            )
                        }
                    }
                    // draw borders
                    if (!CommonVariables.isPuzzleSolved && CommonVariables.drawBorders) {
                        CommonVariables.puzzleSlots[i]?.sx?.let {
                            CommonVariables.puzzleSlots[i]?.sy?.let { it1 ->
                                (CommonVariables.puzzleSlots[i]?.sx)?.plus(
                                    (CommonVariables.puzzleSlots[i]?.puzzlePiece?.bitmap
                                        ?.width ?: 0)
                                )?.let { it2 ->
                                    (CommonVariables.puzzleSlots[i]?.sy)?.plus(
                                        (CommonVariables.puzzleSlots[i]?.puzzlePiece?.bitmap
                                            ?.height ?: 0)
                                    )?.let { it3 ->
                                        canvas.drawRect(
                                            it.toFloat(),
                                            it1.toFloat(), it2.toFloat(), it3.toFloat(),
                                            borderPaintA
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        /**
         * Draw the moving piece in full color and the shadow piece in its place as semi-transparent.
         *
         * @param canvas
         */
        private fun drawImageWithMovingPiece(canvas: Canvas) {
            for (i in 0 until CommonVariables.numberOfPieces) {
                // draw pieces
                if (!CommonVariables.puzzleSlots[i]?.puzzlePiece?.bitmap?.isRecycled!! && CommonVariables.currSlotOnTouchDown != i) CommonVariables.puzzleSlots[i]?.puzzlePiece?.px?.toFloat()
                    ?.let {
                        CommonVariables.puzzleSlots[i]?.puzzlePiece?.py?.let { it1 ->
                            canvas.drawBitmap(
                                CommonVariables.puzzleSlots[i]?.puzzlePiece?.bitmap!!,
                                it,
                                it1.toFloat(), null
                            )
                        }
                    }
                // draw border to pieces
                if (!CommonVariables.isPuzzleSolved && CommonVariables.drawBorders) CommonVariables.puzzleSlots[i]?.sx?.let {
                    CommonVariables.puzzleSlots[i]?.sy?.let { it1 ->
                        (CommonVariables.puzzleSlots[i]?.sx)?.plus(
                            (CommonVariables.puzzleSlots[i]?.puzzlePiece?.bitmap
                                ?.width ?: 0)
                        )?.let { it2 ->
                            (CommonVariables.puzzleSlots[i]?.sy)?.plus(
                                (CommonVariables.puzzleSlots[i]?.puzzlePiece?.bitmap
                                    ?.height ?: 0)
                            )?.let { it3 ->
                                canvas.drawRect(
                                    it.toFloat(),
                                    it1.toFloat(), it2.toFloat(), it3.toFloat(),
                                    borderPaintA
                                )
                            }
                        }
                    }
                }
            }

            // draw moving piece and its shadow
            if (!CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.puzzlePiece?.bitmap?.isRecycled!!) {

                // draw moving imageID in original location
                CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.sy?.let {
                    CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.sx?.let { it1 ->
                        canvas.drawBitmap(
                            CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.puzzlePiece?.bitmap!!,
                            it1.toFloat(),
                            it.toFloat(),
                            transPaint
                        )
                    }
                }

                // draw border around original piece location
                CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.sx?.let {
                    CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.sy?.let { it1 ->
                        (CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.sx)?.plus(
                            (CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.puzzlePiece?.bitmap
                                ?.width ?: 0)
                        )?.let { it2 ->
                            (CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.sy)?.plus(
                                (CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.puzzlePiece?.bitmap
                                    ?.height ?: 0)
                            )?.let { it3 ->
                                canvas.drawRect(
                                    it.toFloat(),
                                    it1.toFloat(), it2.toFloat(), it3.toFloat(), borderPaintB
                                )
                            }
                        }
                    }
                }

                // draw moving piece
                CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.puzzlePiece?.px?.let {
                    CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.puzzlePiece?.py?.let { it1 ->
                        canvas.drawBitmap(
                            CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.puzzlePiece?.bitmap!!,
                            it.toFloat(),
                            it1.toFloat(),
                            fullPaint
                        )
                    }
                }

                // draw border around moving piece
                if (CommonVariables.drawBorders) (CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.puzzlePiece?.px)?.plus(
                    STROKE_VALUE / 2
                )?.let {
                    (CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.puzzlePiece?.py)?.plus(
                        STROKE_VALUE / 2
                    )?.let { it1 ->
                        ((CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.puzzlePiece?.px)?.plus(
                            (CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.puzzlePiece?.bitmap
                                ?.width ?: 0)
                        ))?.minus((STROKE_VALUE / 2))?.let { it2 ->
                            ((CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.puzzlePiece?.py)?.plus(
                                (CommonVariables.puzzleSlots[CommonVariables.currSlotOnTouchDown]?.puzzlePiece?.bitmap
                                    ?.height ?: 0)
                            ))?.minus((STROKE_VALUE / 2))?.let { it3 ->
                                canvas.drawRect(
                                    it.toFloat(),
                                    it1.toFloat(),
                                    it2.toFloat(),
                                    it3.toFloat(),
                                    borderPaintA
                                )
                            }
                        }
                    }
                }
            }
        }

        fun surfaceChanged(width: Int, height: Int) {
            // synchronized to make sure these all change atomically
            synchronized(surfaceHolder) {
                CommonVariables.screenW = width
                CommonVariables.screenH = height
            }
        }

        fun pause() {
            synchronized(surfaceHolder) { if (puzzle != null) puzzle!!.pause() }
        }
    }

    companion object {
        const val TRANS_VALUE = (255 / 2)
        const val STROKE_VALUE = 5
        private const val TAG = "puzzleLog"
    }
}