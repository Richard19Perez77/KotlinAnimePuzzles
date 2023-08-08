package com.compose.myapplication

import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import java.io.IOException

/**
 * A class to extend Media Player and implement handling interfaces. I also
 * started implementing the ability to handle the sound changes due to incoming
 * notification sounds like phone or message alerts *
 *
 * @author Rick
 */
class MyMediaPlayer(private var c: Context?) : OnPreparedListener, MediaPlayer.OnErrorListener,
    OnAudioFocusChangeListener, OnCompletionListener {

    private var mediaPlayer: MediaPlayer?
    private var path: Uri = Uri.parse(c?.getString(R.string.PATH) + Data.TRACK_01)
    private var am: AudioManager?
    private var result = 0
    private var currentVolume = 0f

    /**
     * Used in testing to tell if the headphones were unplugged or not
     */
    private var volumeSet = false
    private var currentState: State? = null

    fun togglePause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            CommonVariables.currentSoundPosition = mediaPlayer?.currentPosition ?: 0
            currentState = State.Paused
        } else {
            init()
            start()
        }
    }

    enum class State {
        Idle, Initialized, Prepared, Started, Preparing, Stopped, Paused, End, Error, PlaybackCompleted
    }

    init {
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setOnPreparedListener(this)
        mediaPlayer?.setOnErrorListener(this)
        mediaPlayer?.setOnCompletionListener(this)
        am = c?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    /**
     * Initialize a new media player and audio manager instance. Set state to Idle.
     */
    fun init() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setOnPreparedListener(this)
            mediaPlayer?.setOnErrorListener(this)
            mediaPlayer?.setOnCompletionListener(this)
        }
        mediaPlayer?.reset()
        currentState = State.Idle
    }

    /**
     * Begin playing music by getting the track and volume prepared and calling the asych prepare method. Set state to Preparing.
     */
    private fun start() {
        if (CommonVariables.playMusic) {
            result = am?.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) ?: 0

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                if (currentState != State.Idle && currentState != State.Preparing) init()
                if (currentState != State.Preparing) try {
                    c?.let { mediaPlayer?.setDataSource(it, path) }
                    currentState = State.Initialized
                    mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
                    mediaPlayer?.setVolume(1f, 1f)
                    // currentVolume = CommonVariables.volume
                    mediaPlayer?.isLooping = true
                    mediaPlayer?.prepareAsync()
                    currentState = State.Preparing
                } catch (ignored: IllegalArgumentException) {
                } catch (ignored: IOException) {
                } catch (ignored: SecurityException) {
                } catch (ignored: IllegalStateException) {
                }
            }
        }
    }

    /**
     * Callback for when the media player is ready to play. On play flag start and seek to current position. Set state to Started.
     *
     * @param player
     */
    override fun onPrepared(player: MediaPlayer) {
        // check for option to play music and resume last position
        if (currentState == State.Preparing) {
            currentState = State.Prepared
            if (CommonVariables.playMusic) {
                if (CommonVariables.currentSoundPosition > 0) {
                    mediaPlayer?.seekTo(CommonVariables.currentSoundPosition)
                }
                if (currentState != State.End && !player.isPlaying) {
                    player.start()
                    currentState = State.Started
                }
            }
        }
    }

    /**
     * On media player error set the state to Error. Reset the media player. Start the media player over.
     *
     * @param mediaPlayer
     * @param i
     * @param i2
     * @return
     */
    override fun onError(mediaPlayer: MediaPlayer, i: Int, i2: Int): Boolean {
        currentState = State.Error
        mediaPlayer.reset()
        start()
        return true
    }

    /**
     * Thi can be temporary or for a long time due to different events, handle them by starting the media player, quieting it, or pausing it.
     *
     * @param focusChange
     */
    override fun onAudioFocusChange(focusChange: Int) {
        // Handle audio lowering and raising for other phone sounds
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN ->                 // resume play back
                if (mediaPlayer == null) init() else if (mediaPlayer?.isPlaying == false) {
                    start()
                } else {
                    mediaPlayer?.setVolume(1f,1f)
                }

            AudioManager.AUDIOFOCUS_LOSS ->                 // lost focus for an unbounded amount of time. stop and release
                pause()

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->                 // lost focus for a short time, but we have to stop play back.
                if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                    currentState = State.Paused
                    CommonVariables.currentSoundPosition = mediaPlayer?.currentPosition ?: 0
                }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> if (mediaPlayer != null) {
                setNewVolume(0.1f)
            }
        }
    }

    /**
     * Reinitialize and restart the media player.
     */
    fun resume() {
        init()
        start()
    }

    /**
     * Will allow for the media player to release focus and play music from phone if playing.
     */
    fun abandonFocus() {
        if (am != null) am?.abandonAudioFocus(this)
    }

    /**
     * Release focus of sound and save current sound position. Set player state to End.
     */
    fun pause() {
        abandonFocus()
        if (currentState != State.End && mediaPlayer != null) {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                currentState = State.Paused
                CommonVariables.currentSoundPosition = mediaPlayer?.currentPosition ?: 0
            }
            if (currentState == State.Started || currentState == State.Paused) {
                mediaPlayer?.stop()
                currentState = State.Stopped
                mediaPlayer?.release()
                currentState = State.End
                mediaPlayer = null
            }
        }
    }

    /**
     * Sets a new playing volume for the media player.
     *
     * @param setVolume
     */
    fun setNewVolume(setVolume: Float) {
        if (currentState != State.End && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.setVolume(setVolume, setVolume)
            currentVolume = setVolume
            volumeSet = true
        }
    }

    /**
     * Release memory associated with the media player.
     */
    fun cleanUp() {
        mediaPlayer?.release()
        currentState = State.End
        mediaPlayer = null
    }

    /**
     * On completion, restart the player. Set state to PlaybackCompleted
     *
     * @param mp
     */
    override fun onCompletion(mp: MediaPlayer) {
        currentState = State.PlaybackCompleted
        CommonVariables.currentSoundPosition = 0
        start()
    }

    /**
     * If playing, stop the media player, record position of sound and set state to Paused.
     */
    fun onStop() {
        if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            currentState = State.Paused
            CommonVariables.currentSoundPosition = mediaPlayer?.currentPosition ?: 0
        }
    }
}