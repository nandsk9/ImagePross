package com.bot.imagepro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.Loader;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG= "MainActivity";
    private Mat mRgba;
    private Mat mGrey;
    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface
                        .SUCCESS:{
                    Log.i(TAG,"Opencv is loaded");
                    mOpenCvCameraView.enableView();
                }
                default:
                {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    public CameraActivity(){
        Log.i(TAG,"Instantiated new "+this.getClass());
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("Test1"," 111111");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Log.e("Test2","22222");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Log.e("Test3","333333333");
        int MY_PERMISSIONS_REQUEST_CAMERA =0;

        if (ContextCompat.checkSelfPermission( CameraActivity. this, Manifest.permission.CAMERA) ==PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(CameraActivity.this, new String[] {Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);

        }


        setContentView(R.layout.activity_camera);
        Log.e("Test4","444444444444");
        mOpenCvCameraView = (CameraBridgeViewBase)findViewById(R.id.frame_Surface);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG,"Opencv initialize is done");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }else {
            Log.d(TAG,"Opencv is not loaded try again");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION,this,mLoaderCallback);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }
    }

    public void onDestroy(){
        super.onDestroy();
        if(mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }
    }

    public void onCameraViewStarted(int width ,int height){
        mRgba=new Mat(height,width, CvType.CV_8UC4);
        mGrey =new Mat(height,width,CvType.CV_8UC1);
    }
    public void onCameraViewStopped(){
        mRgba.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        mRgba=inputFrame.rgba();
        mGrey =inputFrame.gray();

        //mRgba=CascadeRec(mRgba);
        return mRgba;
    }


}

