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

    var borderPaintA: Paint
    var borderPaintB: Paint
    var transPaint: Paint
    var fullPaint: Paint
    var puzzle: AdjustablePuzzle? = null
    var puzzleUpdateAndDraw: PuzzleUpdateAndDraw?
    var myMediaPlayer: MyMediaPlayer? = null
    var common : CommonVariables = CommonVariables.getInstance()!!
    private var ps: PuzzleSurface = this
    private var defaultPuzzleSize = "2"
    lateinit var fragment: FirstFragment

    /**
     * Sets the flag for window focus meaning its able to be interacted with.
     *
     * @param hasWindowFocus
     */
    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        if (common.isLogging) Log.d(
            TAG,
            "onWindowFocusChanged PuzzleSurface hasWindowFocus:$hasWindowFocus"
        )
        if (!hasWindowFocus) {
            common.isWindowInFocus = false
        } else {
            common.isWindowInFocus = true
            puzzleUpdateAndDraw!!.updateAndDraw()
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
        if (common.isLogging) Log.d(TAG, "surfaceChanged PuzzleSurface $width $height")
        puzzleUpdateAndDraw!!.surfaceChanged(width, height)
        if (common.resumePreviousPuzzle) {
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
        if (common.isLogging) Log.d(TAG, "surfaceCreated PuzzleSurface")
        puzzleUpdateAndDraw = PuzzleUpdateAndDraw(holder)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if (common.isLogging) Log.d(TAG, "surfaceDestroyed PuzzleSurface")
    }

    override fun performClick(): Boolean {
        if (common.isLogging) Log.d(TAG, "performClick PuzzleSurface")
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
            if (common.isWindowInFocus && common.isImageLoaded) {
                return if (common.isPuzzleSolved) {
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
        if (common.isLogging) Log.d(TAG, "createNewSizedPuzzle PuzzleSurface")
        common.isImageLoaded = false
        puzzle = AdjustablePuzzle(ps)
        puzzle!!.initPieces(sides)
        puzzle!!.getNewImageLoadedScaledDivided()
        fragment.hideButtons()
    }

    /**
     * Create a default 3 x 3 puzzle, used if shared prefs fail or first app use.
     */
    private fun createPuzzle() {
        if (common.isLogging) Log.d(TAG, "createPuzzle PuzzleSurface")
        common.isImageLoaded = false
        puzzle = AdjustablePuzzle(ps)
        puzzle!!.initPieces(3)
        puzzle!!.getNewImageLoadedScaledDivided()
        fragment.hideButtons()
    }

    /**
     * Load the previous puzzle from shared preferences.
     */
    private fun resumePuzzle() {
        if (common.isLogging) Log.d(TAG, "resumePuzzle PuzzleSurface")
        common.isImageLoaded = false
        puzzle = AdjustablePuzzle(ps)
        val sides = common.dimensions.toInt()
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
            if (common.puzzleSlots != null) for (i in common.puzzleSlots.indices) {
                if (common.puzzleSlots[i] != null) {
                    s = if (i == 0) {
                        "" + common.puzzleSlots[i].puzzlePiece.pieceNum
                    } else {
                        s + "," + common.puzzleSlots[i].puzzlePiece.pieceNum
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
        if (common.playMusic) {
            common.playMusic = false
            showToast("Music Off")
        } else {
            common.playMusic = true
            showToast("Music On")
        }
        myMediaPlayer!!.togglePause()
    }

    /**
     * Set flag for drawing the border on/off.
     */
    fun toggleBorder() {
        if (common.drawBorders) {
            common.drawBorders = false
            showToast("Borders Off")
        } else {
            common.drawBorders = true
            showToast("Borders On")
        }
    }

    /**
     * Set flag for win chime on/off.
     */
    fun toggleWinSound() {
        if (common.playChimeSound) {
            common.playChimeSound = false
            showToast("Win Effect Off")
        } else {
            common.playChimeSound = true
            showToast("Win Effect On")
        }
    }

    /**
     * Pause the puzzle and draw class.
     */
    fun onPause() {
        if (common.isLogging) Log.d(TAG, "onPause PuzzleSurface")
        if (puzzleUpdateAndDraw != null) {
            puzzleUpdateAndDraw!!.pause()
        }
    }

    /**
     * Set flag for toggle sound on/off.
     */
    fun toggleSetSound() {
        if (common.playTapSound) {
            common.playTapSound = false
            showToast("Set Effect Off")
        } else {
            common.playTapSound = true
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
            if (common.isLogging) Log.d(TAG, "updateAndDraw PuzzleSurface")
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
            if (common.isLogging) Log.d(TAG, "doDraw PuzzleSurface")
            if (canvas != null) {
                canvas.drawColor(Color.BLACK)
                if (common.isImageLoaded) {
                    if (common.movingPiece) {
                        drawImageWithMovingPiece(canvas)
                    } else {
                        drawImage(canvas)
                    }
                } else {
                    // the imageID is still loading or in error
                    if (common.isImageError) {
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
            if (common.isLogging) Log.d(TAG, "drawImage PuzzleSurface")
            for (i in 0 until common.numberOfPieces) {
                if (!common.puzzleSlots[i].puzzlePiece.bitmap?.isRecycled!!) {

                    // draw pieces
                    canvas.drawBitmap(
                        common.puzzleSlots[i].puzzlePiece.bitmap!!,
                        common.puzzleSlots[i].puzzlePiece.px.toFloat(),
                        common.puzzleSlots[i].puzzlePiece.py.toFloat(), null
                    )
                    // draw borders
                    if (!common.isPuzzleSolved && common.drawBorders) {
                        canvas.drawRect(
                            common.puzzleSlots[i].sx.toFloat(),
                            common.puzzleSlots[i].sy.toFloat(), (
                                    common.puzzleSlots[i].sx
                                            + (common.puzzleSlots[i].puzzlePiece.bitmap
                                                ?.width ?: 0)).toFloat(), (
                                    common.puzzleSlots[i].sy
                                            + (common.puzzleSlots[i].puzzlePiece.bitmap
                                                ?.height ?: 0)).toFloat(),
                            borderPaintA
                        )
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
            for (i in 0 until common.numberOfPieces) {
                // draw pieces
                if (!common.puzzleSlots[i].puzzlePiece.bitmap?.isRecycled!! && common.currSlotOnTouchDown != i) canvas.drawBitmap(
                    common.puzzleSlots[i].puzzlePiece.bitmap!!,
                    common.puzzleSlots[i].puzzlePiece.px.toFloat(),
                    common.puzzleSlots[i].puzzlePiece.py.toFloat(), null
                )
                // draw border to pieces
                if (!common.isPuzzleSolved && common.drawBorders) canvas.drawRect(
                    common.puzzleSlots[i].sx.toFloat(),
                    common.puzzleSlots[i].sy.toFloat(), (
                            common.puzzleSlots[i].sx
                                    + (common.puzzleSlots[i].puzzlePiece.bitmap
                                        ?.width ?: 0)).toFloat(), (
                            common.puzzleSlots[i].sy
                                    + (common.puzzleSlots[i].puzzlePiece.bitmap
                                        ?.height ?: 0)).toFloat(),
                    borderPaintA
                )
            }

            // draw moving piece and its shadow
            if (!common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.bitmap?.isRecycled!!) {

                // draw moving imageID in original location
                canvas.drawBitmap(
                    common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.bitmap!!,
                    common.puzzleSlots[common.currSlotOnTouchDown].sx.toFloat(),
                    common.puzzleSlots[common.currSlotOnTouchDown].sy.toFloat(),
                    transPaint
                )

                // draw border around original piece location
                canvas.drawRect(
                    common.puzzleSlots[common.currSlotOnTouchDown].sx.toFloat(),
                    common.puzzleSlots[common.currSlotOnTouchDown].sy.toFloat(), (
                            common.puzzleSlots[common.currSlotOnTouchDown].sx
                                    + (common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.bitmap
                                        ?.width ?: 0)).toFloat(), (
                            common.puzzleSlots[common.currSlotOnTouchDown].sy
                                    + (common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.bitmap
                                        ?.height ?: 0)).toFloat(), borderPaintB
                )

                // draw moving piece
                canvas.drawBitmap(
                    common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.bitmap!!,
                    common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.px.toFloat(),
                    common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.py.toFloat(),
                    fullPaint
                )

                // draw border around moving piece
                if (common.drawBorders) canvas.drawRect(
                    (
                            common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.px + STROKE_VALUE / 2).toFloat(),
                    (
                            common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.py + STROKE_VALUE / 2).toFloat(),
                    (
                            common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.px
                                    + (common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.bitmap
                                        ?.width ?: 0)
                                    - (STROKE_VALUE / 2)).toFloat(),
                    (
                            common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.py
                                    + (common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.bitmap
                                        ?.height ?: 0)
                                    - (STROKE_VALUE / 2)).toFloat(),
                    borderPaintA
                )
            }
        }

        fun surfaceChanged(width: Int, height: Int) {
            // synchronized to make sure these all change atomically
            synchronized(surfaceHolder) {
                common.screenW = width
                common.screenH = height
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