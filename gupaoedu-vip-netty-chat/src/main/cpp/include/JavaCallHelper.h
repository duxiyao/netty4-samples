//
// Created by Administrator on 2020/6/30.
//

#include <stdint.h>
//标记线程模式
#define  THREAD_MAIN 1
#define  THREAD_CHILD 2

#ifndef CPP_JAVACALLHELPER_H
#define CPP_JAVACALLHELPER_H

#include <jni.h>

class JavaCallHelper {
public:
    JavaCallHelper(JavaVM *javaVM_, JNIEnv *env_, jobject instance_);

    ~JavaCallHelper();

    void onPrepared(int threadMode);

    void onError(int threadMode, int errorCode);

    void onProgress(int threadMode, jbyteArray pro);

    void onProgress(int threadMode, jobject buffer);

    void onGet264Data(int size,uint8_t *data);
private:
    JavaVM *javaVM;
    JNIEnv *env;
    jobject instance;
    jmethodID jmd_prepared;
    jmethodID jmd_onError;
    jmethodID jmd_onProgress;
    jmethodID jmd_onProgress1;


};


#endif //CPP_JAVACALLHELPER_H
