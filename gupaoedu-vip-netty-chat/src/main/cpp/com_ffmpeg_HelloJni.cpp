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
        {( char *)"onPreviewFrame", ( char *)"([BIILjava/nio/ByteBuffer;)V",                 (void *) onPreviewFrame},
        {( char *)"log",   ( char *)"(Ljava/lang/String;)V", (void *) log},
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
}

JNIEXPORT void JNICALL Java_com_ffmpeg_HelloJni_sayHello1
  (JNIEnv *env, jobject instance){
    jbyte pdata[2];
    pdata[0]=1;
    pdata[1]=11;

    jbyteArray data = env->NewByteArray(2);                  //创建与buffer容量一样的byte[]
    env->SetByteArrayRegion(data, 0, 2, pdata);                //数据拷贝到data中
    javaCallHelper->onProgress(THREAD_MAIN,data);

    printf("\nhello 0.\n");
    env->DeleteLocalRef(data);
    env->ReleaseByteArrayElements(data, pdata, 0);
    javaCallHelper->onError(THREAD_MAIN,-1);

    printf("hello 1.\n");
    void* myBuffer;
//    jbyte* pData1    = NULL;
    jobject buffer=env->NewDirectByteBuffer(myBuffer,2);
    printf("hello 2.\n");
//    jbyte* pData1    = (jbyte*) env->GetDirectBufferAddress(buffer); //获取buffer数据首地址
    jbyte* pData1    = (jbyte*) myBuffer;
    pData1[0]=2;
    pData1[1]=22;
//
    printf("hello 3.\n");
    javaCallHelper->onProgress(THREAD_MAIN,buffer);
    printf("hello 4.\n");
    env->DeleteLocalRef(buffer);
    DELETE(javaCallHelper);
    printf("hello 5.\n");
}
