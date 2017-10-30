package com.example.heartratedect;


import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * main layout Activity
 * Created by VideoMedicine Group on 2017/9/3.
 * 心率检测-录视频-检测-出数据
 * @author GqGAO
 */
public class CameraActivity extends AppCompatActivity {

    private static String tag ="CameraActivity";
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private Camera.Parameters mParams;
    private MediaRecorder mediaRecorder;     //录制视频类
    private Button vedioButton;                    //摄像按钮   
    private ImageButton imageButton;
    private TextView timeTextView;
    protected boolean isPreview= false;             //摄像区域是否准备良好  
    private boolean isRecording = true;           // true表示没有录像，点击开始；false表示正在录像，点击暂停  
    private boolean bool;
    private int hour = 0;
    private int minute = 0;//计时专用  
    private int second = 0;
    private File mRecVedioPath;
    private File mRecAudioFile;
    private MySurfaceView surfaceView;
    private float temp;
    private int preview_width;
    private int preview_height;
    private String HeartRate;      //心率值
    private TextView HeartRateShow; //心率显示框
    private static HorizontalProgressBarWithNumber horizontalProgressBarWithNumber;
    private static final int MSG_PROGRESS_UPDATE = 0x110;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //  getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);
        Toast.makeText(CameraActivity.this,"请将手机靠近脸部，使圆框可以整好显示脸部，并按下下方的按钮",Toast.LENGTH_LONG).show();

        // 设置文件路径
        mRecVedioPath =new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath()+"/HeartRateDect/video/ErrorVideo/");
        if(!mRecVedioPath.exists()){
            mRecVedioPath.mkdirs();
        }
        //　这个貌似是进度条的设置，在按完开始按钮之后的一些操作
        horizontalProgressBarWithNumber = (HorizontalProgressBarWithNumber)findViewById(R.id.progressbar);
        horizontalProgressBarWithNumber.setVisibility(View.INVISIBLE);
        imageButton = (ImageButton)findViewById(R.id.image_button);
        timeTextView =(TextView) findViewById(R.id.camera_time);
        HeartRateShow = (TextView)findViewById(R.id.HeartRateShow);
        timeTextView.setVisibility(View.VISIBLE);

        //  vedioButton = (Button) findViewById(R.id.camera_vedio);
        //初始化摄像头  
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewFrameRate(25);// 每秒25帧
        // 设置图像的压缩格式
        parameters.setPictureFormat(ImageFormat.JPEG);
        camera.setParameters(parameters);
        // 设置摄像机的镜头方向，为垂直方向
        camera.setDisplayOrientation(90);

        // 创建surfaceView类的对象
        surfaceView=(MySurfaceView)findViewById(R.id.camera_mysurfaceview);
        SurfaceHolder cameraSurfaceHolder = surfaceView.getHolder();
        cameraSurfaceHolder.addCallback(new SurfaceHolder.Callback(){

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {

                    camera.setPreviewDisplay(holder);
                    isPreview=true;
                    camera.startPreview();

                }catch(IOException e){
                    e.printStackTrace();
                }
                surfaceHolder=holder;
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
                surfaceHolder = holder;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (camera != null){
                    if(isPreview){
                        camera.setPreviewCallback(null);
                        camera.stopPreview();
                        isPreview=false;
                    }
                    Log.i(tag,"===== 4 =====");
                    camera.release();
                    camera=null;// 记得释放Camera  
                }
                surfaceView=null;
                surfaceHolder=null;
                mediaRecorder=null;
            }
        });
        //开发时建议设置  
        // This method was deprecated in API level 11. this is ignored, this value is set automatically when needed.   
        cameraSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRecording){
                    if(isPreview) {
                        camera.stopPreview();
                        camera.release();
                        camera=null;
                    }
                    second=0;
                    minute=0;
                    hour=0;
                    bool=true;
                    if(null==mediaRecorder){
                        mediaRecorder= new MediaRecorder();
                    }else{
                        mediaRecorder.reset();
                    }

                    HeartRateShow.setText("Heart Rate");
                    imageButton.setBackgroundResource(R.drawable.open);
                    camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                    camera.setDisplayOrientation(90);
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setPreviewFrameRate(25);// 每秒30帧  
                    camera.setParameters(parameters);

                    camera.unlock();
                    mediaRecorder.setCamera(camera);
                    mediaRecorder.setOrientationHint(270);
                    //表面设置显示记录媒体（视频）的预览  
                    // camera = deal(camera);
                    mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
                    //开始捕捉和编码数据到setOutputFile（指定的文件） 
                    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                    //设置用于录制的音源  
                    //mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
                    // mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                    //设置相机参数配置
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                    mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
                    //mediaRecorder.setAudioEncodingBitRate(5*1024*1024);
                    /*CamcorderProfile cProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
                                                    mediaRecorder.setProfile(cProfile);*/
                    // 设置要捕获的视频帧速率  
                    mediaRecorder.setVideoSize(640,480);
                    mediaRecorder.setVideoFrameRate(25);

                    Log.i(tag,"mediaRecorder set sucess");

                    try {
                        mRecAudioFile = File.createTempFile("Vedio",".avi",mRecVedioPath);
                    } catch(IOException e){
                        e.printStackTrace();
                    }
                    mediaRecorder.setOutputFile(mRecAudioFile.getAbsolutePath());
                    try {
                        mediaRecorder.prepare();
                        timeTextView.setVisibility(View.VISIBLE);
                        handler1.postDelayed(task,1000);
                        mediaRecorder.start();
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                    // vedioButton.setText("正在录制");
                    isRecording = !isRecording;

                    Log.i(tag,"=====开始录制视频=====");


                    handler1.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            bool = false;
                            Log.i(tag,"789456123");
                            mediaRecorder.stop();
                            timeTextView.setVisibility(View.INVISIBLE);
                            // timeTextView.setText(FormatUtil.format(hour)+":"+FormatUtil.format(minute)+":"+ FormatUtil.format(second));
                            mediaRecorder.release();
                            mediaRecorder = null;
                            FormatUtil.videoRename(mRecAudioFile);
                            Log.e(tag, "=====录制完成，已保存=====");
                            isRecording =!isRecording;
                            //    vedioButton.setText("完成");
                             /* HeartRateShow.setVisibility(View.INVISIBLE);
                            horizontalProgressBarWithNumber.setVisibility(View.VISIBLE);
                            mHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);*/
                            temp = FormatUtil.GetRate();
                              /*  HeartRateShow.setVisibility(View.VISIBLE);
                             horizontalProgressBarWithNumber.setVisibility(View.INVISIBLE);*/
                            HeartRate = Float.toString(temp);
                            HeartRateShow.setText(HeartRate);
                            imageButton.setBackgroundResource(R.drawable.close);
                            //   isRecording = !isRecording;
                        }
                    },11000);


                }/*else{
                   //点击停止录像  
                   bool = false;
                   mediaRecorder.stop();
                   //timeTextView.setText(FormatUtil.format(hour)+":"+FormatUtil.format(minute)+":"+ FormatUtil.format(second));
                   timeTextView.setVisibility(View.INVISIBLE);
                   mediaRecorder.release();
                   mediaRecorder = null;
                   FormatUtil.videoRename(mRecAudioFile);
                   Log.e(tag, "=====录制完成，已保存=====");
                   isRecording =!isRecording;
                   try{
                       camera = Camera.open();
                       Camera.Parameters parameters = camera.getParameters();
                       camera.setParameters(parameters);
                       camera.setPreviewDisplay(surfaceHolder);
                       camera.startPreview();
                       isPreview = true;
                   } catch (Exception e){
                       e.printStackTrace();
                   }
                   imageButton.setBackgroundResource(R.drawable.close);
                   //vedioButton.setText("录制视频");
               }*/
            }
        });
    }

    /*
     * 定时器设置，实现计时 
     */
    private Handler handler1 =new Handler();
    private Handler handler = new Handler();
    private Runnable task = new Runnable() {
        public void run() {
            if (bool) {
                handler.postDelayed(this,1000);
                second++;
                if(second >= 60){
                    minute++;
                    second = second % 60;
                }
                if (minute >= 60) {
                    hour++;
                    minute = minute % 60;
                }
                timeTextView.setText(FormatUtil.format(hour) + ":" + FormatUtil.format(minute) + ":" + FormatUtil.format(second));
            }
        }
    };

}