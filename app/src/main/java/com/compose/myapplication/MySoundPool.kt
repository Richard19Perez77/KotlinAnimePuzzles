package com.compose.myapplication

import android.content.Context
import android.media.SoundPool

/**
 * Sound Pool extension that included loading sound and playing sounds.
 *
 * @author Rick
 */
class MySoundPool @Suppress("deprecation") constructor(
    var context: Context?,
    maxStreams: Int,
    streamType: Int,
    srcQuality: Int
) : SoundPool(maxStreams, streamType, srcQuality), SoundPool.OnLoadCompleteListener {

    private val TAP = 1
    private val CHIME = 2

    /**
     * Check for sound file to be loaded and wanting to be player
     */
    fun playChimeSound() {
        if (CommonVariables.chimeLoaded && CommonVariables.playChimeSound)
            play(CommonVariables.saveSound, 1f, 1f, 1, 0, 1f)
    }

    /**
     * Check for tap sound to be loaded and it in preferences
     */
    fun playSetSound() {
        if (CommonVariables.tapLoaded && CommonVariables.playTapSound)
            play(CommonVariables.tapSound, 1f, 1f, 1, 0, 1f)
    }

    /**
     * Load the sound pool sounds.
     */
    fun init() {
        setOnLoadCompleteListener(this)
        CommonVariables.saveSound = load(context, R.raw.imagesaved, 1)
        CommonVariables.tapSound = load(context, R.raw.tap, 1)
    }

    /**
     * Verify the sounds have been loaded.
     *
     * @param soundPool
     * @param sampleId
     * @param status
     */
    override fun onLoadComplete(soundPool: SoundPool, sampleId: Int, status: Int) {
        if (sampleId == TAP)
            CommonVariables.tapLoaded = true
        else if (sampleId == CHIME)
            CommonVariables.chimeLoaded = true
    }
}