package com.bot.imagepro;

import android.Manifest;
import android.app.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;


import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;


import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UploadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
  public class UploadFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int RESULT_OK = 1002;
    private static final int IMAGE_PICK_CODE=1000;
    private static final int PERMISSION_CODE=1001;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

     ImageView image_v;
     Button select_image;
    //int SELECT_PICTURE=200;
    //String filePath = "";
   // Uri pickedImageUri;

   // private Uri uri;
    //private String stringPath;
   // private Intent iData;
    private  age_gender_recognition age_gender_recognition;
    private age_gender_recognition.facialExpressionRecognition facialExpressionRecognition;
    private Bitmap bitmap;
    public UploadFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UploadFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UploadFragment newInstance(String param1, String param2) {
        UploadFragment fragment = new UploadFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            setContentView(R.layout.fragment_upload);
        }


    }

    private void setContentView(int fragment_upload) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parentView = inflater.inflate(R.layout.fragment_upload, container, false);


         image_v = parentView.findViewById(R.id.image_v);
        select_image = parentView.findViewById(R.id.select_image);


        try {
            //model input image size (96,96,3)
            int inputSize=96;
            age_gender_recognition = new age_gender_recognition(getActivity().getAssets(), getActivity(),"model.tflite",inputSize);

        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            //input size of model is 48
            int inputSize=48;
            facialExpressionRecognition=new age_gender_recognition.facialExpressionRecognition(getActivity().getAssets(),getActivity(),"model1.tflite",inputSize);

        }
        catch (IOException e){
            e. printStackTrace();
        }


        //load our model file and haar cascade classifier in on create//handle button click
        select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Check run time permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (getContext().checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        //permission not granted ,request it.
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        //show popup for runtime permissions
                        requestPermissions(permissions, PERMISSION_CODE);

                    } else {
                        //permission already granted
                        pickImageFromGallery();
                    }

                } else {
                     //system os is less then marshmallow
                    pickImageFromGallery();
                }
            }

        });
        return parentView;
    }

    private void pickImageFromGallery() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_PICK_CODE);
    }
    // handle result of runtime permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {



            switch (requestCode) {
                case PERMISSION_CODE: {
                    if (grantResults.length > 0 && grantResults[0] ==
                           PackageManager.PERMISSION_GRANTED) {
                       // PERMISSION was granted
                        pickImageFromGallery();
                    } else {
                       // permission denied
                        Toast.makeText(getContext(), "Permission denied...!", Toast.LENGTH_SHORT).show();
                    }

                }
             }
        }

    //handle result of run time permission
        @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == Activity.RESULT_OK) {

                if (requestCode == IMAGE_PICK_CODE) {
                    //set image to image view
                    //image_v.setImageURI(data.getData());
                    Uri selectedImageUri = data.getData();
                    if (selectedImageUri != null) {
                        Log.d("UploadFragment", "Output Uri:" + selectedImageUri);
                        Bitmap bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImageUri);
                        } catch (IOException e) {
                            e.printStackTrace();

                        }
                        Mat selected_image = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
                        Utils.bitmapToMat(bitmap, selected_image);
                        //output                                          input
                        selected_image = facialExpressionRecognition.recognizePhoto(selected_image);
                        selected_image=age_gender_recognition.recognizePhoto1(selected_image);
                        // now convert returned selected_image to bitmap
                        Bitmap bitmap1 = null;
                        bitmap1 = Bitmap.createBitmap(selected_image.cols(), selected_image.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(selected_image, bitmap1);
                        //set imageView with this bitmap1
                        image_v.setImageBitmap(bitmap1);

                    }

                }
            }
        }



}










