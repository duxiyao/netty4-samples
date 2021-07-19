package com.stun.test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.LockSupport;

/**
 * 用semaphore 实现停车场提示牌功能。
 * <p>
 * <p>
 * 每个停车场入口都有一个提示牌，上面显示着停车场的剩余车位还有多少，当剩余车位为0时，不允许车辆进入停车场，直到停车场里面有车离开停车场，这时提示牌上会显示新的剩余车位数。
 * <p>
 * <p>
 * <p>
 * 业务场景 ：
 * <p>
 * 1、停车场容纳总停车量10。
 * <p>
 * 2、当一辆车进入停车场后，显示牌的剩余车位数响应的减1.
 * <p>
 * 3、每有一辆车驶出停车场后，显示牌的剩余车位数响应的加1。
 * <p>
 * 4、停车场剩余车位不足时，车辆只能在外面等待。
 */
public class TestSemaphore {
    //停车场同时容纳的车辆10
    private static Semaphore semaphore = new Semaphore(10);
    private static Semaphore semaphore1 = new Semaphore(0);
    static CountDownLatch c = new CountDownLatch(2);
//    static CountDownLatch c=new CountDownLatch(1);

    static void test1() throws InterruptedException {

//        semaphore1.release();
//        semaphore1.acquire();
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                semaphore1.release();
                System.out.println("release");
                semaphore1.acquire();
                System.out.println("re acquire");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        semaphore1.acquire();
    }

    private static void test2() throws InterruptedException {

        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            c.countDown();
        }).start();
        c.await();
    }

    private static void test3() throws InterruptedException {

        for (int i = 0; i < 5; i++) {

            int finalI = i;
            new Thread(() -> {
                System.out.println("t" + finalI);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    c.await();
                    System.out.println("end t" + finalI);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        new Thread(() -> {
            System.out.println("countDown" );
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            c.countDown();
            System.out.println(" end countDown" );
        }).start();

    }

    public static void main(String[] args) throws InterruptedException {

//        test1();
        test3();
        System.out.println("123");

//        test2();
        if (true)
            return;
        //模拟100辆车进入停车场
        for (int i = 0; i < 100; i++) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        System.out.println("====" + Thread.currentThread().getName() + "来到停车场");
                        if (semaphore.availablePermits() == 0) {
                            System.out.println("车位不足，请耐心等待");
                        }
                        semaphore.acquire();//获取令牌尝试进入停车场
                        System.out.println(Thread.currentThread().getName() + "成功进入停车场");
                        Thread.sleep(new Random().nextInt(10000));//模拟车辆在停车场停留的时间
                        System.out.println(Thread.currentThread().getName() + "驶出停车场");
                        semaphore.release();//释放令牌，腾出停车场车位
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, i + "号车");
            thread.start();
        }
    }

}
