package com.example.taoti.activity;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;
import com.example.taoti.R;

import java.io.File;

public class SearchAnswerActivity extends BaseActivity {

    private ProgressDialog mProgressDialog=null;

    private static final String TAG = "SearchAnswerActivity";

    private Bundle bundle;
    private int show_choice;
    private static final int TAKE_PHOTO =1;
    public static final int CHOOSE_PHOTO = 2;
    private Uri imageUri;
    private static TextView answerTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_answer_activity);
        answerTextView =(TextView) findViewById(R.id.answer_text);
        answerTextView.setText("在这儿显示内容");
        bundle = getIntent().getExtras();
        show_choice=bundle.getInt("id");
        switch (show_choice){
            case TAKE_PHOTO:{
                showProgressDialog();
                String imagePath = bundle.getString("path");
                File file = new File(imagePath);
                imageUri = Uri.fromFile(file);
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(imageUri,"image/*");//用第三方程序打开image
                intent.putExtra("scale",true);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                Currency(imagePath);
            }
            break;
            case CHOOSE_PHOTO:{
                openAlbum();
            }
            break;
            default:
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        String imagePath = null;
        switch (show_choice){
            case CHOOSE_PHOTO:{
                showProgressDialog();
                // 判断手机系统版本号
                if (Build.VERSION.SDK_INT >= 19) {
                    // 4.4及以上系统使用这个方法处理图片
                    imagePath=handleImageOnKitKat(data);
                }
                else {
                    // 4.4以下系统使用这个方法处理图片
                    imagePath=handleImageBeforeKitKat(data);
                }
                /*Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(imageUri,"image/*");//用第三方程序打开image
                intent.putExtra("scale",true);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                Currency(imagePath);*/
                break;
            }
        }
    }

    private void openAlbum(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO); // 打开相册
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private String handleImageOnKitKat(Intent data){
        String imagePath = null;
        Uri uri = data.getData();
        Log.d("TAG", "handleImageOnKitKat: uri is " + uri);

        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            }
            else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        }
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        return imagePath; // 根据图片路径显示图片
    }

    private String handleImageBeforeKitKat(Intent data){
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        return imagePath;
    }

    private String getImagePath(Uri uri, String selection){
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void showProgressDialog() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null == mProgressDialog) {
                    mProgressDialog = ProgressDialog.show(SearchAnswerActivity.this,
                            "",
                            "正在识别，请稍后...",
                            true,
                            false);
                } else if (!mProgressDialog.isShowing()) {
                    mProgressDialog.show();
                }
            }
        });

    }

    private void dismissProgressDialog() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != mProgressDialog) {
                    mProgressDialog.dismiss();
                }
            }
        });

    }

    /**
     * 调用通用文字识别服务
     */
    public void Currency(String imagePath){
        final StringBuffer sb=new StringBuffer();
        //通用文字识别参数设置
        Log.d(TAG, "Currency: ");
        GeneralBasicParams param = new GeneralBasicParams();
        param.setDetectDirection(true);//检测朝向的
        //param.setImageFile(new File(getExternalCacheDir().getPath()+"/output_image.jpg"));
        param.setImageFile(new File(imagePath));
        Log.d(TAG, "Currency: "+imagePath);
        //调用通用文字识别服务
        OCR.getInstance(this).recognizeGeneralBasic(param, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(GeneralResult generalResult) {
                // 调用成功，返回GeneralResult对象
                for (WordSimple wordSimple : generalResult.getWordList()) {
                    // wordSimple不包含位置信息
                    //WordSimple word = wordSimple;
                    sb.append(wordSimple.getWords());
                    sb.append("\n");
                }
                answerTextView.setText(sb.toString());
                Log.d(TAG, sb.toString());
                // json格式返回字符串result.getJsonRes())
                dismissProgressDialog();
            }

            @Override
            public void onError(OCRError ocrError) {
                //调用失败，返回OCRError对象
                Log.d(TAG, "失败");
                Toast.makeText(SearchAnswerActivity.this,"识别出现错误", Toast.LENGTH_SHORT).show();
                dismissProgressDialog();
            }
        });
    }
}
