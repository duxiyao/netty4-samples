package com.server.im.android.clinet.audio;

import java.util.LinkedList;
import java.util.Stack;

public class DataQueue {


    private LinkedList<byte[]> encoderDatas = new LinkedList<>();
    private LinkedList<byte[]> encoderDatasTmp = new LinkedList<>();
    private LinkedList<byte[]> originData = new LinkedList<>();

    public synchronized byte[] getData() {
        if (encoderDatasTmp.size() > 0) {
            return encoderDatasTmp.poll();
        }
        return encoderDatas.poll();
    }

    public synchronized void put(byte[] data) {
        short p = getShort(data);
        int unsinedShort = ((int) p) & 0xffff;

        System.out.println(" p=" + unsinedShort + " data.len=" + data.length);

        if (unsinedShort == 0 && encoderDatas.size() > 0) {
            encoderDatasTmp.clear();
            encoderDatasTmp.addAll(encoderDatas);
            encoderDatas.clear();
        }

        if (encoderDatas.size() == 0)
            encoderDatas.add(data);
        else {
            Stack<byte[]> tmp = new Stack<>();
            boolean flag = true;
            do {
                byte[] last = encoderDatas.pollLast();
                short pLast = getShort(data);
                int unsLast = ((int) pLast) & 0xffff;

                tmp.push(last);
                if (unsinedShort >= unsLast) {
                    encoderDatas.add(tmp.pop());
                    encoderDatas.add(data);
                    flag = false;
                }
            } while (flag);
            while (!tmp.isEmpty()) {
                encoderDatas.add(tmp.pop());
            }
        }
    }

    private static short getShort(byte[] data) {
        short value = 0;

        value |= (data[0] & 0xFF);
        value = (short) (value << 8);
        value |= (data[1] & 0xFF);

        return value;
    }

    public synchronized void release() {
        encoderDatas.clear();
        encoderDatasTmp.clear();
    }

    /*
    只是测试时使用
     */
    public synchronized int size() {
        return encoderDatas.size();
    }

    public synchronized LinkedList<byte[]> hold() {
        originData.addAll(encoderDatas);
        return originData;
    }
}
