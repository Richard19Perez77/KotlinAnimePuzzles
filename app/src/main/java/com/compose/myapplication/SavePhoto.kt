package com.compose.myapplication

import android.app.Activity
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * The save photo class allows for the user to save a high resolution version of
 * the puzzle they just solved.
 *
 * @author Rick
 */
class SavePhoto(context: Context?, var currentImageToSave: Int) {

    init {
        val activity = context as Activity
        activity.runOnUiThread(object : Runnable {
            private var mExternalStorageAvailable = false
            private var mExternalStorageWriteable = false
            fun showToast(message: String?) {
                // Create and show toast for save photo
                val toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT)
                toast.show()
            }

            override fun run() {
                // save current image to devices images folder
                val state = Environment.getExternalStorageState()
                // check if writing is an option
                if (Environment.MEDIA_MOUNTED == state) {
                    // We can read and write the media
                    mExternalStorageWriteable = true
                    mExternalStorageAvailable = mExternalStorageWriteable
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
                    mExternalStorageAvailable = mExternalStorageWriteable
                }
                if (mExternalStorageAvailable && mExternalStorageWriteable) {
                    // then write picture to phone
                    val path = Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val name =
                        (CommonVariables.data.artworks[currentImageToSave].titleOfArtist + "_"
                                + CommonVariables.data.artworks[currentImageToSave].titleOfArtwork + ".jpeg")
                    val file = File(path, name)

                    // check for file in directory
                    if (file.exists()) {
                        showToast("Photo Exists Already!")
                    } else {
                        try {
                            val b1 = path.mkdirs()
                            val b2 = path.exists()
                            // Make sure the Pictures directory exists.
                            if (b1 || b2) {

                                // get file into input stream
                                val `is` = activity.resources.openRawResource(
                                    CommonVariables.data.artworks[currentImageToSave].imageID
                                )
                                val os: OutputStream = FileOutputStream(file)
                                val data = ByteArray(`is`.available())
                                `is`.read(data)
                                os.write(data)
                                `is`.close()
                                os.close()
                                CommonVariables.imagesSaved++
                                val act = activity as MainActivity
                                act.updatePuzzleStats()
                                showToast("Image Saved!")
                                MediaScannerConnection
                                    .scanFile(
                                        context, arrayOf(file.toString()),
                                        null
                                    ) { path1: String?, uri: Uri? -> }
                            } else {
                                showToast("Could not make/access directory.")
                            }
                        } catch (e: IOException) {
                            showToast("Error making/accessing directory.")
                        }
                    }
                } else {
                    showToast("Directory not available/writable.")
                }
            }
        })
    }
}