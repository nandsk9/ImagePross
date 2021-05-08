package com.bot.imagepro;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.flatbuffers.FlexBuffers;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.TreeMap;

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

    //create a new function
    public Mat recognizeImage(Mat mat_image){
        //input is Mat and output is also mat
        //call this function in onCameraframe
        //rotate input mat_image by 90 degree to get proper alignment
        Core.flip(mat_image.t(),mat_image,1);
        //create a gray-scale of mat_image
        Mat grayscaleImage=new Mat();
        Imgproc.cvtColor(mat_image,grayscaleImage,Imgproc.COLOR_RGBA2GRAY);
        //now define height and width
        height=grayscaleImage.height();
        width=grayscaleImage.width();

        //now define minimum height of face in frame
        int absoluteFaceSize=(int) (height*0.1);
        MatOfRect faces=new MatOfRect();//holds all faces in frame

        //check if cascadeclassifier is loaded or not
        if (cascadeClassifier!=null){
            //detect face in frame(grayscaleImage)
            //           input                   output                    change this factors for better face detection
            cascadeClassifier.detectMultiScale(grayscaleImage,faces,1.1,2,2, new Size(absoluteFaceSize,absoluteFaceSize),new Size());
            //minimum size of face in frame

        }
        // now convert faces into array
        Rect[] faceArray=faces.toArray();
        //loop through each faces
        for (int i=0;i<faceArray.length;i++){
            //if you want to draw rectangle around face
            //        input/output  starting  point
            Imgproc.rectangle(mat_image,faceArray[i].tl(),faceArray[i].br(),new Scalar(0,255,0,255),2);
                   //                         end point                  color          R  G  B alpha    thickness

            //now crop the face from frame
            //starting x coordinates         y coordinates
            Rect roi =new Rect((int)faceArray[i].tl().x,(int)faceArray[i].tl().y,
                    (int)(faceArray[i].br().x)-(int)(faceArray[i].tl().x),
                    (int)faceArray[i].br().y-(int)(faceArray[i].tl().y));
            //it is very important to get roi right
            Mat cropped=new Mat(grayscaleImage,roi);//gray scaled cropped face
            Mat cropped_rgba=new Mat(mat_image,roi);//rgba cropped face

            //convert cropped_rgba to bitmap
            Bitmap bitmap=null;
            bitmap=Bitmap.createBitmap(cropped_rgba.cols(),cropped_rgba.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(cropped_rgba,bitmap);
            //before converting resize it to (96,96)
            Bitmap scaledBitmap=Bitmap.createScaledBitmap(bitmap,96,96,false);

            //convert bitmap to byte buffer

            ByteBuffer byteBuffer=convertBitmapToByteBuffer(scaledBitmap);
            //now put this byte buffer in object
            Object[] input=new Object[1];
            input[0]=byteBuffer;
            //now create an map for output
            Map<Integer,Object> output_map=new TreeMap<>();
            float[][] age=new float[1][1];
            float[][] gender=new float[1][1];
            //put this into output_map
            output_map.put(0,age);
            //  integer object
            output_map.put(1,gender);
            //run Interpreter for prediction
            interpreter.runForMultipleInputsOutputs(input,output_map);




        }

        //before return rotate back it with -90 degree
        Core.flip(mat_image.t(),mat_image,0);
        return mat_image;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap scaledBitmap) {
        ByteBuffer byteBuffer;
        int size_image=96;
        //4 is for float
        //3 is for rgb
        byteBuffer=ByteBuffer.allocateDirect(4*1*size_image*size_image*3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intvalues=new int[size_image*size_image];
        scaledBitmap.getPixels(intvalues,0,scaledBitmap.getWidth(),0,0,scaledBitmap.getWidth(),scaledBitmap.getHeight());
        int pixel=0;
        for (int i=0;i<size_image;++i){
            for(int j=0;j<size_image;++j){
                final int val=intvalues[pixel++];
                //now most important part to put float in byte buffer
                byteBuffer.putFloat((((val>>16)& 0xFF))/255.0f);
                byteBuffer.putFloat((((val>>8)& 0xFF))/255.0f);
                byteBuffer.putFloat(((val & 0xFF))/255.0f);
                // scaling image from 0-255 to 0-1

            }
        }
        return byteBuffer;



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
