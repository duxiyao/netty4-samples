package com.stun.test;

import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//https://repository.jboss.org/nexus/content/repositories/thirdparty-releases/net/java/stun4j/stun4j/1.0.MOBICENTS/
//https://www.it1352.com/977010.html
//https://www.jianshu.com/p/84e8c78ca61d
public class Test {

    static final ThreadPoolExecutor executorService =
            new ThreadPoolExecutor(0, Runtime.getRuntime().availableProcessors(),
                    10L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    public static void main(String[] args) throws Exception {
//        InetSocketAddress test;
//        test = stun.getPublicAddress(40446);
//        System.out.println("STUN: " + test.toString());
        //-Xmx256m
//        System.out.println(Runtime.getRuntime().maxMemory()/(1024*1024)+"");

        executorService.execute(()->{
            for(long i=0;i<Long.MAX_VALUE/1000000000;i++){

            }
            System.out.println("inner");
        });
        System.out.println("abc");

        try {
            if (executorService != null) {
                executorService.shutdown();
                executorService.shutdownNow().clear();
                executorService.getQueue().clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("end");
    }
}
