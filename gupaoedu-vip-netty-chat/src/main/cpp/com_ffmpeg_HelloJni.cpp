#include <stdio.h>
#include "include/com_ffmpeg_HelloJni.h"
#include "JavaCallHelper.h"
#include "HelloJni2.h"

#define DELETE(object) if(object){delete object; object = 0;}

JavaVM *javaVM = 0;
JavaCallHelper *javaCallHelper = 0;

/**
 * 动态注册
 */
JNINativeMethod methods[] = {
        {( char *)"onPreviewFrame", ( char *)"([BII)V",                 (void *) onPreviewFrame},
        {( char *)"startPublish",   ( char *)"(Ljava/lang/String;II)V", (void *) startPublish},
        {( char *)"stopPublish",    ( char *)"()V",                     (void *) stopPublish}
};

/**
 * 动态注册
 * @param env
 * @return
 */
jint registerNativeMethod(JNIEnv *env) {
    jclass cl = env->FindClass("com/ffmpeg/HelloJni2");
    if ((env->RegisterNatives(cl, methods, sizeof(methods) / sizeof(methods[0]))) < 0) {
        return -1;
    }
    return 0;
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    javaVM = vm;
    printf("JNI_OnLoad.\n");
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

JNIEXPORT void JNICALL Java_com_ffmpeg_HelloJni_sayHello
  (JNIEnv *env, jobject instance){
    printf("hello world.\n");

    javaCallHelper = new JavaCallHelper(javaVM, env, instance);
    javaCallHelper->onPrepared(THREAD_MAIN);
    javaCallHelper->onProgress(THREAD_MAIN,100);
    javaCallHelper->onError(THREAD_MAIN,-1);
    DELETE(javaCallHelper);
}
