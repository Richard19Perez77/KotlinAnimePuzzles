package com.compose.myapplication;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;

/**
 * A class to extend Media Player and implement handling interfaces. I also
 * started implementing the ability to handle the sound changes due to incoming
 * notification sounds like phone or message alerts *
 *
 * @author Rick
 */
public class MyMediaPlayer implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnCompletionListener {

    Context context;
    CommonVariables cv = CommonVariables.getInstance();
    public MediaPlayer mediaPlayer;
    Uri path;
    AudioManager am;
    int result;
    public float currentVolume = 0f;
    /**
     * Used in testing to tell if the headphones were unplugged or not
     */
    public boolean volumeSet = false;
    public State currentState;

    public void togglePause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            cv.currentSoundPosition = mediaPlayer.getCurrentPosition();
            currentState = State.Paused;
        } else {
            init();
            start();
        }
    }

    public enum State {
        Idle, Initialized, Prepared, Started, Preparing, Stopped, Paused, End, Error, PlaybackCompleted
    }

    public MyMediaPlayer(Context context) {
        this.context = context;
        path = Uri.parse(context.getString(R.string.PATH) + Data.TRACK_01);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);
        am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * Initialize a new media player and audio manager instance. Set state to Idle.
     */
    public void init() {
        if (mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnCompletionListener(this);
        }
        mediaPlayer.reset();
        currentState = State.Idle;
    }

    /**
     * Begin playing music by getting the track and volume prepared and calling the asych prepare method. Set state to Preparing.
     */
    public void start() {
        if (cv.playMusic) {
            result = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                if (currentState != State.Idle && currentState != State.Preparing)
                    init();
                if (currentState != State.Preparing) try {
                    mediaPlayer.setDataSource(context, path);
                    currentState = State.Initialized;
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setVolume(cv.volume, cv.volume);
                    currentVolume = cv.volume;
                    mediaPlayer.setLooping(true);
                    mediaPlayer.prepareAsync();
                    currentState = State.Preparing;
                } catch (IllegalArgumentException | IOException | SecurityException |
                         IllegalStateException ignored) {
                }
            }
        }
    }

    /**
     * Callback for when the media player is ready to play. On play flag start and seek to current position. Set state to Started.
     *
     * @param player
     */
    @Override
    public void onPrepared(MediaPlayer player) {
        // check for option to play music and resume last position
        if (currentState == State.Preparing) {
            currentState = State.Prepared;
            if (cv.playMusic) {
                if (cv.currentSoundPosition > 0) {
                    mediaPlayer.seekTo(cv.currentSoundPosition);
                }
                if (currentState != State.End && !player.isPlaying()) {
                    player.start();
                    currentState = State.Started;
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
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
        currentState = State.Error;
        mediaPlayer.reset();
        start();
        return true;
    }

    /**
     * Thi can be temporary or for a long time due to different events, handle them by starting the media player, quieting it, or pausing it.
     *
     * @param focusChange
     */
    @Override
    public void onAudioFocusChange(int focusChange) {
        // Handle audio lowering and raising for other phone sounds
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume play back
                if (mediaPlayer == null)
                    init();
                else if (!mediaPlayer.isPlaying()) {
                    start();
                } else {
                    mediaPlayer.setVolume(cv.volume, cv.volume);
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // lost focus for an unbounded amount of time. stop and release
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // lost focus for a short time, but we have to stop play back.
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    currentState = State.Paused;
                    cv.currentSoundPosition = mediaPlayer.getCurrentPosition();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer != null) {
                    setNewVolume(0.1f);
                }
                break;
        }
    }

    /**
     * Reinitialize and restart the media player.
     */
    public void resume() {
        init();
        start();
    }

    /**
     * Will allow for the media player to release focus and play music from phone if playing.
     */
    public void abandonFocus() {
        if (am != null)
            am.abandonAudioFocus(this);
    }

    /**
     * Release focus of sound and save current sound position. Set player state to End.
     */
    public void pause() {
        abandonFocus();
        if (currentState != State.End && mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                currentState = State.Paused;
                cv.currentSoundPosition = mediaPlayer.getCurrentPosition();
            }
            if (currentState == State.Started || currentState == State.Paused) {
                mediaPlayer.stop();
                currentState = State.Stopped;
                mediaPlayer.release();
                currentState = State.End;
                mediaPlayer = null;
            }
        }
    }

    /**
     * Sets a new playing volume for the media player.
     *
     * @param setVolume
     */
    public void setNewVolume(Float setVolume) {
        if (currentState != State.End && mediaPlayer.isPlaying()) {
            mediaPlayer.setVolume(setVolume, setVolume);
            currentVolume = setVolume;
            volumeSet = true;
        }
    }

    /**
     * Release memory associated with the media player.
     */
    public void cleanUp() {
        mediaPlayer.release();
        currentState = State.End;
        mediaPlayer = null;
    }

    /**
     * On completion, restart the player. Set state to PlaybackCompleted
     *
     * @param mp
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        currentState = State.PlaybackCompleted;
        cv.currentSoundPosition = 0;
        start();
    }

    /**
     * If playing, stop the media player, record position of sound and set state to Paused.
     */
    public void onStop() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            currentState = State.Paused;
            cv.currentSoundPosition = mediaPlayer.getCurrentPosition();
        }
    }
}