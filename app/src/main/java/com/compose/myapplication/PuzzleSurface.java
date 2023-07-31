package com.compose.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

/**
 * A class to hold the surface of the puzzle and the thread for updating physics
 * and drawing.
 *
 * @author Rick
 */
public class PuzzleSurface extends SurfaceView implements
        SurfaceHolder.Callback {

    public static final int TRANS_VALUE = (255 / 2);
    public static final int STROKE_VALUE = 5;
    private static final String TAG = "puzzleLog";
    public Paint borderPaintA, borderPaintB, transPaint, fullPaint;
    public AdjustablePuzzle puzzle;
    public PuzzleUpdateAndDraw puzzleUpdateAndDraw;
    public MyMediaPlayer myMediaPlayer;
    public CommonVariables common = CommonVariables.getInstance();
    public PuzzleSurface ps;
    public String defaultPuzzleSize = "2";
    public Context context;

    public PuzzleSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        // set context for access in other classes
        this.context = context;
        ps = this;

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        borderPaintA = new Paint();
        borderPaintA.setStyle(Paint.Style.STROKE);
        borderPaintA.setStrokeWidth(STROKE_VALUE);
        borderPaintA.setColor(Color.LTGRAY);
        borderPaintA.setAlpha(TRANS_VALUE);

        borderPaintB = new Paint();
        borderPaintB.setStyle(Paint.Style.STROKE);
        borderPaintB.setStrokeWidth(STROKE_VALUE);
        borderPaintB.setColor(Color.DKGRAY);
        borderPaintB.setAlpha(TRANS_VALUE);

        transPaint = new Paint();
        transPaint.setAlpha(TRANS_VALUE);
        transPaint.setStyle(Paint.Style.FILL);

        fullPaint = new Paint();
        puzzleUpdateAndDraw = new PuzzleUpdateAndDraw(holder, context);

    }

    /**
     * Prepare dialog for getting a new image, let the user know the deviant art link is updating as new images are loaded.
     */
    public void nextImage() {
        common.hideButtons(context);
        common.isImageLoaded = false;
        puzzle.getNewImageLoadedScaledDivided();

        Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_out);
        Animation.AnimationListener animationListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                common.textViewSolve.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
        animation.setAnimationListener(animationListener);
        common.textViewSolve.setAnimation(animation);
    }

    /**
     * Sets the flag for window focus meaning its able to be interacted with.
     *
     * @param hasWindowFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (common.isLogging) Log.d(TAG, "onWindowFocusChanged PuzzleSurface hasWindowFocus:" + hasWindowFocus);

        if (!hasWindowFocus) {
            common.isWindowInFocus = false;
        } else {
            common.isWindowInFocus = true;
            puzzleUpdateAndDraw.updateAndDraw();
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
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        if (common.isLogging) Log.d(TAG, "surfaceChanged PuzzleSurface " + width + " " + height);

        puzzleUpdateAndDraw.surfaceChanged(width, height);
        if (common.resumePreviousPuzzle) {
            common.resumePreviousPuzzle = false;
            resumePuzzle();
        } else if (common.createNewPuzzle) {
            common.createNewPuzzle = false;
            createPuzzle();
        }
    }

    /**
     * Keep a reference to our surface holder.
     *
     * @param holder
     */
    public void surfaceCreated(SurfaceHolder holder) {
        if (common.isLogging) Log.d(TAG, "surfaceCreated PuzzleSurface");

        puzzleUpdateAndDraw = new PuzzleUpdateAndDraw(holder, context);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (common.isLogging) Log.d(TAG, "surfaceDestroyed PuzzleSurface");
    }

    @Override
    public boolean performClick() {
        if (common.isLogging) Log.d(TAG, "performClick PuzzleSurface");

        super.performClick();
        return true;
    }

    /**
     * Perform Click is called on UP press to perform accessibility type
     * actions.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        synchronized (puzzleUpdateAndDraw.getSurfaceHolder()) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                performClick();
            }

            if (common.isWindowInFocus && common.isImageLoaded) {
                if (common.isPuzzleSolved) {
                    common.toggleUIOverlay(context);
                    return false;
                } else {
                    return puzzle.onTouchEvent(event);
                }
            }
            return super.onTouchEvent(event);
        }
    }

    /**
     * Create a new puzzle based on the sides passed to it.
     *
     * @param sides
     */
    public void createNewSizedPuzzle(int sides) {
        if (common.isLogging) Log.d(TAG, "createNewSizedPuzzle PuzzleSurface");

        common.isImageLoaded = false;
        puzzle = new AdjustablePuzzle(ps);
        puzzle.initPieces(sides);
        puzzle.getNewImageLoadedScaledDivided();
        common.hideButtons(context);
    }

    /**
     * Create a default 3 x 3 puzzle, used if shared prefs fail or first app use.
     */
    public void createPuzzle() {
        if (common.isLogging) Log.d(TAG, "createPuzzle PuzzleSurface");

        common.isImageLoaded = false;
        puzzle = new AdjustablePuzzle(ps);
        puzzle.initPieces(3);
        puzzle.getNewImageLoadedScaledDivided();
        common.hideButtons(context);
    }

    /**
     * Load the previous puzzle from shared preferences.
     */
    public void resumePuzzle() {
        if (common.isLogging) Log.d(TAG, "resumePuzzle PuzzleSurface");

        common.isImageLoaded = false;
        puzzle = new AdjustablePuzzle(ps);
        int sides = (int) common.dimensions;
        puzzle.initPieces(sides);
        puzzle.getPreviousImageLoadedScaledDivided();
        common.hideButtons(context);
    }

    /**
     * Load a new window for the internet link provided.
     */
    public void devartActivity() {
        Activity act = (Activity) context;
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setTitle(common.res.getString(R.string.deviant_title));
        builder.setMessage(common.data.artworks[common.currentPuzzleImagePosition].titleOfArtist + common.res.getString(R.string.deviant_message))
                .setPositiveButton(common.res.getString(R.string.continue_desc), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        common.hideButtons(context);
                        Intent intent2 = new Intent(Intent.ACTION_VIEW);
                        intent2.setData(Uri.parse(common.data.artworks[common.currentPuzzleImagePosition].urlOfArtist));
                        common.blogLinksTraversed++;
                        context.startActivity(intent2);
                    }
                })
                .setNegativeButton(common.res.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //close dialog
                    }
                });
        builder.show();
    }

    /**
     * Load a new window for the internet link provided.
     */
    public void wordpressActivity() {
        common.hideButtons(context);
        Intent intent1 = new Intent(Intent.ACTION_VIEW);
        intent1.setData(Uri.parse(context
                .getString(R.string.wordpress_link)));
        common.blogLinksTraversed++;
        context.startActivity(intent1);
    }

    public void cleanUp() {
        if (puzzle != null)
            puzzle.recylceAll();
    }

    /**
     * Parse the saved slot order from a saved slot String array.
     *
     * @return
     */
    public String getSlotString() {
        String s = "";
        if (common.puzzleSlots != null)
            for (int i = 0; i < common.puzzleSlots.length; i++) {
                if (common.puzzleSlots[i] != null) {
                    if (i == 0) {
                        s = "" + common.puzzleSlots[i].puzzlePiece.pieceNum;
                    } else {
                        s = s + "," + common.puzzleSlots[i].puzzlePiece.pieceNum;
                    }
                }
            }
        return s;
    }

    public AlertDialog dialog;

    /**
     * On create of a new puzzle ask for the new size wanted, range valid 2 - 7 inclusive.
     */
    public void newPuzzle() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(context);

        builder.setTitle("Create New Puzzle");
        builder.setMessage("Enter number of sides 2 - 7");

        final EditText inputH = new EditText(context);
        inputH.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputH.setText(defaultPuzzleSize);
        builder.setView(inputH);

        builder.setPositiveButton("Create",
                (dialog, whichButton) -> {
                    try {
                        String s = inputH.getText().toString();
                        s.replaceAll("[^0-9]", "");
                        int sides = Integer.parseInt(s);

                        if (sides > 7 || sides < 2) {
                            showToast("2 to 7 dimension limit");
                        } else {
                            createNewSizedPuzzle(sides);
                        }
                    } catch (NumberFormatException nfe) {
                        showToast("Unable to parse number entered.");
                    }
                });

        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });
        dialog = builder.show();
    }

    Toast toast = null;

    public void showToast(String message) {
        // Create and show toast for save photo
        toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Set flag for music on/off.
     */
    public void toggleMusic() {
        if (common.playMusic) {
            common.playMusic = false;
            myMediaPlayer.pause();
            showToast("Music Off");
        } else {
            common.playMusic = true;
            myMediaPlayer.resume();
            showToast("Music On");
        }
    }

    /**
     * Set flag for drawing the border on/off.
     */
    public void toggleBorder() {
        if (common.drawBorders) {
            common.drawBorders = false;
            showToast("Borders Off");
        } else {
            common.drawBorders = true;
            showToast("Borders On");
        }
    }

    /**
     * Set flag for win chime on/off.
     */
    public void toggleWinSound() {
        if (common.playChimeSound) {
            common.playChimeSound = false;
            showToast("Win Effect Off");
        } else {
            common.playChimeSound = true;
            showToast("Win Effect On");
        }
    }

    /**
     * Pause the puzzle and draw class.
     */
    public void onPause() {
        if (common.isLogging) Log.d(TAG, "onPause PuzzleSurface");

        if (puzzleUpdateAndDraw != null) {
            puzzleUpdateAndDraw.pause();
        }
    }

    /**
     * Set flag for toggle sound on/off.
     */
    public void toggleSetSound() {
        if (common.playTapSound) {
            common.playTapSound = false;
            showToast("Set Effect Off");
        } else {
            common.playTapSound = true;
            showToast("Set Effect On");
        }
    }

    /**
     * A class to hold the SurfaceHolder and perform the draw operations.
     */
    public class PuzzleUpdateAndDraw {

        public final SurfaceHolder mSurfaceHolder;

        public PuzzleUpdateAndDraw(SurfaceHolder surfaceHolder,
                                   Context context) {
            mSurfaceHolder = surfaceHolder;
            context = context;
        }

        /**
         * Lock the canvas before drawing then unlock to perform the draw.
         */
        public void updateAndDraw() {
            if (common.isLogging)
                Log.d(TAG, "updateAndDraw PuzzleSurface");

            Canvas c = null;
            try {
                c = mSurfaceHolder.lockCanvas(null);
                if (c != null) {
                    synchronized (mSurfaceHolder) {
                        updatePhysics();
                        doDraw(c);
                    }
                }
            } finally {
                if (c != null) {
                    mSurfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }

        public SurfaceHolder getSurfaceHolder() {
            return mSurfaceHolder;
        }

        /**
         * The UI is updated on solve of a puzzle where the buttons are shown.
         */
        private void updatePhysics() {
            if (common.isLogging) Log.d(TAG, "updatePhysics PuzzleSurface");

            if (common.isPuzzleSolved) {
                final String solveTime = "Solve time = " + puzzle.getSolveTime() + " secs.";
                common.textViewSolve.postDelayed(() -> {
                    common.textViewSolve.setText(solveTime);
                    common.textViewSolve.setVisibility(View.VISIBLE);
                    Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    common.textViewSolve.setAnimation(animation);
                }, 0);
                common.showButtons(context);
            }
        }

        /**
         * Draw may include a shadow piece to replace a moving piece.
         *
         * @param canvas
         */
        private void doDraw(Canvas canvas) {
            if (common.isLogging)
                Log.d(TAG, "doDraw PuzzleSurface");

            if (canvas != null) {
                canvas.drawColor(Color.BLACK);
                if (common.isImageLoaded) {
                    if (common.movingPiece) {
                        drawImageWithMovingPiece(canvas);
                    } else {
                        drawImage(canvas);
                    }
                } else {
                    // the imageID is still loading or in error
                    if (common.isImageError) {
                        canvas.drawColor(Color.RED);
                    } else {
                        canvas.drawColor(Color.BLUE);
                    }
                }
            }
        }

        /**
         * Draw each piece at the slot it currently is placed into.
         *
         * @param canvas
         */
        private void drawImage(Canvas canvas) {
            if (common.isLogging) Log.d(TAG, "drawImage PuzzleSurface");

            for (int i = 0; i < common.numberOfPieces; i++) {
                if (!common.puzzleSlots[i].puzzlePiece.bitmap
                        .isRecycled()) {
                    // draw pieces
                    canvas.drawBitmap(
                            common.puzzleSlots[i].puzzlePiece.bitmap,
                            common.puzzleSlots[i].puzzlePiece.px,
                            common.puzzleSlots[i].puzzlePiece.py, null);
                    // draw borders
                    if (!common.isPuzzleSolved && common.drawBorders) {
                        canvas.drawRect(
                                common.puzzleSlots[i].sx,
                                common.puzzleSlots[i].sy,
                                common.puzzleSlots[i].sx
                                        + common.puzzleSlots[i].puzzlePiece.bitmap
                                        .getWidth(),
                                common.puzzleSlots[i].sy
                                        + common.puzzleSlots[i].puzzlePiece.bitmap
                                        .getHeight(),
                                borderPaintA);
                    }
                }
            }
        }

        /**
         * Draw the moving piece in full color and the shadow piece in its place as semi-transparent.
         *
         * @param canvas
         */
        private void drawImageWithMovingPiece(Canvas canvas) {
            for (int i = 0; i < common.numberOfPieces; i++) {
                // draw pieces
                if (!common.puzzleSlots[i].puzzlePiece.bitmap
                        .isRecycled()
                        && common.currSlotOnTouchDown != i)
                    canvas.drawBitmap(
                            common.puzzleSlots[i].puzzlePiece.bitmap,
                            common.puzzleSlots[i].puzzlePiece.px,
                            common.puzzleSlots[i].puzzlePiece.py, null);
                // draw border to pieces
                if (!common.isPuzzleSolved && common.drawBorders)
                    canvas.drawRect(
                            common.puzzleSlots[i].sx,
                            common.puzzleSlots[i].sy,
                            common.puzzleSlots[i].sx
                                    + common.puzzleSlots[i].puzzlePiece.bitmap
                                    .getWidth(),
                            common.puzzleSlots[i].sy
                                    + common.puzzleSlots[i].puzzlePiece.bitmap
                                    .getHeight(),
                            borderPaintA);
            }

            // draw moving piece and its shadow
            if (!common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.bitmap
                    .isRecycled()) {

                // draw moving imageID in original location
                canvas.drawBitmap(
                        common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.bitmap,
                        common.puzzleSlots[common.currSlotOnTouchDown].sx,
                        common.puzzleSlots[common.currSlotOnTouchDown].sy,
                        transPaint);

                // draw border around original piece location
                canvas.drawRect(
                        common.puzzleSlots[common.currSlotOnTouchDown].sx,
                        common.puzzleSlots[common.currSlotOnTouchDown].sy,
                        common.puzzleSlots[common.currSlotOnTouchDown].sx
                                + common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.bitmap
                                .getWidth(),
                        common.puzzleSlots[common.currSlotOnTouchDown].sy
                                + common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.bitmap
                                .getHeight(), borderPaintB);

                // draw moving piece
                canvas.drawBitmap(
                        common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.bitmap,
                        common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.px,
                        common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.py,
                        fullPaint);

                // draw border around moving piece
                if (common.drawBorders)
                    canvas.drawRect(
                            common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.px
                                    + (STROKE_VALUE / 2),
                            common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.py
                                    + (STROKE_VALUE / 2),
                            common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.px
                                    + common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.bitmap
                                    .getWidth()
                                    - (STROKE_VALUE / 2),
                            common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.py
                                    + common.puzzleSlots[common.currSlotOnTouchDown].puzzlePiece.bitmap
                                    .getHeight()
                                    - (STROKE_VALUE / 2),
                            borderPaintA);
            }
        }

        public void surfaceChanged(int width, int height) {
            // synchronized to make sure these all change atomically
            synchronized (mSurfaceHolder) {
                common.screenW = width;
                common.screenH = height;
            }
        }

        public void pause() {
            synchronized (mSurfaceHolder) {
                if (puzzle != null)
                    puzzle.pause();
            }
        }
    }
}