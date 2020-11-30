package com;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class TestJoin implements Runnable{

    @Override
    public void run() {
        System.out.println("t start");
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("t fin");
    }

    public static void main(String[] args) throws InterruptedException {
        LinkedList<Integer> a=new LinkedList<>();
//        a.push(1);
//        a.push(2);
//        a.push(3);
        a.pollLast();
        for(int o:a){
            System.out.println("for:"+o);
        }
//        System.out.println(a.poll());
//        System.out.println(a.poll());
//        System.out.println(a.poll());
//        System.out.println(a.pop());
//        System.out.println(a.pop());
        Thread t=new Thread(new TestJoin());
        t.start();
//        t.join();
        System.out.println("main fin");
    }
}
