#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <jni.h>
//#include <capture_camera.h>

#ifndef JNI_INIT_H
#define JNI_INIT_H

//jni

#define DELETE(object) if(object){delete object; object = 0;}

JavaVM *javaVM = 0;

/**
 * 动态注册
 */
static JNINativeMethod methods[] = {
//        {( char *)"startCapture",   ( char *)"()V", (int *) capture}
};

/**
 * 动态注册
 * @param env
 * @return
 */
static jint registerNativeMethod(JNIEnv *env) {
    jclass cl = env->FindClass("com/ffmpeg/CaptureCamera");
    if ((env->RegisterNatives(cl, methods, sizeof(methods) / sizeof(methods[0]))) < 0) {
        return -1;
    }
    return 0;
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    printf("jni_init.h JNI_OnLoad.\n");
    javaVM = vm;
    JNIEnv *env = NULL;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        printf("获取JNIEnv失败.\n");
        return -1;
    }
    //注册方法
    if (registerNativeMethod(env) != JNI_OK) {
        printf("注册方法失败.\n");
        return -1;
    }

    return JNI_VERSION_1_4;
}

#endif