package com.compose.myapplication

import android.content.Context
import android.media.SoundPool

/**
 * Sound Pool extension that included loading sound and playing sounds.
 *
 * @author Rick
 */
class MySoundPool @Suppress("deprecation") constructor(
    var context: Context,
    maxStreams: Int,
    streamType: Int,
    srcQuality: Int
) : SoundPool(maxStreams, streamType, srcQuality), SoundPool.OnLoadCompleteListener {

    private val TAP = 1
    private val CHIME = 2
    private var commonVariables: CommonVariables = CommonVariables.getInstance()

    /**
     * Check for sound file to be loaded and wanting to be player
     */
    fun playChimeSound() {
        if (commonVariables.chimeLoaded && commonVariables.playChimeSound)
            play(commonVariables.saveSound, commonVariables.volume, commonVariables.volume, 1, 0, 1f)
    }

    /**
     * Check for tap sound to be loaded and it in preferences
     */
    fun playSetSound() {
        if (commonVariables.tapLoaded && commonVariables.playTapSound)
            play(commonVariables.tapSound, commonVariables.volume, commonVariables.volume, 1, 0, 1f)
    }

    /**
     * Load the sound pool sounds.
     */
    fun init() {
        setOnLoadCompleteListener(this)
        commonVariables.saveSound = load(context, R.raw.imagesaved, 1)
        commonVariables.tapSound = load(context, R.raw.tap, 1)
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
            commonVariables.tapLoaded = true
        else if (sampleId == CHIME)
            commonVariables.chimeLoaded = true
    }
}