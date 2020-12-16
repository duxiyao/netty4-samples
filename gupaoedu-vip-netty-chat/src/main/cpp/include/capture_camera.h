#include <jni.h>

int capture();

#include <stdint.h>

#ifndef CPP_CAPTURE_CAMERA_H
#define CPP_CAPTURE_CAMERA_H

class CaptureHelper {
public:
    CaptureHelper(JavaVM *javaVM_, JNIEnv *env_, jobject instance_);

    ~CaptureHelper();

    void onGet264Data(int size,uint8_t *data);

    void calcStart();
    void calcEnd();
private:
    JavaVM *javaVM;
    JNIEnv *env;
    jobject instance;
    jmethodID jmd_on_get_data;
    jmethodID jmd_on_calc_start;
    jmethodID jmd_on_calc_end;


};

#endif //CPP_CAPTURE_CAMERA_H
