package com.bot.imagepro;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


public  class autistic_detection {

    private Interpreter interpreter;
    private int INPUT_SIZE;
    //image_std is to scale image from 0-255 0-1
    private float IMAGE_STD = 255.0f;
    private float IMAGE_MEAN = 0;
    private GpuDelegate gpuDelegate = null;
    //it is use to setup GPU and thread for Interpreter
    //define height and width
    private int height = 0;
    private int width = 0;
    //this is used to load haar cascade classifier
    private CascadeClassifier cascadeClassifier;

    autistic_detection(AssetManager assetManager, Context context, String modelPath, int inputSize) throws IOException {

        INPUT_SIZE = inputSize;
        Interpreter.Options options=new Interpreter.Options();
        gpuDelegate=new GpuDelegate();
        options.addDelegate(gpuDelegate);
        options.setNumThreads(4);
        interpreter = new  Interpreter(loadModelFile(assetManager,modelPath),options);
        Log.d("autistic_detection","CNN model is loaded");
        try {
            //define input stream haarcascade model
            InputStream is = context.getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
            File cascadeDir=context.getDir("cascade",context.MODE_PRIVATE);
            File mCascadeFile=new File(cascadeDir,"haarcascade_frontalface_alt");
            FileOutputStream os=new FileOutputStream(mCascadeFile);
            byte[] buffer=new byte[4096];
            int byteRead;
            //save file from raw to mCascadeFile
            while ((byteRead=is.read(buffer)) !=-1){
                os.write(buffer,0,byteRead);
            }
            is.close();
            os.close();
            cascadeClassifier=new CascadeClassifier(mCascadeFile.getAbsolutePath());
            Log.d("autistic_detection","haar cascade classifier is loaded");
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }





    public Mat recognizeImage(Mat mat_image) {
        Core.flip(mat_image.t(), mat_image, 1);
        Mat grayscaleImage = new Mat();
        Imgproc.cvtColor(mat_image, grayscaleImage, Imgproc.COLOR_RGBA2GRAY);
        height = grayscaleImage.height();
        width = grayscaleImage.width();
        int absoluteFaceSize = (int) (height * 0.1);
        MatOfRect faces = new MatOfRect();
        if (cascadeClassifier != null) {
            //detect face in frame(grayscaleImage)
            //           input                   output                    change this factors for better face detection
            cascadeClassifier.detectMultiScale(grayscaleImage, faces, 1.1, 2, 2, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }
        Rect[] faceArray = faces.toArray();
        //loop through each faces
        for (int i = 0; i < faceArray.length; i++) {
            //if you want to draw rectangle around face
            //        input/output  starting  point
            // Imgproc.rectangle(mat_image, faceArray[i].tl(), faceArray[i].br(), new Scalar(0, 255, 0, 255), 2);
            //                         end point                  color          R  G  B alpha    thickness
            Rect roi = new Rect((int) faceArray[i].tl().x, (int) faceArray[i].tl().y,
                    (int) (faceArray[i].br().x) - (int) (faceArray[i].tl().x),
                    (int) faceArray[i].br().y - (int) (faceArray[i].tl().y));
            //it is very important to get roi right
            //Mat cropped = new Mat(grayscaleImage, roi);//gray scaled cropped face
            Mat cropped_rgba = new Mat(mat_image, roi);//rgba cropped face
            Bitmap bitmap = null;
            bitmap = Bitmap.createBitmap(cropped_rgba.cols(), cropped_rgba.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(cropped_rgba, bitmap);
            //before converting resize it to (96,96)
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, false);
            ByteBuffer byteBuffer = convertBitmapToByteBuffer(scaledBitmap);
            //now put this byte buffer in object
            //Object[] input = new Object[1];
            // input[0] = byteBuffer;
            //now create an map for output

            float[][] detection = new float[1][2];
            interpreter.run(byteBuffer,detection);
            float detection_v=(float) Array.get(Array.get(detection,0),0);
            Log.d("autistic_detection","Output:"+ detection_v);
            String detection_s=get_detection_text(detection_v);
            Imgproc.putText(mat_image,detection_s+"("+detection_v+")",
                    //starting x coordinate                starting y coordinate
                    new Point((int)faceArray[i].tl().x-10,(int)faceArray[i].tl().y-60),
                    1,1.5,new Scalar(255, 0, 0),2);
            //0,0,255,150



        }
        Core.flip(mat_image.t(),mat_image,0);
        return mat_image;
    }




    public  Mat recognizePhoto2(Mat mat_image) {

        Mat grayscaleImage = new Mat();
        Imgproc.cvtColor(mat_image, grayscaleImage, Imgproc.COLOR_RGBA2GRAY);
        height = grayscaleImage.height();
        width = grayscaleImage.width();
        int absoluteFaceSize = (int) (height * 0.1);
        MatOfRect faces = new MatOfRect();
        if (cascadeClassifier != null) {
            //detect face in frame(grayscaleImage)
            //           input                   output                    change this factors for better face detection
            cascadeClassifier.detectMultiScale(grayscaleImage, faces, 1.1, 2, 2, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }
        Rect[] faceArray = faces.toArray();
        //loop through each faces
        for (int i = 0; i < faceArray.length; i++) {
            //if you want to draw rectangle around face
            //        input/output  starting  point
            // Imgproc.rectangle(mat_image, faceArray[i].tl(), faceArray[i].br(), new Scalar(0, 255, 0, 255), 2);
            //                         end point                  color          R  G  B alpha    thickness
            Rect roi = new Rect((int) faceArray[i].tl().x, (int) faceArray[i].tl().y,
                    (int) (faceArray[i].br().x) - (int) (faceArray[i].tl().x),
                    (int) faceArray[i].br().y - (int) (faceArray[i].tl().y));
            //it is very important to get roi right
            //Mat cropped = new Mat(grayscaleImage, roi);//gray scaled cropped face
            Mat cropped_rgba = new Mat(mat_image, roi);//rgba cropped face
            Bitmap bitmap = null;
            bitmap = Bitmap.createBitmap(cropped_rgba.cols(), cropped_rgba.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(cropped_rgba, bitmap);
            //before converting resize it to (96,96)
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, false);
            ByteBuffer byteBuffer = convertBitmapToByteBuffer(scaledBitmap);
            //now put this byte buffer in object
            //Object[] input = new Object[1];
            // input[0] = byteBuffer;
            //now create an map for output

            float[][] detection = new float[1][2];
            interpreter.run(byteBuffer,detection);
            float detection_v=(float) Array.get(Array.get(detection,0),0);
            Log.d("autistic_detection","Output:"+ detection_v);
            String detection_s=get_detection_text(detection_v);
            Imgproc.putText(mat_image,detection_s+"("+detection_v+")",
                    //starting x coordinate                starting y coordinate
                    new Point((int)faceArray[i].tl().x-10,(int)faceArray[i].tl().y-230),
                    2,2.5,new Scalar(255, 0, 0),15);
            //0,0,255,150



        }
        // Core.flip(mat_image.t(),mat_image,0);
        return mat_image;
    }





    private ByteBuffer convertBitmapToByteBuffer(Bitmap scaledBitmap) {
        ByteBuffer byteBuffer;
        int size_image=INPUT_SIZE;
        byteBuffer=ByteBuffer.allocateDirect(4*1*size_image*size_image*3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues=new int[size_image*size_image];
        scaledBitmap.getPixels(intValues,0,scaledBitmap.getWidth(),0,0,scaledBitmap.getWidth(),scaledBitmap.getHeight());
        int pixel=0;
        for (int i=0;i<size_image;++i){
            for(int j=0;j<size_image;++j){
                final int val=intValues[pixel++];
                //now most important part to put float in byte buffer
                byteBuffer.putFloat((((val>>16)& 0xFF))/255.0f);
                byteBuffer.putFloat((((val>>8)& 0xFF))/255.0f);
                byteBuffer.putFloat(((val & 0xFF))/255.0f);
                // scaling image from 0-255 to 0-1

            }
        }
        return byteBuffer;
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException{
        AssetFileDescriptor assetFileDescriptor=assetManager.openFd(modelPath);
        //define a input stream to load file
        FileInputStream inputStream=new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startOffset=assetFileDescriptor.getStartOffset();
        long declaredLength=assetFileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declaredLength);
    }

    private String get_detection_text(float detection_v) {
        String val="";
        if(detection_v>=0  & detection_v<=0.88 & detection_v >=0.999){
            val="Non Autistic Child";



        }
        else if (detection_v>0.88 & detection_v < 0.99){
            val="Autistic Child";
        }

        return val;
    }



}


