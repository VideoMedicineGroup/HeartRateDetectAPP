package com.example.heartratedect;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * welcome layout Activity
 * Created by VideoMedicine Group on 2017/9/3.
 * 设置APP启动动画
 * @author GqGAO
 */

public class FormatUtil {

    private static HorizontalProgressBarWithNumber horizontalProgressBarWithNumber;
    private  static String path;
    private static String Tag = "FormatUtil";
    private static String fileName;

    static {
        System.loadLibrary("native-lib");
    }

    public static native float stringFromJNI(String str);

    private static final int MSG_PROGRESS_UPDATE = 0x110;
    //文件存贮
    public static  void videoRename(File recAudioFile) {

      path = Environment.getExternalStorageDirectory()
        .getAbsolutePath()+  "/HeartRateDect/video/"+ "SucessVideo" + "/";
         fileName = new SimpleDateFormat("yyyyMMddHHmmss")
        .format(new Date()) + ".avi";
        File out = new File(path);
        if (!out.exists()){
            out.mkdirs();
            }
        out = new File(path, fileName);
        if(recAudioFile.exists())
        recAudioFile.renameTo(out);
    }

    //计时器 一位补位
     public static String format(int num){
        String s = num + "";
        if (s.length() == 1) {
            s = "0" + s;
            }
        return s;
     }

    /*  The most important method to compute
     the heartrate and return the value*/
    public static float GetRate(){
        String videoPath;
        //./storage/emulated/0/HeartRateDect/video/SucessVideo/Video.avi
        String testPath="/storage/emulated/0/HeartRateDect/video/SucessVideo/videoMJPG.avi";
        float heartRate;
        heartRate = 66.66f;
        videoPath = path + fileName;
        Log.i(Tag,"videoPath"+videoPath);
        heartRate = stringFromJNI(testPath);
        Log.i("HeartRateValue",""+heartRate);
        return heartRate;
    }

    private static Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            int progress = horizontalProgressBarWithNumber.getProgress();

            horizontalProgressBarWithNumber.setProgress(++progress);

            Log.i(Tag,"加一");
            if (progress >= 100) {
                mHandler.removeMessages(MSG_PROGRESS_UPDATE);
            }
            mHandler.sendEmptyMessageDelayed(MSG_PROGRESS_UPDATE, 100);
        }
    };
}
