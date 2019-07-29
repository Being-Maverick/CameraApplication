package com.hfad.cameraapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_PHOTO = 1;
    private static File mFile;
    private static Context mContext;
    private static Button mButton;
    private static ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        mButton = findViewById(R.id.camera_button);
        mImageView = findViewById(R.id.image_captured);
        mFile = mCameraFile();
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mFile!=null && captureImage.resolveActivity(getPackageManager()) != null;
        mButton.setEnabled(canTakePhoto);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(getApplicationContext(),"com.hfad.cameraapplication.fileprovider",mFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT,uri);
                List<ResolveInfo> cameraActivities = getPackageManager().queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY);
                for(ResolveInfo activity: cameraActivities){
                    grantUriPermission(activity.activityInfo.packageName,uri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                startActivityForResult(captureImage,REQUEST_PHOTO);
            }
        });
        updatePhotoView();
    }

    public static File mCameraFile(){
        File directory = mContext.getFilesDir();
        return new File(directory,"IMG_" + UUID.randomUUID() + ".jpg");
    }

    private void updatePhotoView() {
        if (mFile == null || !mFile.exists()) {
            mImageView.setImageDrawable(null);
        } else {
            Bitmap bitmap = Picture.getScaledBitmap(
                    mFile.getPath(), this);
            mImageView.setImageBitmap(bitmap);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode != Activity.RESULT_OK){
            return;
        }
        if (requestCode == REQUEST_PHOTO){
            Uri uri =  FileProvider.getUriForFile(getApplicationContext(),
                    "com.hfad.cameraapplication.fileprovider",
                    mFile);
            this.revokeUriPermission(uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            updatePhotoView();
        }
    }
}
