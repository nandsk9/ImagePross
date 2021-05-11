package com.bot.imagepro;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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
    private static final int RESULT_OK = 0;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ImageView mImageView;
    Button mUploadBtn;
    private static final int IMAGE_PICK_CODE=1000;
    private static final int PERMISSION_CODE=1001;

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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parentView = inflater.inflate(R.layout.fragment_upload, container, false);

        mImageView= parentView.findViewById(R.id.imageView1);
        mUploadBtn=parentView.findViewById(R.id.upload_image_btn);
        //handle button click
        mUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Check run time permission
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if (getContext().checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED){
                        //permission not granted ,request it.
                        String[] permissions ={Manifest.permission.READ_EXTERNAL_STORAGE};
                        //show popup for runtime permissions
                        requestPermissions(permissions,PERMISSION_CODE);

                    }
                    else {
                        //permission already granted
                        pickImageFromGallery();
                    }

                }
                else {
                    // system os is less then marshmallow
                    pickImageFromGallery();
                }
                //loading.. dialog on upload image
                //loadingDialog.startLoadingDialog();
                //Handler handler = new Handler();
                //handler.postDelayed(new Runnable() {
                //@Override
                // public void run() {
                // loadingDialog.dismissDialog();
                //}
                //},1000);


            }

        });



        return parentView;
    }




    private void pickImageFromGallery() {
        //intent to pick image
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_CODE);


    }
    // handle result of runtime permission


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case PERMISSION_CODE:{
                if (grantResults.length>0 && grantResults[0]==
                        PackageManager.PERMISSION_GRANTED){
                    //PERMISSION was granted
                    pickImageFromGallery();
                }
                else {
                    //permission denied
                    Toast.makeText(getContext(),"Permission denied...!",Toast.LENGTH_SHORT).show();
                }

            }
        }
    }
    //handle result of run time permission

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            //set image to image view
            mImageView.setImageURI(data.getData());

        }
    }
}