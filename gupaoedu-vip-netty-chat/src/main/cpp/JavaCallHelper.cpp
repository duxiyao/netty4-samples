#include <JavaCallHelper.h>

JavaCallHelper::JavaCallHelper(JavaVM *javaVM_, JNIEnv *env_, jobject instance_) {
    this->javaVM = javaVM_;
    this->env = env_;
//    this->instance = instance_;//不能直接赋值！
    //一旦涉及到 jobject 跨方法、跨线程，需要创建全局引用
    this->instance = env->NewGlobalRef(instance_);
    jclass clazz = env->GetObjectClass(instance);
//    cd 进入 class所在的目录 执行： javap -s 全限定名,查看输出的 descriptor
//    xx\app\build\intermediates\classes\debug>javap -s com.netease.jnitest.Helper
    jmd_prepared = env->GetMethodID(clazz, "onPrepared", "()V");
    jmd_onError = env->GetMethodID(clazz, "onError", "(I)V");
    jmd_onProgress = env->GetMethodID(clazz, "onProgress", "([B)V");
    jmd_onProgress1 = env->GetMethodID(clazz, "onProgress", "(Ljava/nio/ByteBuffer;)V");

}

JavaCallHelper::~JavaCallHelper() {
    javaVM = 0;
    env->DeleteGlobalRef(instance);
    instance = 0;
}

void JavaCallHelper::onPrepared(int threadMode) {
    if (threadMode == THREAD_MAIN) {
        //主线程
        env->CallVoidMethod(instance, jmd_prepared);
    } else {
        //子线程
        //当前子线程的 JNIEnv
        JNIEnv *env_child;
        javaVM->AttachCurrentThread((void**)&env_child, 0);
        env_child->CallVoidMethod(instance, jmd_prepared);
        javaVM->DetachCurrentThread();
    }
}

void JavaCallHelper::onError(int threadMode, int errorCode) {
    if (threadMode == THREAD_MAIN) {
        //主线程
        env->CallVoidMethod(instance, jmd_onError, errorCode);
    } else {
        //子线程
        //当前子线程的 JNIEnv
        JNIEnv *env_child;
        javaVM->AttachCurrentThread((void**)&env_child, 0);
        env_child->CallVoidMethod(instance, jmd_onError, errorCode);
        javaVM->DetachCurrentThread();
    }
}

void JavaCallHelper::onProgress(int threadMode, jbyteArray progress) {
    if (threadMode == THREAD_MAIN) {
        //主线程
        env->CallVoidMethod(instance, jmd_onProgress, progress);
    } else {
        //子线程
        //当前子线程的 JNIEnv
        JNIEnv *env_child;
        javaVM->AttachCurrentThread((void**)&env_child, 0);
        env_child->CallVoidMethod(instance, jmd_onProgress, progress);
        javaVM->DetachCurrentThread();
    }
}

void JavaCallHelper::onProgress(int threadMode, jobject buffer) {
    if (threadMode == THREAD_MAIN) {
        //主线程
        env->CallVoidMethod(instance, jmd_onProgress1, buffer);
    } else {
        //子线程
        //当前子线程的 JNIEnv
        JNIEnv *env_child;
        javaVM->AttachCurrentThread((void**)&env_child, 0);
        env_child->CallVoidMethod(instance, jmd_onProgress1, buffer);
        javaVM->DetachCurrentThread();
    }
}

void JavaCallHelper::onGet264Data(int size,uint8_t *d){
    jbyteArray data = env->NewByteArray(size);                  //创建与buffer容量一样的byte[]
    env->SetByteArrayRegion(data, 0, size, (jbyte*)d);                //数据拷贝到data中
    onProgress(THREAD_MAIN,data);
    env->DeleteLocalRef(data);
}

