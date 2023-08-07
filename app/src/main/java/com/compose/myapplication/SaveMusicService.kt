package com.compose.myapplication

import android.app.IntentService
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * Save a music track in the background and then update the UI if the user is in the stats
 * Fragment. The user is messaged in error conditions. The return activity can handle the result of
 * saving.
 */
class SaveMusicService : IntentService("SaveMusicService") {
    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            saveMusicTrack()
        }
    }

    private fun serviceToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(
                applicationContext, message, Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun serviceUpdateUI() {
        Handler(Looper.getMainLooper()).post {
            val intent = Intent(MUSIC_SAVED)
            sendBroadcast(intent)
        }
    }

    private fun saveMusicTrack() {
        val mExternalStorageAvailable: Boolean
        val mExternalStorageWriteable: Boolean

        // save current image to devices images folder
        val state = Environment.getExternalStorageState()
        // check if writing is an option
        if (Environment.MEDIA_MOUNTED == state) {
            // We can read and write the media
            mExternalStorageWriteable = true
            mExternalStorageAvailable = true
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY == state) {
            // We can only read the media
            mExternalStorageAvailable = true
            mExternalStorageWriteable = false
        } else {
            // Something else is wrong. It may be one of many other
            // states, but
            // all we need
            // to know is we can neither read nor write
            mExternalStorageWriteable = false
            mExternalStorageAvailable = false
        }
        if (mExternalStorageAvailable && mExternalStorageWriteable) {

            // then write picture to phone
            val path = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            val name = "signal.mp3"
            val file = File(path, name)

            // check for file in directory
            if (file.exists()) {
                serviceToast("signal.mp3 already saved!")
            } else {
                try {
                    val b1 = path.mkdirs()
                    val b2 = path.exists()
                    // Make sure the Pictures directory exists.
                    if (b1 || b2) {
                        val `is` = applicationContext.resources.openRawResource(
                            Data.TRACK_01
                        )
                        val os: OutputStream = FileOutputStream(file)
                        val data = ByteArray(`is`.available())
                        `is`.read(data)
                        os.write(data)
                        `is`.close()
                        os.close()
                        serviceToast("Track Saved!")
                        CommonVariables.musicSaved++
                        serviceUpdateUI()
                        MediaScannerConnection
                            .scanFile(
                                applicationContext, arrayOf(file.toString()),
                                null
                            ) { _, _ -> }
                    } else {
                        serviceToast("Could not make/access directory.")
                    }
                } catch (e: IOException) {
                    serviceToast("ERROR making/accessing directory.")
                }
            }
        } else {
            serviceToast("Directory not available/writable.")
        }
    }

    companion object {
        const val MUSIC_SAVED = "MUSIC_SAVED"
    }
}