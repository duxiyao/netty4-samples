package com.ffmpeg;

public class HelloJni2 {

    public native void startPublish(String stream, int width, int height);

    public native void stopPublish();

    public native void onPreviewFrame(byte[] yuvData, int width, int height);
}
