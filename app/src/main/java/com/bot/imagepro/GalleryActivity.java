package com.bot.imagepro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {
    ViewPager mViewPager;
    //create a new array list
    ArrayList<String> filePath=new ArrayList<>();
    ViewPageAdapter viewPageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        //we will create new file for path imagepro
        File folder=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ImagePro");
        //we will create new function which will add all file to array list
        createFileArray(folder);
        mViewPager=(ViewPager)findViewById(R.id.viewPagerMain);
        viewPageAdapter=new ViewPageAdapter(GalleryActivity.this,filePath);
        //set adapter for viewpager
        mViewPager.setAdapter(viewPageAdapter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    private void createFileArray(File folder) {
        //convert file to listfile

        File listFile[] = folder.listFiles();
        //if it is not empty
        //loop through each image
        if(listFile !=null){
            for (int i=0;i<listFile.length;i++){
                filePath.add(listFile[i].getAbsolutePath());

            }
        }
    }
}