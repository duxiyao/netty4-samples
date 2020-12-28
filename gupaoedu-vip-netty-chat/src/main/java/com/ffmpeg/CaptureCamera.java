package com.ffmpeg;

import com.StreamNum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.server.im.android.clinet.sdk.IMOnReceive;
import com.server.im.android.clinet.sdk.IMSdk;
import com.server.im.android.clinet.sdk.codec.IMEncoder;
import com.server.im.android.clinet.sdk.model.PkgInfo;

import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CaptureCamera {
    static {
        System.loadLibrary("myffmpeg");
    }

    private volatile long time, timeSecond;
    private StreamNum streamNum = new StreamNum();

    static ExecutorService executorService = Executors.newSingleThreadExecutor();

    static String uid = "4c69bacb-cd0c-4ea9-ab02-17ba6816f6c8", toUid = "4c69bacb-cd0c-4ea9-ab02-17ba6816f6c9";

    public native void startCapture();

    private long tmp;

    public void onCalcEnd() {
        long cur = System.currentTimeMillis();
        System.out.println("-------delta=" + (cur - tmp));
    }

    public void onCalcStart() {
        tmp = System.currentTimeMillis();
    }

    public void onGetData(byte[] data) {
//        System.out.println(data.length);
//        System.out.println("send-----------"+Thread.currentThread().getName());

        //计算帧间耗时
//        if (time == 0) {
//            time = System.currentTimeMillis();
//            timeSecond = System.currentTimeMillis();
//        } else {
//            long cur = System.currentTimeMillis();
//            long delta = cur - time;
//            time = cur;
//            streamNum.add(delta);
//            long deltaTime = cur - timeSecond;
//            if (deltaTime > 5000) {
//                timeSecond = cur;
//                StringBuilder sb = new StringBuilder();
//                sb.append("deltaTime:" + deltaTime);
//                sb.append("\n");
//                sb.append("streamNum:" + streamNum.toString());
//
//                System.err.println(sb.toString());
//                streamNum.clear();
//            }
//        }

        executorService.execute(() -> {
            IMSdk.getInstance().getChat().sendVideo(toUid, data);
        });
    }

    public static void main(String[] args) {
//        System.out.println("start-----------"+Thread.currentThread().getName());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mimeType", "video/avc");
        jsonObject.put("videoWidth", 1920);
        jsonObject.put("videoHeight", 1080);
//        jsonObject.put("videoWidth", 1280);
//        jsonObject.put("videoHeight", 720);
        jsonObject.put("keyBitRate", 800 * 1000);
        jsonObject.put("fps", 30);
//            jsonObject.put("iFrameInterval", 1);
        final String json = jsonObject.toString();
        System.out.println(json);
        IMSdk.getInstance().init(uid, null, () -> {
            IMSdk.getInstance().getChat().sendTransparentTxt(toUid, json);
            new Thread(() -> {
                System.out.println("start capture-----------" + Thread.currentThread().getName());
                new CaptureCamera().startCapture();
            }).start();
        });
        IMSdk.getInstance().setImOnReceive(new IMOnReceive() {
            @Override
            public void onReceive(PkgInfo pkgInfo) {
                int len = 0;
                byte[] data = pkgInfo.getData();
                if (pkgInfo != null) {
                    len = data.length;
                }
                switch (pkgInfo.getType()) {
                    case PkgInfo.TYPE_TRANSFER_TRANSPARENT_TXT:
                        //: 2020-10-17 解析数据，设置format
                        try {
                            String recejson = new String(data, Charset.defaultCharset());
                            System.out.println("rece------"+recejson);
                            JSONObject jsonObject = JSON.parseObject(recejson);
                            if (jsonObject.containsKey("msgType")) {
                                String msgType = jsonObject.getString("msgType");
                                switch (msgType) {
                                    case "ttl":
                                        executorService.execute(() -> {
                                            IMSdk.getInstance().getChat().sendTransparentTxt(toUid, recejson);
                                        });
                                        break;
                                    default:
                                        break;
                                }
                            }
//                            System.out.println("received  " + recejson + " " + System.currentTimeMillis());
                            
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }
}
