#include <stdio.h>
#include "include/com_ffmpeg_HelloJni.h"

JNIEXPORT void JNICALL Java_com_ffmpeg_HelloJni_sayHello
  (JNIEnv *, jobject){
    printf("hello world.\n");
}