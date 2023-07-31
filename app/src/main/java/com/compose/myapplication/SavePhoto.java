package com.compose.myapplication;

import android.app.Activity;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The save photo class allows for the user to save a high resolution version of
 * the puzzle they just solved.
 *
 * @author Rick
 */
public class SavePhoto {

    CommonVariables commonVariables = CommonVariables.getInstance();
    int currentImageToSave;
    Toast toast = null;

    public SavePhoto(Context context, final int currentImage) {
        currentImageToSave = currentImage;

        final Activity activity = (Activity) context;
        activity.runOnUiThread(new Runnable() {

            private boolean mExternalStorageAvailable = false;
            private boolean mExternalStorageWriteable = false;

            public void showToast(String message) {
                // Create and show toast for save photo
                toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
                toast.show();
            }

            @Override
            public void run() {
                // save current image to devices images folder
                String state = Environment.getExternalStorageState();
                // check if writing is an option
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    // We can read and write the media
                    mExternalStorageAvailable = mExternalStorageWriteable = true;
                } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                    // We can only read the media
                    mExternalStorageAvailable = true;
                    mExternalStorageWriteable = false;
                } else {
                    // Something else is wrong. It may be one of many other
                    // states, but
                    // all we need
                    // to know is we can neither read nor write
                    mExternalStorageAvailable = mExternalStorageWriteable = false;
                }

                if (mExternalStorageAvailable && mExternalStorageWriteable) {
                    // then write picture to phone
                    File path = Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

                    final String name = commonVariables.data.artworks[currentImageToSave].titleOfArtist + "_"
                            + commonVariables.data.artworks[currentImageToSave].titleOfArtwork + ".jpeg";

                    File file = new File(path, name);

                    // check for file in directory
                    if (file.exists()) {
                        showToast("Photo Exists Already!");
                    } else {
                        try {
                            boolean b1 = path.mkdirs();
                            boolean b2 = path.exists();
                            // Make sure the Pictures directory exists.
                            if (b1 || b2) {

                                // get file into input stream
                                InputStream is = activity.getResources().openRawResource(
                                        commonVariables.data.artworks[currentImageToSave].imageID);

                                OutputStream os = new FileOutputStream(file);
                                byte[] data = new byte[is.available()];
                                is.read(data);
                                os.write(data);
                                is.close();
                                os.close();

                                commonVariables.imagesSaved++;
                                MainActivity act = (MainActivity) activity;
                                act.updatePuzzleStats();

                                showToast("Image Saved!");

                                MediaScannerConnection
                                        .scanFile(
                                                context,
                                                new String[]{file.toString()},
                                                null,
                                                (path1, uri) -> {
                                                    // new image is in phone
                                                    // database now for use

                                                });
                            } else {
                                showToast("Could not make/access directory.");
                            }
                        } catch (IOException e) {
                            showToast("Error making/accessing directory.");
                        }
                    }
                } else {
                    showToast("Directory not available/writable.");
                }
            }
        });
    }
}
