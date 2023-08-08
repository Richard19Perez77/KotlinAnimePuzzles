package com.compose.myapplication

import android.content.Context
import android.media.SoundPool

/**
 * Sound Pool extension that included loading sound and playing sounds.
 *
 * @author Rick
 */
class MySoundPool @Suppress("deprecation") constructor(
    maxStreams: Int,
    streamType: Int,
    srcQuality: Int
) : SoundPool(maxStreams, streamType, srcQuality), SoundPool.OnLoadCompleteListener {

    private var chimeLoaded: Boolean = false
    private var tapLoaded: Boolean = false
    private var tapSound: Int = 0
    private var saveSound: Int = 0
    private val TAP = 1
    private val CHIME = 2

    /**
     * Check for sound file to be loaded and wanting to be player
     */
    fun playChimeSound() {
        if (chimeLoaded && CommonVariables.playChimeSound)
            play(saveSound, 1f, 1f, 1, 0, 1f)
    }

    /**
     * Check for tap sound to be loaded and it in preferences
     */
    fun playSetSound() {
        if (tapLoaded && CommonVariables.playTapSound)
            play(tapSound, 1f, 1f, 1, 0, 1f)
    }

    /**
     * Load the sound pool sounds.
     */
    fun init() {
        setOnLoadCompleteListener(this)
    }

    /***
     *  Load the sounds to the sound pool
     */
    fun load(c: Context?) {
        saveSound = load(c, R.raw.imagesaved, 1)
        tapSound = load(c, R.raw.tap, 1)
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
            tapLoaded = true
        else if (sampleId == CHIME)
            chimeLoaded = true
    }
}