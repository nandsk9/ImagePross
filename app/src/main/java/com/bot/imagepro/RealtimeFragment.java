package com.bot.imagepro;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RealtimeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RealtimeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button camera_button;
    private Button camera_button1;
    ImageView mImageView;


    public RealtimeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RealtimeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RealtimeFragment newInstance(String param1, String param2) {
        RealtimeFragment fragment = new RealtimeFragment();
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


        View parentView = inflater.inflate(R.layout.fragment_realtime, container, false);
        camera_button = parentView.findViewById(R.id.camera_btn);
        mImageView = parentView.findViewById(R.id.imageView);
       // camera_button1 = parentView.findViewById(R.id.camera_btn1);


        //loading .. dioalog calling
        LoadingDialog loadingDialog = new LoadingDialog(getActivity());
        camera_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), CameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                //loading ....dialog on scan face button
               loadingDialog.startLoadingDialog();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       loadingDialog.dismissDialog();
                    }
                },2000);
            }
        });
        //loading .. dioalog calling
        //LoadingDialog loadingDialog = new LoadingDialog(getActivity());
       // camera_button1.setOnClickListener(new View.OnClickListener() {
           // @Override
           // public void onClick(View view) {
               // startActivity(new Intent(getContext(), CameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                //loading ....dialog on scan face button
               // loadingDialog.startLoadingDialog();
               // Handler handler = new Handler();
               // handler.postDelayed(new Runnable() {
                 //   @Override
                  //  public void run() {
                   //     loadingDialog.dismissDialog();
                  //  }
               // },2000);
           // }
       // });


        return parentView;
    }
}

