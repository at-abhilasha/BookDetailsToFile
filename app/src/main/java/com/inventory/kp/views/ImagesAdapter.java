package com.inventory.kp.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.inventory.kp.R;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by abhilasha.jain on 12/23/2015.
 */
public class ImagesAdapter extends BaseAdapter {

    private ArrayList<Bitmap> mImagesList;
    private Context mContext;

    public ImagesAdapter(Context context) {
        mContext = context;
        mImagesList = new ArrayList<>();
        Bitmap add_icon = BitmapFactory.decodeResource(mContext.getResources(), android.R.drawable.ic_input_add);
        mImagesList.add(add_icon);
    }

    @Override
    public int getCount() {
        return mImagesList.size();
    }

    @Override
    public Bitmap getItem(int position) {
        return mImagesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new SquareImageView(mContext);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageBitmap(mImagesList.get(position));
        return imageView;
    }

    public void addNewImage(String newImagePath) {
        Bitmap imageBitmap = getScaledImage(newImagePath);
        imageBitmap = rotateBitmapIfRequired(imageBitmap, newImagePath);
        mImagesList.add(mImagesList.size() - 1, imageBitmap);
        notifyDataSetChanged();
    }

    private Bitmap getScaledImage(String imagePath) {
        int targetW = (int)mContext.getResources().getDimension(R.dimen.grid_item_width);
        int targetH = (int)mContext.getResources().getDimension(R.dimen.grid_item_width);

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        Log.d("abhi", "photoWidth : " + photoW + "photoHeight" + photoH);
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeFile(imagePath, bmOptions);
    }

    Bitmap rotateBitmapIfRequired(Bitmap image, String imagePath) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }

            } else {
                Log.d("abhi", "orientation is -1");
            }
        }
        Matrix m = new Matrix();
        m.postRotate(degree);
        Bitmap rotatedImage = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), m, true);
        image = null;
        return rotatedImage;
    }

}
