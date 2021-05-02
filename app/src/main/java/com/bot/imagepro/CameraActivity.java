package com.bot.imagepro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.Loader;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
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
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";
    private Mat mRgba;
    private Mat mGrey;
    private CameraBridgeViewBase mOpenCvCameraView;
    private Net mAgeNet;
    private static final String[] AGES = new String[]{"0-2", "4-6", "8-13", "15-20", "25-32", "38-43", "48-53", "60+"};
    private Net mGenderNet;
    private static final String[] GENDERS = new String[]{"male", "female"};
    private Rect[] mTempFrontalFacesArray;

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

        try {
            InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
            File cascadeDir = getDir("cascade", MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);
            byte[] buffer = new byte[4096];
            int byteRead;
            while ((byteRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, byteRead);
            }
            is.close();
            os.close();

            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());

        } catch (IOException e) {
            Log.i(TAG, "Cascade file not Found ");
        }

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
        //Loading age module
        String proto = getPath("deploy_age.prototxt");
        String weights = getPath("age_net.caffemodel");
        Log.i(TAG, "onCameraViewStarted| ageProto : " + proto + ",ageWeights : " + weights);
        mAgeNet = Dnn.readNetFromCaffe(proto, weights);

        //Loading gender module
        proto = getPath("deploy_gender.prototxt");
        weights = getPath("gender_net.caffemodel");
        Log.i(TAG, "onCameraViewStarted| genderProto : " + proto + ",genderWeights : " + weights);
        mGenderNet = Dnn.readNetFromCaffe(proto, weights);

        if (mAgeNet.empty()) {
            Log.i(TAG, "Network loading failed");
        } else {
            Log.i(TAG, "Network loading success");
        }

    }

    public void onCameraViewStopped() {
        mRgba.release(); //mGray.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGrey = inputFrame.gray();

        Log.i(TAG, "mTempFrontalFacesArray : " + Arrays.toString(mTempFrontalFacesArray));
        if (mTempFrontalFacesArray != null) {
            String age;
            Log.i(TAG, "mTempFrontalFacesArray.length : " + mTempFrontalFacesArray.length);
            for (int i = 0; i < mTempFrontalFacesArray.length; i++) {
                Log.i(TAG, "Age : " + analyseAge(mRgba, mTempFrontalFacesArray[i]));
                Log.i(TAG, "Gender : " + analyseGender(mRgba, mTempFrontalFacesArray[i]));
            }
            mTempFrontalFacesArray = null;
        }

            mRgba = CascadeRec(mRgba);
            return mRgba;
        }

        private Mat CascadeRec (Mat mRgba){
            Core.flip(mRgba.t(), mRgba, 1);
            Mat mRbg = new Mat();
            Imgproc.cvtColor(mRgba, mRbg, Imgproc.COLOR_RGBA2RGB);

            int height = mRbg.height();
            int absoluteFaceSize = (int) (height * 0.1);

            MatOfRect faces = new MatOfRect();
            if (cascadeClassifier != null) {

                cascadeClassifier.detectMultiScale(mRbg, faces, 1.1, 2, 2, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
            }
            Rect[] facesArray = faces.toArray();
            for (int i = 0; i < facesArray.length; i++) {
                Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 2);


            }

            Core.flip(mRgba.t(), mRgba, 0);
            return mRgba;

        }





    private String analyseAge(Mat mRgba, Rect face) {
        try {
            Log.e("analyseAgefunction", " analyseAgefunction open aayi");
            Mat capturedFace = new Mat(mRgba, face);
            //Resizing pictures to resolution of Caffe model
            Imgproc.resize(capturedFace, capturedFace, new Size(227, 227));
            //Converting RGBA to BGR
            Imgproc.cvtColor(capturedFace, capturedFace, Imgproc.COLOR_RGBA2BGR);

            //Forwarding picture through Dnn
            Mat inputBlob = Dnn.blobFromImage(capturedFace, 1.0f, new Size(227, 227),
                    new Scalar(78.4263377603, 87.7689143744, 114.895847746), false, false);
            mAgeNet.setInput(inputBlob, "data");
            Mat probs = mAgeNet.forward("prob").reshape(1, 1);
            Core.MinMaxLocResult mm = Core.minMaxLoc(probs); //Getting largest softmax output

            double result = mm.maxLoc.x; //Result of age recognition prediction
            Log.i(TAG, "Result is: " + result);
            return AGES[(int) result];
        } catch (Exception e) {
            Log.e(TAG, "Error processing age", e);
        }
        return null;
    }


    private String analyseGender(Mat mRgba, Rect face) {
        try {
            Log.e("analyseGenderfunction", " analyseGenderfunction open aayi");
            Mat capturedFace = new Mat(mRgba, face);
            //Resizing pictures to resolution of Caffe model
            Imgproc.resize(capturedFace, capturedFace, new Size(227, 227));
            //Converting RGBA to BGR
            Imgproc.cvtColor(capturedFace, capturedFace, Imgproc.COLOR_RGBA2BGR);

            //Forwarding picture through Dnn
            Mat inputBlob = Dnn.blobFromImage(capturedFace, 1.0f, new Size(227, 227),
                    new Scalar(78.4263377603, 87.7689143744, 114.895847746), false, false);
            mGenderNet.setInput(inputBlob, "data");
            Mat probs = mGenderNet.forward("prob").reshape(1, 1);
            Core.MinMaxLocResult mm = Core.minMaxLoc(probs); //Getting largest softmax output

            double result = mm.maxLoc.x; //Result of gender recognition prediction. 1 = FEMALE, 0 = MALE
            Log.i(TAG, "Result is: " + result);
            return GENDERS[(int) result];
        } catch (Exception e) {
            Log.e(TAG, "Error processing gender", e);
        }
        return null;
    }

    private String getPath(String file) {
        AssetManager assetManager = getApplicationContext().getAssets();
        BufferedInputStream inputStream;

        try {
            //Reading data from app/src/main/assets
            Log.e("data read", " data read");
            inputStream = new BufferedInputStream(assetManager.open(file));
            byte[] data = new byte[inputStream.available()];

            inputStream.read(data);
            inputStream.close();

            File outputFile = new File(getApplicationContext().getFilesDir(), file);
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            fileOutputStream.write(data);
            fileOutputStream.close();
            return outputFile.getAbsolutePath();
        } catch (IOException ex) {
            Log.e(TAG, ex.toString());

        }
        return "";

    }
}


//https://www.youtube.com/watch?v=HeLi7UmQssI&list=PL0aoTDj9Nwgh0hTC3QBHwKtJuxl1veGyG&index=8