package com.example.link.photogallery;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainPhotoView extends AppCompatActivity {

    final private int REQUEST_PERMISSION_CODE = 123;
    final private int CAMERA_REQUEST_PERMISSION_CODE = 110;

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private Uri fileUri;

    Toolbar toolBar;
    GridView gridView;

    ArrayList<String> imageList;

    Context context;
    Bundle myOriginalMemoryBundle;
    SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("sdsadsadsadasdasdasd");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_photo_view);

        context = this;
        myOriginalMemoryBundle = savedInstanceState;

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshlayout);

        initToolbar();

        int version = Build.VERSION.SDK_INT;
        System.out.println(version);

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED)
        {
            initImageGridView();
        }
        else
        {
            sendRequestPermission();
        }

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initImageGridView();
            }
        });
    }

    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }


    private void initImageGridView() {
        imageList = getImageFromStorage(new File(Environment.getExternalStorageDirectory().toString()));
        //toolBar.setTitle(String.valueOf(imageList.size()));

        TextView tw_noimage = (TextView) findViewById(R.id.tw_noImage);
        if (imageList.size() > 0)
            tw_noimage.setVisibility(View.GONE);
        else
            tw_noimage.setVisibility(View.VISIBLE);

        gridView = (GridView) findViewById(R.id.gridview);
        gridView.setAdapter(new ImageAdapter(this, imageList));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                Intent openSingleImage = new Intent(MainPhotoView.this, SinglePhotoView.class);
                openSingleImage.putExtra(getResources().getString(R.string.intent_position), pos);
                openSingleImage.putStringArrayListExtra(getResources().getString(R.string.intent_image_list), imageList);
                startActivityForResult(openSingleImage, 1);
            }

        });
        refreshLayout.setRefreshing(false);
    }

    private void sendRequestPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_CODE);

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_CODE);
            }
        }
    }

    public void sendCameraRequestPermission() {

            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_PERMISSION_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_PERMISSION_CODE);
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    initImageGridView();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    finish();
                }
                break;
            }

            case CAMERA_REQUEST_PERMISSION_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCameraIntent();
                }
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void initToolbar()
    {
        toolBar = (Toolbar) findViewById(R.id.toolbar);
        toolBar.setLogo(R.drawable.ic_logo);
        toolBar.setTitle(R.string.title);
        setSupportActionBar(toolBar);

    }

    ArrayList<String> getImageFromStorage(File root) {
        ArrayList<String> newList = new ArrayList<>();

        File[] files = root.listFiles();

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                newList.addAll(getImageFromStorage(files[i]));
            }
            else
            {
                if (files[i].getName().endsWith(".jpg")
                        || files[i].getName().endsWith(".png")
                        || files[i].getName().endsWith(".JPG")
                        || files[i].getName().endsWith(".PNG")) {
                    newList.add(files[i].toString());
                }
            }
        }

        return newList;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){

            int index = 0;
            if (imageList.size() > 0)
                index = gridView.getFirstVisiblePosition();

            initImageGridView();

            if (imageList.size() > 0)
                gridView.setSelection(index);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main_view, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_capture) {
            openCamera();
        }
        return super.onOptionsItemSelected(item);
    }

    private void openCamera(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        {
            openCameraIntent();
        }
        else
        {
            sendCameraRequestPermission();
        }
    }

    private void openCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

        // start the image capture Intent
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }
}
