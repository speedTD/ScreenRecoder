package com.developer.dinhduy.dembuocchan;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.CamcorderProfile;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static  final  int  REQUEST_CODE =1998;
    private static  final int REQUEST_PERMISSION=2003;
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private MediaRecorder mediaRecorder;
    private MediaProjectioncallback  mediaProjectioncallback;
    private static final SparseIntArray SPARSE_ARRAY=new SparseIntArray();


    private int Mscreen;
    private  static  int DISPLAY_WIDTH=720;
    private  static int DISPLAY_HIGHT=1080;
    static {
        SPARSE_ARRAY.append(Surface.ROTATION_0,90);
        SPARSE_ARRAY.append(Surface.ROTATION_90,0);
        SPARSE_ARRAY.append(Surface.ROTATION_180,270);
        SPARSE_ARRAY.append(Surface.ROTATION_270,180);
    }
    //view

    private ToggleButton toggleButton;
    private VideoView videoView;
    private String URI_VIDEO="";
    private RelativeLayout Root_layout;



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       init();
        Log.d("AAA", "onClick: z1");
       toggleButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Log.d("AAA", "onClick: z1");
               //check Permission
               if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                       + ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.RECORD_AUDIO)
                       != PackageManager.PERMISSION_GRANTED){
                   Log.d("AAA", "onClick: 1");
                   if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                           || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.RECORD_AUDIO)) {

                       Log.d("AAA", "onClick: 2");
                       toggleButton.setChecked(false);
                       Snackbar.make(Root_layout,"Permissions",Snackbar.LENGTH_INDEFINITE)
                               .setAction("OK", new View.OnClickListener() {
                                   @Override
                                   public void onClick(View view) {
                                       ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                Manifest.permission.RECORD_AUDIO
                                        },REQUEST_PERMISSION);

                                   }
                               }).show();
                   }
                   else {
                       ActivityCompat.requestPermissions(MainActivity.this,
                               new String[]{
                                       Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                       Manifest.permission.RECORD_AUDIO
                               },REQUEST_PERMISSION);

                       Log.d("AAA", "onClick: 3");

                   }

               }
               else{
                   Log.d("AAA", "onClick: 2ok");
                   try {
                       Thread.sleep(2000);
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }
                   ToggleButtonShare(view);
                   Toast.makeText(MainActivity.this, "Recodding......", Toast.LENGTH_SHORT).show();
               }

           }
       });


    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void ToggleButtonShare(View view) {
        if(((ToggleButton)view).isChecked()){
            Log.d("AAA", "recoder");
            initRecoder();
            ScreenRecoder();
        }else {
            mediaRecorder.stop();
            mediaRecorder.reset();
            StopRecoder();
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(new File(URI_VIDEO)));
            sendBroadcast(intent);
            //play view video
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoURI(Uri.parse(URI_VIDEO));
            videoView.start();
        //    File from = new File(URI_VIDEO.toString());
        }
    }





    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void ScreenRecoder() {
        if(mediaProjection==null){
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(),REQUEST_CODE);
            return;
        }
        virtualDisplay=CREATE_VIRUAL_DISPLAY();
        mediaRecorder.start();
    }
    MediaCodec codec = null;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private VirtualDisplay CREATE_VIRUAL_DISPLAY() {


        Log.d("AAA", "CREATE_VIRUAL_DISPLAY: "+mediaRecorder.getSurface());
        return mediaProjection.createVirtualDisplay("MainActivity",
                DISPLAY_WIDTH,DISPLAY_HIGHT,Mscreen,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.getSurface(),null,null);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initRecoder() {
        try {

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

            //Uri

            URI_VIDEO= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    +new StringBuilder("/FILE_")
                    .append(new SimpleDateFormat("dd-MM-yyyy-hh_mm_ss")
                    .format(new Date()))
                    .append(".mp4")
                    .toString();
            mediaRecorder.setOutputFile(URI_VIDEO);
            mediaRecorder.setVideoSize(DISPLAY_WIDTH,DISPLAY_HIGHT);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setVideoEncodingBitRate(3*1024*1024);
            mediaRecorder.setVideoFrameRate(40);

            int Rotation=getWindowManager().getDefaultDisplay().getRotation();
          /*  int Oritation= SPARSE_ARRAY.get(Rotation+90);
            mediaRecorder.setOrientationHint(Oritation);
*/


            mediaRecorder.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    int height;
    int width;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void init(){
        toggleButton=(ToggleButton) findViewById(R.id.Btn_Screen);
        videoView=(VideoView) findViewById(R.id.videoview);

        DisplayMetrics displayMetrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mediaRecorder=new MediaRecorder();
        Mscreen=displayMetrics.densityDpi;
         height = displayMetrics.heightPixels-10;
         width = displayMetrics.widthPixels-10;
        Log.d("AAA", "init: "+height+" ...."+width);
        mediaProjectionManager=(MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Root_layout=(RelativeLayout) findViewById(R.id.Root_Layout);
      /*  Root_layout.addView(toggleButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
*/



    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode!=REQUEST_CODE){
            Toast.makeText(this, "ERROR TRY AGAIN", Toast.LENGTH_SHORT).show();
            return;
        }
        if(resultCode!=RESULT_OK){
            Toast.makeText(this, "ERROR TRY AGAIN", Toast.LENGTH_SHORT).show();
            toggleButton.setChecked(false);
            return;
        }
        mediaProjectioncallback=new MediaProjectioncallback();
        mediaProjection=mediaProjectionManager.getMediaProjection(resultCode,data);
        mediaProjection.registerCallback(mediaProjectioncallback,null);
        virtualDisplay=CREATE_VIRUAL_DISPLAY();
        mediaRecorder.start();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class MediaProjectioncallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            if(toggleButton.isChecked()){
                toggleButton.setChecked(false);
                mediaRecorder.stop();
                mediaRecorder.reset();

            }
            mediaProjection=null;
           
            StopRecoder();
            super.onStop();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void StopRecoder() {
        if(virtualDisplay==null){
            return;
        }
        virtualDisplay.release();
        Distroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void Distroy() {
        if(mediaProjection!=null){
           mediaProjection.unregisterCallback(mediaProjectioncallback);
           mediaProjection.stop();
           mediaProjection=null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case REQUEST_PERMISSION :

                if(grantResults.length>0&&(grantResults[0]+grantResults[1]==PackageManager.PERMISSION_GRANTED)){
                    ToggleButtonShare(toggleButton);
                }else {
                    toggleButton.setChecked(false);
                    Snackbar.make(Root_layout,"Permission",Snackbar.LENGTH_INDEFINITE)
                            .setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                    Manifest.permission.RECORD_AUDIO
                                            },REQUEST_PERMISSION);

                                }
                            }).show();
                }
                return;
        }
    }
    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }
}

