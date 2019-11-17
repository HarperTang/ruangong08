package com.example.taoti.activity;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.taoti.MainActivity;
import com.example.taoti.R;
import com.example.taoti.camera.crop.CropImageView;

import java.io.File;
import java.io.IOException;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class TakePhotoActivity extends BaseActivity {

    private static final String TAG = "TakePhotoActivity";
    private FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
    private Camera2BasicFragment cameraFragment;
    private static final int COMPLETED = 0;
    private int TAKE_PHOTO=1;
    private int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE=2;
    private int CHOOSE_PHOTO = 4;

    CropImageView mCropImageView;
    RelativeLayout mTakePhotoLayout;
    LinearLayout mCropperLayout;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == COMPLETED) {
                mTakePhotoLayout.setVisibility(View.GONE);
                mCropperLayout.setVisibility(View.VISIBLE);//UI更改操作

            }
        }
    };

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉Activity上面的状态栏
        setContentView(R.layout.take_photo_layout);
        mCropImageView = (CropImageView) findViewById(R.id.CropImageView);
        mTakePhotoLayout = (RelativeLayout) findViewById(R.id.photo_layout);
        mCropperLayout = (LinearLayout) findViewById(R.id.cropper_layout);
        mCropImageView.setGuidelines(2);
        cameraFragment = Camera2BasicFragment.newInstance();
        if(null==savedInstanceState){
                    transaction.replace(R.id.container, cameraFragment)
                    .commit();
        }

        cameraFragment.setOnCameraStopped(new Camera2BasicFragment.OnCameraStopped(){

            @Override
            public void OnPictureTaken() {

                Log.d(TAG, "OnPictureTaken: ");
                File mFile = cameraFragment.getFile();
                //transaction.remove(cameraFragment);
                //准备截图
                try {
                    mCropImageView.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(mFile)));
               // mCropImageView.rotateImage(90);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
                showCropperLayout();
                /*Intent intent = new Intent(TakePhotoActivity.this, SearchAnswerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("id",TAKE_PHOTO);
                bundle.putString("path",mFile.getAbsolutePath());
                Log.d(TAG, "OnPictureTaken: "+mFile.getAbsolutePath());
                intent.putExtras(bundle);
                startActivity(intent);*/
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if(requestCode==CHOOSE_PHOTO){
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                cropView.setFilePath(getRealPathFromURI(uri));
                showCrop();
            } else {
                cameraView.getCameraControl().resume();
            }
        }*/
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(contentURI, null, null, null, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public void takePhoto(View view){
        cameraFragment.lockFocus();
    }

    public void openAlbum(View view){
        if(ContextCompat.checkSelfPermission(TakePhotoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        )!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(TakePhotoActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }else{
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, CHOOSE_PHOTO);
        }
    }
    public void close(View view){
        finish();
    }

    public void startCropper(View v){

    }

    public void closeCropper(View v){
        //showTakePhotoLayout();
    }

    private void showCropperLayout() {
        Message message = new Message();
        message.what = COMPLETED;
        handler.sendMessage(message);
    }
}