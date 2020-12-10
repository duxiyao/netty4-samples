

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
                    jint height) {
    printf("onPreviewFrame.\n");
//    if (NULL != videoPublisher && videoPublisher->isTransform()) {
//        jbyte *yuv420Buffer = env->GetByteArrayElements(yuvArray, 0);
//        videoPublisher->EncodeBuffer((unsigned char *) yuv420Buffer);
//        env->ReleaseByteArrayElements(yuvArray, yuv420Buffer, 0);
//    }
}