#include <jni.h>"
#include "HRD.h"

extern "C"
JNIEXPORT jfloat JNICALL
Java_com_example_heartratedect_FormatUtil_stringFromJNI(
        JNIEnv *env,
        jobject /* this */,
        jstring jstr) {
    // 解析传入的string
    float heartRate;
    const char*video = env->GetStringUTFChars(jstr, NULL);
    HRD hrd;
    bool test = hrd.videoGet(video);
    if(hrd.videoGet(video)){
        hrd.gaussPyramid();
        hrd.idealBandPass();
        hrd.caculateHRValue();
        heartRate = hrd.getHRValue();
    }else{
        heartRate = -1.0f;
    }
    return heartRate;
}
