package com.stun.test;

import java.net.InetSocketAddress;

//https://repository.jboss.org/nexus/content/repositories/thirdparty-releases/net/java/stun4j/stun4j/1.0.MOBICENTS/
//https://www.it1352.com/977010.html
//https://www.jianshu.com/p/84e8c78ca61d
public class Test {

    public static void main(String[] args) throws Exception {
        InetSocketAddress test;
        test = stun.getPublicAddress(40446);
        System.out.println("STUN: " + test.toString());
    }
}
