/**
https://blog.csdn.net/zhangbangqian/article/details/78753126
https://blog.csdn.net/trent1985/article/details/39055291
https://www.jianshu.com/p/c867e4abcb5f 哪些jni对象需要手动释放
*/

void log(JNIEnv *env, jobject obj, jstring log) {

    const char *l = env->GetStringUTFChars(log, NULL);
    printf("%s\n",l);
    env->ReleaseStringUTFChars(log, l);
}
/**
 * 编码开始
 * @param env
 * @param obj
 * @param jmp4Path
 * @param width
 * @param height
 */
void startPublish(JNIEnv *env, jobject obj, jstring jmp4Path, jint width, jint height) {
    printf("startPublish.\n");
//    const char *mp4Path = env->GetStringUTFChars(jmp4Path, NULL);
//
//    if (videoPublisher == NULL) {
//        videoPublisher = new H264Publisher();
//    }
//    videoPublisher->InitPublish(mp4Path, width, height);
//    videoPublisher->StartPublish();
//
//    env->ReleaseStringUTFChars(jmp4Path, mp4Path);
}

/**
 * 编码结束
 * @param env
 * @param obj
 * @param jmp4Path
 * @param width
 * @param height
 */
void stopPublish(JNIEnv *env, jobject obj) {
    printf("stopPublish.\n");
//    if (NULL != videoPublisher) {
//        videoPublisher->StopPublish();
//        videoPublisher = NULL;
//    }
}

/**
 * 处理相机回调的预览数据
 * @param env
 * @param obj
 * @param yuvArray
 * @param width
 * @param height
 */
void onPreviewFrame(JNIEnv *env, jobject obj, jbyteArray yuvArray, jint width,
                    jint height,jobject buffer) {
    printf("onPreviewFrame.\n");
    jbyte* pData    = (jbyte*) env->GetDirectBufferAddress(buffer); //获取buffer数据首地址
    jlong dwCapacity  = env->GetDirectBufferCapacity(buffer);         //获取buffer的容量
    if(!pData)
    {
        printf("GetDirectBufferAddress() return null");
//        return NULL;
        return ;
    }
    printf("%d %d",pData[0],pData[1]);
    env->DeleteLocalRef(buffer);
    //jbyteArray data = env->NewByteArray(dwCapacity);                  //创建与buffer容量一样的byte[]
    //env->SetByteArrayRegion(data, 0, dwCapacity, pData);              //数据拷贝到data中
    //return data;

//    if (NULL != videoPublisher && videoPublisher->isTransform()) {
//        jbyte *yuv420Buffer = env->GetByteArrayElements(yuvArray, 0);
//        videoPublisher->EncodeBuffer((unsigned char *) yuv420Buffer);
//        env->ReleaseByteArrayElements(yuvArray, yuv420Buffer, 0);
//    }
}