package com.bot.imagepro;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import org.opencv.objdetect.CascadeClassifier;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class age_gender_recognition {
    // define interpreter
    private Interpreter interpreter;
   // using this we will load model and predict on real time frames

    //define input size
    private int INPUT_SIZE;
    //image_std is to scale image from 0-255 0-1
    private  float IMAGE_STD=255.0f;
    private  float IMAGE_MEAN=0;
    private GpuDelegate gpuDelegate=null;
    //it is use to setup GPU and thead for Interpreter
    //define height and width
    private  int height=0;
    private int width=0;
    //this is used to load haar cascade classifier
    private CascadeClassifier cascadeClassifier;
    // this we have to call in onCreate
    age_gender_recognition(AssetManager assetManager, Context context,String modelPath,int inputSize)throws IOException{
     INPUT_SIZE=inputSize;
     //define GPU and number of thread to interpreter
        Interpreter.Options options=new Interpreter.Options();
        gpuDelegate=new GpuDelegate();
        options.addDelegate(gpuDelegate);
        options.setNumThreads(4);//you can set number of thread according to you phone
        //if you phone have more number of thread - frame rate will be high
        //load model
        interpreter = new  Interpreter(loadModelFile(assetManager,modelPath),options);
        //if this work model is loaded to interpreter
        Log.d("Age_Gender_Recognition","CNN model is loaded");

        // now we have to load haar cascade model frontal face
        //why we are using face detection?
        //answer is first we detect face in original frame
        //then cropped face is pass through Interpreter
        //which will give you output (age , gender)
        //if we don't use face detection input of interpreter will be original frame
        //it will give improper output

        try {
            //define input stream haarcascade model
            InputStream is=context.getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
            //create a folder
            File cascadeDir=context.getDir("cascade",context.MODE_PRIVATE);
            //create a file name haarcascade_frontalface_alt in that folder
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

            // now we have mCascadeFile
            //load cascadeclassifier
                   //                           path of mCascadeFile
            cascadeClassifier=new CascadeClassifier(mCascadeFile.getAbsolutePath());

            //if haarcascade file is loaded
            Log.d("Age_Gender_Recognition","haar cascade classifier is loaded");


        }
        catch (IOException e){
            e.printStackTrace();
        }

    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath)throws IOException {
        //given you description of file
        AssetFileDescriptor assetFileDescriptor=assetManager.openFd(modelPath);
        //define a input stream to load file
        FileInputStream inputStream=new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startOffset=assetFileDescriptor.getStartOffset();
        long declaredLength=assetFileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declaredLength);

    }


}
