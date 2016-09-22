package com.inventory.kp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;

import com.inventory.kp.dialogs.DatePickerDialogFragment;
import com.inventory.kp.dialogs.TimePickerDialogFragment;
import com.inventory.kp.views.ImagesAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class PostingActivity extends AppCompatActivity
        implements DatePickerDialogFragment.DatePickerDialogListener, TimePickerDialogFragment.TimePickerDialogListener {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int SELECT_IMAGE_ACTIVITY_REQUEST_CODE = 200;
    private static final int MY_PERMISSIONS_REQUEST_READ_WRITE_EXTERNAL_STORAGE = 110;
    private static final String CSV_SEPARATOR = ",";
    private static final String PERMISSION_REQUESTED_HISTORY = "permission_requested_history";
    private GregorianCalendar mCalender;
    private boolean mStoragePermission = false;
    private ArrayList<String> mImagesList = new ArrayList<>();
    private ImagesAdapter mImagesAdapter;
    private GridView mImagesGridView;
    private EditText mBookName;
    private EditText mBookDescription;
    private EditText mBookCost;
    private Spinner mBookCover;
    private Spinner mBookType;
    private Button mDateSelectionButton;
    private Button mTimeSelectionButton;
    private CoordinatorLayout mCoordinatorLayout;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences pref = getSharedPreferences(PERMISSION_REQUESTED_HISTORY, MODE_PRIVATE);
        Boolean permissionRequested = pref.getBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (!permissionRequested || (permissionRequested && shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_WRITE_EXTERNAL_STORAGE);
            } else {
                mStoragePermission = false;
            }
        } else {
            mStoragePermission = true;
        }
        setupViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_posting, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            mImagesList.add(mCurrentPhotoPath);
            mImagesAdapter.addNewImage(mCurrentPhotoPath);
            //MediaScannerConnection.scanFile(this, new String[]{mCurrentPhotoPath}, new String[]{"image/jpeg"}, null);
        } else if (requestCode == SELECT_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Log.d("abhi", "clipdata : " + data.getClipData());
            Log.d("abhi", "data.getData() : " + data.getData());
            ArrayList<Uri> uris = new ArrayList<>();
            if (data.getData() != null) {
                uris.add(data.getData());
            } else if (data.getClipData() != null) {
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    uris.add(clipData.getItemAt(i).getUri());
                }
            }
            Log.d("abhi", "UriPaths : " + uris);
            if (!uris.isEmpty()) {
                ArrayList<String> realPaths = FileUtils.getRealPathFromURI(this, uris);
                Log.d("abhi", "realPaths : " + realPaths);
                for (int i = 0; i < realPaths.size(); i++) {
                    mImagesList.add(realPaths.get(i));
                    mImagesAdapter.addNewImage(realPaths.get(i));
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_WRITE_EXTERNAL_STORAGE: {
                SharedPreferences.Editor pref_edit = getSharedPreferences(PERMISSION_REQUESTED_HISTORY, MODE_PRIVATE).edit();
                pref_edit.putBoolean(permissions[0], true);
                pref_edit.commit();
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mStoragePermission = true;
                } else {
                    showMessageToEnablePermission(getString(R.string.message_permission_denied));
                    mStoragePermission = false;
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_send) {
            if (checkEmptyMandatoryFields()) {
                return true;
            }
            if (mImagesList.size() == 0) {
                showEmptyImagesListDialog();
                return true;
            }
            writeBookDataToFile();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showMessageToEnablePermission(String message) {
        new AlertDialog.Builder(PostingActivity.this)
                .setMessage(message)
                .setPositiveButton(R.string.message_dialog_OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    boolean checkEmptyMandatoryFields() {
        boolean hasEmptyField = false;
        if (TextUtils.isEmpty(mBookName.getText())) {
            mBookName.setError(getString(R.string.book_name_empty_error_message));
            hasEmptyField = true;
        }
        if (TextUtils.isEmpty(mBookDescription.getText())) {
            mBookDescription.setError(getString(R.string.book_description_empty_error_message));
            hasEmptyField = true;
        }
        if (TextUtils.isEmpty(mBookCost.getText())) {
            mBookCost.setError(getString(R.string.book_cost_empty_error_message));
            hasEmptyField = true;
        }
        return hasEmptyField;
    }

    private void setupViews() {
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mImagesGridView = (GridView) findViewById(R.id.images_grid);
        mDateSelectionButton = (Button) findViewById(R.id.date_view);
        mTimeSelectionButton = (Button) findViewById(R.id.time_view);
        mBookName = (EditText) findViewById(R.id.book_name);
        mBookDescription = (EditText) findViewById(R.id.book_description);
        mBookCost = (EditText) findViewById(R.id.book_cost);
        mBookCover = (Spinner) findViewById(R.id.book_cover);
        mBookType = (Spinner) findViewById(R.id.book_type);
        mCalender = new GregorianCalendar();
        String dateStamp = new SimpleDateFormat("dd-MM-yyyy").format(mCalender.getTime());
        String timeStamp = new SimpleDateFormat("hh:mm aa").format(mCalender.getTime());
        mDateSelectionButton.setText(dateStamp);
        mTimeSelectionButton.setText(timeStamp);
        mImagesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == (mImagesList.size())) {
                    if (mStoragePermission) {
                        showSelectImageOptionsDialog();
                    } else {
                        showMessageToEnablePermission(getString(R.string.message_permission_denied));
                    }

                }
            }
        });
        mImagesAdapter = new ImagesAdapter(this);
        mImagesGridView.setAdapter(mImagesAdapter);
    }

    private void showSelectImageOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_photo_options_title);
        builder.setItems(R.array.select_photo_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        File photoFile = null;
                        try {
                            photoFile = FileUtils.createImageFile();
                            mCurrentPhotoPath = photoFile.getAbsolutePath();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        if (photoFile != null) {
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                            startActivityForResult(takePictureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                        }
                    }
                } else if (item == 1) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE_ACTIVITY_REQUEST_CODE);
                }
            }
        });
        builder.show();
    }

    private void showEmptyImagesListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.no_images_dialog_title);
        builder.setMessage(R.string.no_images_dialog_message);
        builder.setPositiveButton(R.string.no_images_dialog_continue, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                writeBookDataToFile();
            }
        });
        builder.setNegativeButton(R.string.no_images_dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    public void showDatePickerDialog(View v) {
        new DatePickerDialogFragment().show(getFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog(View v) {
        new TimePickerDialogFragment().show(getFragmentManager(), "timePicker");
    }

    private void writeBookDataToFile() {
        BookData bookDetails = new BookData();
        bookDetails.setBookName(mBookName.getText().toString());
        bookDetails.setBookDescription(mBookDescription.getText().toString());
        bookDetails.setCost(Integer.parseInt(mBookCost.getText().toString()));
        bookDetails.setBookCover(mBookCover.getSelectedItem().toString());
        bookDetails.setBookType(mBookType.getSelectedItem().toString());
        bookDetails.setDate(mDateSelectionButton.getText().toString());
        bookDetails.setTime(mTimeSelectionButton.getText().toString());
        bookDetails.setImagesPathList(mImagesList);
        File dataFile = new File(FileUtils.getStorageFileCSV());
        try {
            FileOutputStream outputStream = new FileOutputStream(dataFile, true);
            OutputStreamWriter outWriter = new OutputStreamWriter(outputStream);
            outWriter.write(bookDetails.writeToFileFormat(CSV_SEPARATOR));
            outWriter.flush();
            outWriter.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(dataFile)));
        Snackbar.make(mCoordinatorLayout, R.string.message_data_successful, Snackbar.LENGTH_LONG);
    }

    @Override
    public void onDateSet(int year, int month, int date) {
        mCalender.set(year, month, date);
        String dateStamp = new SimpleDateFormat("dd-MM-yyyy").format(mCalender.getTime());
        mDateSelectionButton.setText(dateStamp);
    }

    @Override
    public void onTimeSet(int hour, int minute) {
        mCalender.set(Calendar.HOUR, hour);
        mCalender.set(Calendar.MINUTE, minute);
        String dateStamp = new SimpleDateFormat("dd-MM-yyyy").format(mCalender.getTime());
        mDateSelectionButton.setText(dateStamp);
    }

}
