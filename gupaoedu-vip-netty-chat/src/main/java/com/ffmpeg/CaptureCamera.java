package com.ffmpeg;

public class CaptureCamera {
    static {
        System.loadLibrary("myffmpeg");
    }

    public native void startCapture();

    public void onGetData(byte[] data){
        System.out.println(data.length);
    }

    public static void main(String[] args) {
        new CaptureCamera().startCapture();
    }
}
