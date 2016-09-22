package com.inventory.kp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by abhilasha.jain on 9/2/2016.
 */
public class FileUtils {

    private static File mStorageDir = null;

    public static ArrayList<String> getRealPathFromURI(Context context, ArrayList<Uri> uris) {
        ArrayList<String> filePaths = new ArrayList<>();
        String[] ids = new String[uris.size()];
        StringBuilder whereClause = new StringBuilder(MediaStore.Images.Media._ID + " IN (?");
        for (int i = 0; i<uris.size(); i++) {
            String path = uris.get(i).getPath();
            // Split at colon, use second item in the array
            ids[i] = path.split(":")[1];
            whereClause.append(",?");
        }
        whereClause.append(")");
        String[] column = { MediaStore.Images.Media.DATA };
        Log.d("abhi", "ids : " + Arrays.toString(ids));
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, whereClause.toString(), ids, null);

        int columnIndex = cursor.getColumnIndex(column[0]);
        while (cursor.moveToNext()) {
            filePaths.add(cursor.getString(columnIndex));
        }
        cursor.close();
        return filePaths;
    }

    public static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "image_" + timeStamp + ".jpeg";
        createDirectoryIFNeeded();
        File image = new File(mStorageDir, imageFileName);
        Log.d("abhi", "mStrorageDir : " + mStorageDir);
        return image;
    }

    private static void createDirectoryIFNeeded() {
        if (mStorageDir == null) {
            Log.d("abhi", "mStrorageDir is null ");
            mStorageDir = new File(Environment.getExternalStorageDirectory().toString() + "/KP");
            Log.d("abhi", "mStrorageDir : " + mStorageDir);
            if (!mStorageDir.exists()) {
                mStorageDir.mkdirs();
            }
        }
    }

    public static String getStorageFileCSV() {
        createDirectoryIFNeeded();
        return (mStorageDir.toString() + "/book_details.csv");
    }

}
