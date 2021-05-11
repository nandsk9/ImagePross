package com.bot.imagepro;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    //ImageView mImageView;
    //Button mUploadBtn;
   // private static final int IMAGE_PICK_CODE=1000;
    //private static final int PERMISSION_CODE=1001;




    static {
        if(OpenCVLoader.initDebug()){
            Log.d("mainActivity","opencv is loaded");
        }
        else {
            Log.d("mainActivity","opencv is fail to load");
        }
    }

    //private Button camera_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setting fragment
        TabLayout tabLayout=findViewById(R.id.tabBar);
        TabItem tabRealtime =findViewById(R.id.tabRealtime);
        TabItem tabUpload = findViewById(R.id.tabUpload);
        ViewPager viewPager=findViewById(R.id.viewPager);
        PagerAdapter pagerAdapter= new PagerAdapter(getSupportFragmentManager(),
                tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);

        //one last step is to change the tabs view when the tab is selected or clicked
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


       // camera_button = findViewById(R.id.camera_btn);
        //loading .. dioalog calling
        //LoadingDialog loadingDialog = new LoadingDialog(MainActivity.this);
      //  camera_button.setOnClickListener(new View.OnClickListener() {
          //  @Override
          // public void onClick(View view) {
              //  startActivity(new Intent(MainActivity.this, CameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                //loading ....dialog on scan face button
               // loadingDialog.startLoadingDialog();
              //  Handler handler = new Handler();
               // handler.postDelayed(new Runnable() {
                  // @Override
                //    public void run() {
                     //   loadingDialog.dismissDialog();
                  //  }
              //  },2000);
           // }
        //});
        //upload image view
       // mImageView=findViewById(R.id.imageView1);
       // mUploadBtn=findViewById(R.id.upload_image_btn);
        //handle button click
       // mUploadBtn.setOnClickListener(new View.OnClickListener() {
           // @Override
           // public void onClick(View view) {
                //Check run time permission
                //if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                   // if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                        //permission not granted ,request it.
                        String[] permissions ={Manifest.permission.READ_EXTERNAL_STORAGE};
                        //show popup for runtime permissions
                       // requestPermissions(permissions,PERMISSION_CODE);

                   // }
                   // else {
                        //permission already granted
                     //   pickImageFromGallery();
                    //}

               // }
                //else {
                    // system os is less then marshmallow
                  //  pickImageFromGallery();
               // }
                //loading.. dialog on upload image
                //loadingDialog.startLoadingDialog();
                //Handler handler = new Handler();
                //handler.postDelayed(new Runnable() {
                    //@Override
                   // public void run() {
                       // loadingDialog.dismissDialog();
                    //}
                //},1000);


           // }

       // });



   // }

   // private void pickImageFromGallery() {
        //intent to pick image
       // Intent intent=new Intent(Intent.ACTION_PICK);
      //  intent.setType("image/*");
       // startActivityForResult(intent,IMAGE_PICK_CODE);


   // }
    // handle result of runtime permission


    //@Override
    //public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
      //  switch(requestCode){
         //   case PERMISSION_CODE:{
           //     if (grantResults.length>0 && grantResults[0]==
            //    PackageManager.PERMISSION_GRANTED){
                    //PERMISSION was granted
             //       pickImageFromGallery();
              //  }
              //  else {
                    //permission denied
                   // Toast.makeText(this, "Permission denied...!", Toast.LENGTH_SHORT).show();
              //  }

           // }
       // }
    //}
    //handle result of run time permission

    //@Override
    //protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


       // super.onActivityResult(requestCode, resultCode, data);
        //if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            //set image to image view
          //  mImageView.setImageURI(data.getData());

       // }
    }
}

//https://www.youtube.com/watch?v=3NVsN72gWJQ&list=PL0aoTDj9Nwgh0hTC3QBHwKtJuxl1veGyG&index=2//
//new
//my new test code
