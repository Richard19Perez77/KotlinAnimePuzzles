package com.compose.myapplication;

import android.app.IntentService;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Save a music track in the background and then update the UI if the user is in the stats
 * Fragment. The user is messaged in error conditions. The return activity can handle the result of
 * saving.
 */
public class SaveMusicService extends IntentService {

    public final static String MUSIC_SAVED = "MUSIC_SAVED";

    public SaveMusicService() {
        super("SaveMusicService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            saveMusicTrack();
        }
    }

    private void serviceToast(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void serviceUpdateUI() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MUSIC_SAVED);
                sendBroadcast(intent);
            }
        });
    }

    private void saveMusicTrack() {
        boolean mExternalStorageAvailable;
        boolean mExternalStorageWriteable;

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
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

            final String name = "signal.mp3";

            File file = new File(path, name);

            // check for file in directory
            if (file.exists()) {
                serviceToast("signal.mp3 already saved!");
            } else {
                try {
                    boolean b1 = path.mkdirs();
                    boolean b2 = path.exists();
                    // Make sure the Pictures directory exists.

                    if (b1 || b2) {
                        InputStream is = getApplicationContext().getResources().openRawResource(
                                Data.TRACK_01);

                        OutputStream os = new FileOutputStream(file);
                        byte[] data = new byte[is.available()];
                        is.read(data);
                        os.write(data);
                        is.close();
                        os.close();

                        serviceToast("Track Saved!");

                        CommonVariables.musicSaved++;
                        serviceUpdateUI();

                        MediaScannerConnection
                                .scanFile(
                                        getApplicationContext(),
                                        new String[]{file.toString()},
                                        null,
                                        new MediaScannerConnection.OnScanCompletedListener() {
                                            @Override
                                            public void onScanCompleted(
                                                    String path, Uri uri) {
                                            }
                                        });
                    } else {
                        serviceToast("Could not make/access directory.");
                    }
                } catch (IOException e) {
                    serviceToast("ERROR making/accessing directory.");
                }
            }
        } else {
            serviceToast("Directory not available/writable.");
        }
    }
}