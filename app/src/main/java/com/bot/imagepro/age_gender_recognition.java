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
    //it is use to setup GPU and thread for Interpreter
    //define height and width
    private  int height=0;
    private int width=0;
    //this is used to load haar cascade classifier
    private CascadeClassifier cascadeClassifier;





    public static class facialExpressionRecognition {
        //define interpreter
        //Before this implement tensorflow to build gradle file
        private Interpreter interpreter;
        private Interpreter interpreter1;
        //define input size
        private int INPUT_SIZE;

        //define height and width
        private int height = 0;
        private int width = 0;
        //now it use to implement gpu in interpreter
        private GpuDelegate gpuDelegate = null;
        private GpuDelegate gpuDelegate1 = null;


        //now define cascadeclassifier for face detection
        private CascadeClassifier cascadeClassifier;

        //now call this in cameraActivity


        facialExpressionRecognition(AssetManager assetManager, Context context, String modelPath, int inputSize) throws IOException {
            INPUT_SIZE = inputSize;
            //set GPU for the interpreter
            Interpreter.Options options = new Interpreter.Options();
            gpuDelegate = new GpuDelegate();
            //add gpudelegate to option
            options.addDelegate(gpuDelegate);
            //now set number of thread to option
            options.setNumThreads(4);//set according to your phone
            //this will be load modelweight to interpreter
            interpreter = new Interpreter(loadModelFile(assetManager, modelPath), options);
            //if model is load print
            Log.d("facial_Expression", "model is loaded");


            //// now we will  load haar cascade classifier
            try {
                //define input stream
                InputStream is = context.getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
                // create a folder
                File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
                //now create a new file in that folder
                File mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt");
                //now define output stream to transfer data to file we created
                FileOutputStream os = new FileOutputStream(mCascadeFile);
                //now create buffer to store byte
                byte[] buffer = new byte[4096];
                int byteRead;
                //read byte in while loop
                //when it read -1 that means no data to read

                while ((byteRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, byteRead);

                }
                is.close();
                os.close();
                cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                //if cascade file is loaded print
                Log.d("facial_Expression", "Classifier is loaded");



            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        public Mat recognizeImage(Mat mat_image) {
            //before predicting
            //our image is not properly alighn
            //we have to rotate it by  90 degree for proper prediction
            Core.flip(mat_image.t(), mat_image, 1);//rotate mat_image by 90 degree
            //start with our process
            //convert mat_image to gray scale image
            Mat grayscaleImage = new Mat();
            Imgproc.cvtColor(mat_image, grayscaleImage, Imgproc.COLOR_RGBA2GRAY);
            //set height and weight
            height = grayscaleImage.height();
            width = grayscaleImage.width();


            //define minimum height of face in original image
            //below this size of face in original image will show
            int absoluteFaceSize = (int) (height * 0.1);
            //now create MatofRect to store face
            MatOfRect faces = new MatOfRect();
            //check if cascadeclassifier is loaded or not
            if (cascadeClassifier != null) {
                //detect face in frame
                //                     input                              output
                cascadeClassifier.detectMultiScale(grayscaleImage, faces, 1.1, 2, 2,
                        new Size(absoluteFaceSize, absoluteFaceSize), new Size());
                //minimum size
            }

            // now convert it to array
            Rect[] faceArray = faces.toArray();
            //loop through each face
            for (int i = 0; i < faceArray.length; i++) {
                //if  you want to rectangle around face
                //
                // Imgproc.rectangle(mat_image,faceArray[i].tl(),faceArray[i].br(),new Scalar(0,255,0,255),2);
                //now crop face from original frame and grayscaleImage
                Rect roi = new Rect((int) faceArray[i].tl().x, (int) faceArray[i].tl().y,
                        (int) (faceArray[i].br().x) - (int) (faceArray[i].tl().x),
                        (int) faceArray[i].br().y - (int) (faceArray[i].tl().y));
                //it is very important to get roi right
                Mat cropped_rgba = new Mat(mat_image, roi);////rgba cropped face
                // Mat cropped=new Mat(grayscaleImage,roi);//gray scaled cropped face
                //now converted into cropped rgba to bit map
                Bitmap bitmap = null;
                bitmap = Bitmap.createBitmap(cropped_rgba.cols(), cropped_rgba.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(cropped_rgba, bitmap);
                //before converting resize it to (96,96)
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 48, 48, false);
                ByteBuffer byteBuffer = convertBitmapToByteBuffer(scaledBitmap);
                //now create an object to hold output
                float[][] emotion = new float[1][1];

                //now predict the byte buffer as an input and emotion as an output
                interpreter.run(byteBuffer, emotion);

                //if emotion is recognize print value of it

                //define float value of emotion
                float emotion_v = (float) Array.get(Array.get(emotion, 0), 0);

                Log.d("facial_expression", "Output:" + emotion_v);

                //create a function that return text emotion
                String emotion_s = get_emotion_text(emotion_v);
                //now put text on original frame(mat_image)
                //           input/output text : Angry (2.934234)
                Imgproc.putText(mat_image, emotion_s + "(" + emotion_v + ")",
                        //starting x coordinate                starting y coordinate
                        new Point((int) faceArray[i].tl().x - 10, (int) faceArray[i].tl().y - 20),
                        1, 1.5, new Scalar(255, 0, 0), 2);
                //0,0,255,150
            }
            //after prediction
            //rotate mat_image -90 degree
            Core.flip(mat_image.t(), mat_image, 0);


            return mat_image;

        }


        public Mat recognizePhoto(Mat mat_image) {

            Mat grayscaleImage = new Mat();
            Imgproc.cvtColor(mat_image, grayscaleImage, Imgproc.COLOR_RGBA2GRAY);
            //set height and weight
            height = grayscaleImage.height();
            width = grayscaleImage.width();


            //define minimum height of face in original image
            //below this size of face in original image will show
            int absoluteFaceSize = (int) (height * 0.1);
            //now create MatofRect to store face
            MatOfRect faces = new MatOfRect();
            //check if cascadeclassifier is loaded or not
            if (cascadeClassifier != null) {
                //detect face in frame
                //                     input                              output
                cascadeClassifier.detectMultiScale(grayscaleImage, faces, 1.1, 2, 2,
                        new Size(absoluteFaceSize, absoluteFaceSize), new Size());
                //minimum size
            }

            // now convert it to array
            Rect[] faceArray = faces.toArray();
            //loop through each face
            for (int i = 0; i < faceArray.length; i++) {
                //if  you want to rectangle around face
                //
                // Imgproc.rectangle(mat_image,faceArray[i].tl(),faceArray[i].br(),new Scalar(0,255,0,255),20);
                //now crop face from original frame and grayscaleImage
                Rect roi = new Rect((int) faceArray[i].tl().x, (int) faceArray[i].tl().y,
                        (int) (faceArray[i].br().x) - (int) (faceArray[i].tl().x),
                        (int) faceArray[i].br().y - (int) (faceArray[i].tl().y));
                //it is very important to get roi right
                Mat cropped_rgba = new Mat(mat_image, roi);////rgba cropped face
                // Mat cropped=new Mat(grayscaleImage,roi);//gray scaled cropped face
                //now converted into cropped rgba to bit map
                Bitmap bitmap = null;
                bitmap = Bitmap.createBitmap(cropped_rgba.cols(), cropped_rgba.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(cropped_rgba, bitmap);
                //before converting resize it to (96,96)
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 48, 48, false);
                ByteBuffer byteBuffer = convertBitmapToByteBuffer(scaledBitmap);
                //now create an object to hold output
                float[][] emotion = new float[1][1];
                //now predict the byte buffer as an input and emotion as an output
                interpreter.run(byteBuffer, emotion);
                //if emotion is recognize print value of it

                //define float value of emotion


                float emotion_v = (float) Array.get(Array.get(emotion, 0), 0);
                Log.d("facial_expression", "Output:" + emotion_v);
                //create a function that return text emotion
                String emotion_s = get_emotion_text(emotion_v);
                //now put text on original frame(mat_image)
                //           input/output text : Angry (2.934234)
                Imgproc.putText(mat_image, emotion_s,
                        //starting x coordinate                starting y coordinate
                        new Point((int) faceArray[i].tl().x + 20, (int) faceArray[i].tl().y - 60),
                        4, 4.5, new Scalar(255, 0, 0, 255), 15);
                //                       color        red

            }

            // Core.flip(mat_image.t(),mat_image,0);
            return mat_image;

        }


        private String get_emotion_text(float emotion_v) {
            //create an empty string
            String val = "";
            //use if statement to determine val
            //you can change starting value and ending value to get better result

            if (emotion_v >= 0 & emotion_v < 0.5) {
                val = "Surprise";

            } else if (emotion_v > 0.5 & emotion_v < 1.5) {
                val = "Fear";
            } else if (emotion_v > 1.5 & emotion_v < 2.5) {
                val = "Angry";
            } else if (emotion_v > 2.5 & emotion_v < 3.5) {
                val = "Neutral";
            } else if (emotion_v > 3.5 & emotion_v < 4.5) {
                val = "Sad";
            } else if (emotion_v > 4.5 & emotion_v < 5.5) {
                val = "Disgust";

            } else {
                val = "Happy";
            }
            return val;
        }


        private ByteBuffer convertBitmapToByteBuffer(Bitmap scaledBitmap) {
            ByteBuffer byteBuffer;
            int size_image = INPUT_SIZE;//48
            //4 is multiplied for float
            //3 is multiplied for rgb
            byteBuffer = ByteBuffer.allocateDirect(4 * 1 * size_image * size_image * 3);
            byteBuffer.order(ByteOrder.nativeOrder());
            int[] intValues = new int[size_image * size_image];
            scaledBitmap.getPixels(intValues, 0, scaledBitmap.getWidth(), 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());
            int pixel = 0;
            for (int i = 0; i < size_image; ++i) {
                for (int j = 0; j < size_image; ++j) {
                    final int val = intValues[pixel++];
                    //now most important part to put float in byte buffer
                    byteBuffer.putFloat((((val >> 16) & 0xFF)) / 255.0f);
                    byteBuffer.putFloat((((val >> 8) & 0xFF)) / 255.0f);
                    byteBuffer.putFloat(((val & 0xFF)) / 255.0f);
                    // scaling image from 0-255 to 0-1

                }
            }
            return byteBuffer;


        }


        private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
            //this will give description of file
            AssetFileDescriptor assetFileDescriptor = assetManager.openFd(modelPath);
            // create a input stream to read file
            FileInputStream inputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = assetFileDescriptor.getStartOffset();
            long declaredLength = assetFileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);


        }

    }



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

            //read object from output map
            Object age_o=output_map.get(0);
            Object gender_o=output_map.get(1);
            //extract value from object
            int age_value=(int)(float) Array.get(Array.get(age_o,0),0);
            //converting age in float to int
            float gender_value=(float)Array.get(Array.get(gender_o,0),0);
            //gender value is from 0 to 1
            //close to 1 is for female
            //close to 0 is for male
            //you have set threshold to get better result
            //if  threshold is too small or too large
            // result will be bad
            if (gender_value>0.80){
                 // put age ,gender in text
                //Female,49
                //           input/output              text
                Imgproc.putText(cropped_rgba,"Female,"+age_value
                        ,new Point(10,20),1,1.5,new Scalar(255,0,0,255),2);
                //  starting point                                color             R  G  B  alpha    thickness

            }
            else if(gender_value<0.80){

                Imgproc.putText(cropped_rgba,"Male,"+age_value
                        ,new Point(10,20),1,1.5,new Scalar(0, 0, 200),2);
                //                                                                  blue color
            }
            else{
                Imgproc.putText(cropped_rgba,"Not a child"+age_value
                        ,new Point(10,20),1,1.5,new Scalar(0,0,255,255),2);

            }
              //if you want to see number in
            Log.d("age_gender_recognition","Out "+age_value+","+gender_value);


            //replace  face in original frame(mat_image) with cropped_rgba
            cropped_rgba.copyTo(new Mat(mat_image,roi));



        }

        //

        //before return rotate back it with -90 degree
        Core.flip(mat_image.t(),mat_image,0);
        return mat_image;
    }


    public Mat recognizePhoto1(Mat mat_image){

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
            Imgproc.rectangle(mat_image,faceArray[i].tl(),faceArray[i].br(),new Scalar(0,255,0,255),7);
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

            //read object from output map
            Object age_o=output_map.get(0);
            Object gender_o=output_map.get(1);
            //extract value from object
            int age_value=(int)(float) Array.get(Array.get(age_o,0),0);
            //converting age in float to int
            float gender_value=(float)Array.get(Array.get(gender_o,0),0);
            //gender value is from 0 to 1
            //close to 1 is for female
            //close to 0 is for male
            //you have set threshold to get better result
            //if  threshold is too small or too large
            // result will be bad
            if (gender_value > 0.80){
                // put age ,gender in text
                //Female,49
                //           input/output              text
                Imgproc.putText(cropped_rgba,"Female,"+age_value
                        ,new Point(15,110),4,4.5,new Scalar(255,0,0,255),15);
                //  starting point                                color             R  G  B  alpha    thickness

            }
            else if (gender_value < 0.80){

                Imgproc.putText(cropped_rgba,"Male,"+age_value
                        ,new Point(15,110),4,4.5,new Scalar(0,0,255,255),15);
                //                                                                  blue color
            }
            else{
                Imgproc.putText(cropped_rgba,"not a child,"+age_value
                        ,new Point(15,110),4,4.5,new Scalar(0,0,255,255),15);

            }





            //if you want to see number in
            Log.d("age_gender_recognition","Out "+age_value+","+gender_value);


            //replace  face in original frame(mat_image) with cropped_rgba
            cropped_rgba.copyTo(new Mat(mat_image,roi));



        }

        //


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

//kl60m3941
}
