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
private:
    JavaVM *javaVM;
    JNIEnv *env;
    jobject instance;
    jmethodID jmd_on_get_data;


};

#endif //CPP_CAPTURE_CAMERA_H
