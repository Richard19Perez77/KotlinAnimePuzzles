package com.compose.myapplication;

import android.content.Context;
import android.media.SoundPool;

/**
 * Sound Pool extension that included loading sound and playing sounds.
 *
 * @author Rick
 */
public class MySoundPool extends SoundPool implements SoundPool.OnLoadCompleteListener {

    Context context;
    final int TAP = 1;
    final int CHIME = 2;
    CommonVariables commonVariables = CommonVariables.getInstance();

    @SuppressWarnings("deprecation")
    public MySoundPool(Context context, int maxStreams, int streamType, int srcQuality) {
        super(maxStreams, streamType, srcQuality);
        this.context = context;
    }

    /**
     * Check for sound file to be loaded and wanting to be player
     */
    public void playChimeSound() {
        if (commonVariables.chimeLoaded && commonVariables.playChimeSound)
            play(commonVariables.saveSound, commonVariables.volume,
                    commonVariables.volume, 1, 0, 1f);
    }

    /**
     * Check for tap sound to be loaded and it in preferences
     */
    public void playSetSound() {
        if (commonVariables.tapLoaded && commonVariables.playTapSound)
            play(commonVariables.tapSound, commonVariables.volume,
                    commonVariables.volume, 1, 0, 1f);
    }

    /**
     * Load the sound pool sounds.
     */
    public void init() {
        setOnLoadCompleteListener(this);
        commonVariables.saveSound = load(context,
                R.raw.imagesaved, 1);
        commonVariables.tapSound = load(context, R.raw.tap, 1);
    }

    /**
     * Verify the sounds have been loaded.
     *
     * @param soundPool
     * @param sampleId
     * @param status
     */
    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        if (sampleId == TAP)
            commonVariables.tapLoaded = true;
        else if (sampleId == CHIME)
            commonVariables.chimeLoaded = true;
    }
}