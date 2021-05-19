package com.bot.imagepro;

import android.Manifest;
import android.app.Activity;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import org.opencv.objdetect.CascadeClassifier;


import java.io.IOException;


public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";
    private Mat mRgba;
    private Mat mGrey;
    private CameraBridgeViewBase mOpenCvCameraView;
    //call for image view of flip button
    private ImageView flip_camera;
    //now we define integer that represent camera
    //0 - back camera
    //1 - front camera
    //initially it will be start with back camera
    private int mCameraId=0;

    //call java class
    private age_gender_recognition age_gender_recognition;
    private age_gender_recognition.facialExpressionRecognition facialExpressionRecognition ;





    private CascadeClassifier cascadeClassifier;
    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface
                    .SUCCESS) {
                Log.i(TAG, "Opencv is loaded");
                mOpenCvCameraView.enableView();
            }
            super.onManagerConnected(status);
        }
    };

    public CameraActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("Test1", " 111111");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Log.e("Test2", "22222");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Log.e("Test3", "333333333");
        int MY_PERMISSIONS_REQUEST_CAMERA = 0;

        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);

        }


        setContentView(R.layout.activity_camera);
        Log.e("Test4", "444444444444");
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.frame_Surface);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        flip_camera=findViewById(R.id.flip_camera);
        flip_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //this function change camera
                swapCamera();
            }
        });

        // try {
        //   InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
        //  File cascadeDir = getDir("cascade", MODE_PRIVATE);
        //File mCascadeFile = new File(cascadeDir, "raw/haarcascade_frontalface_alt.xml");
        //FileOutputStream os = new FileOutputStream(mCascadeFile);
        //byte[] buffer = new byte[4096];
        //int byteRead;
        //while ((byteRead = is.read(buffer)) != -1) {
        //  os.write(buffer, 0, byteRead);
        // }
        //is.close();
        //os.close();

        //cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());

        //catch (IOException e) {
        //Log.i(TAG, "Cascade file not Found ");
        // }
    try {
        //model input image size (96,96,3)
        int inputSize=96;
        age_gender_recognition = new age_gender_recognition(getAssets(), CameraActivity. this,"model.tflite",inputSize);


    } catch (IOException e) {
        e.printStackTrace();
    }

   //facial emotion calling
        // this will load cascade and model

    try {
       //input size of model is 48
        int inputSize=48;
        facialExpressionRecognition=new age_gender_recognition.facialExpressionRecognition(getAssets(),CameraActivity.this,"model1.tflite",inputSize);

    }
    catch (IOException e){
        e. printStackTrace();
    }
    }

    private void swapCamera() {
        //first we will change mCameraId
        // if 0 change it to 1
        //if 1 change it to 0
        mCameraId=mCameraId^1;//basic not operation
        //disable current cameraview
        mOpenCvCameraView.disableView();
        //set camera index
        mOpenCvCameraView.setCameraIndex(mCameraId);
        // enable now
        mOpenCvCameraView.enableView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Opencv initialize is done");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.d(TAG, "Opencv is not loaded try again");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGrey = new Mat(height, width, CvType.CV_8UC1);


    }

    public void onCameraViewStopped() {
        mRgba.release(); //mGray.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGrey = inputFrame.gray();
        // when mCameraId is 1 (front) rotate camera frame with 180 degree
        if(mCameraId==1){
            Core.flip(mRgba,mRgba,-1);
            Core.flip(mGrey,mGrey,-1);
        }
        //   output                     input
        mRgba=age_gender_recognition.recognizeImage(mRgba);
        mRgba=facialExpressionRecognition.recognizeImage(mRgba);




           // mRgba = CascadeRec(mRgba);
            return mRgba;
        }

        //private Mat CascadeRec (Mat mRgba){
           // Core.flip(mRgba.t(), mRgba, 1);
            //Mat mRbg = new Mat();
           // Imgproc.cvtColor(mRgba, mRbg, Imgproc.COLOR_RGBA2RGB);

            //int height = mRbg.height();
           // int absoluteFaceSize = (int) (height * 0.1);

           // MatOfRect faces = new MatOfRect();
           // if (cascadeClassifier != null) {

               // cascadeClassifier.detectMultiScale(mRbg, faces, 1.1, 2, 2, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
           // }
           // Rect[] facesArray = faces.toArray();
           // for (int i = 0; i < facesArray.length; i++) {
              //  Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 2);


            //}

           // Core.flip(mRgba.t(), mRgba, 0);
           // return mRgba;

        //}











}


//https://www.youtube.com/watch?v=HeLi7UmQssI&list=PL0aoTDj9Nwgh0hTC3QBHwKtJuxl1veGyG&index=8