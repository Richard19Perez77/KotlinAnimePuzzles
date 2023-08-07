package com.compose.myapplication

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
class SavePhoto(private var c: Context?, private var currentImageToSave: Int) {

    suspend fun run(): FirstFragment.Companion.PHOTO_RESULT = withContext(Dispatchers.IO) {
        var success: FirstFragment.Companion.PHOTO_RESULT

        try {

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
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val name =
                    (CommonVariables.data.artworks[currentImageToSave].titleOfArtist + "_"
                            + CommonVariables.data.artworks[currentImageToSave].titleOfArtwork + ".jpeg")
                val file = File(path, name)

                // check for file in directory
                if (file.exists()) {
                    success = FirstFragment.Companion.PHOTO_RESULT.EXISTS
                } else {
                    try {
                        val b1 = path.mkdirs()
                        val b2 = path.exists()
                        // Make sure the Pictures directory exists.
                        if (b1 || b2) {

                            // get file into input stream
                            val `is` = c?.resources?.openRawResource(
                                CommonVariables.data.artworks[currentImageToSave].imageID
                            )
                            val os: OutputStream = FileOutputStream(file)
                            val data = `is`?.let { ByteArray(it.available()) }
                            `is`?.read(data)
                            os.write(data)
                            `is`?.close()
                            withContext(Dispatchers.IO) {
                                os.close()
                            }
                            CommonVariables.imagesSaved++
                            val act = c as MainActivity
                            act.updatePuzzleStats()
                            MediaScannerConnection
                                .scanFile(
                                    c, arrayOf(file.toString()),
                                    null
                                ) { _: String?, _: Uri? -> }
                            success = FirstFragment.Companion.PHOTO_RESULT.SAVED
                        } else {
                            success = FirstFragment.Companion.PHOTO_RESULT.ERROR
                        }
                    } catch (e: IOException) {
                        success = FirstFragment.Companion.PHOTO_RESULT.ERROR
                    }
                }
            } else {
                success = FirstFragment.Companion.PHOTO_RESULT.ERROR
            }
        } catch (_: Exception) {
            success = FirstFragment.Companion.PHOTO_RESULT.ERROR
        }
        return@withContext success
    }
}