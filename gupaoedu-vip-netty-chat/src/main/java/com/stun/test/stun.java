package com.stun.test;

import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import net.java.stun4j.StunAddress;
import net.java.stun4j.StunException;
import net.java.stun4j.client.SimpleAddressDetector;

import java.util.Random;

public class stun {
    public static final int MAX_PORT_NUMBER = 65535;
    public static final int MIN_PORT_NUMBER = 1024;
    private static DatagramSocket socket = null;
    private static SimpleAddressDetector detector;

    public static synchronized InetAddress resolveInternetInterface(InetAddress dest) {

        InetAddress networkInterface = null;

        if (dest == null) {
            try {
                dest = InetAddress.getByName("78.12.2.61");
            } catch (UnknownHostException e) {
            }
        }

        if (dest != null) {
            socket.connect(dest, getRandomPortNumber());
            networkInterface = socket.getLocalAddress();
            socket.disconnect();
        }

        if (networkInterface == null || networkInterface.isAnyLocalAddress()) {
            try {
                networkInterface = InetAddress.getLocalHost();
            } catch (Exception ex) {
            }
        }
        return networkInterface;
    }

    public static synchronized InetAddress resolveInternetInterface() {
        return resolveInternetInterface(null);
    }

    public static int getRandomPortNumber() {
        return new Random().nextInt(MAX_PORT_NUMBER - MIN_PORT_NUMBER);
    }

    public static InetSocketAddress getPublicAddress(int localPort) throws Exception {
        InetSocketAddress resolvedAddr = null;
        String stunAddressStr = "stun.xten.com";
        String portStr = "3478";
        int stunPort = Integer.parseInt(portStr);

        StunAddress stunAddr = new StunAddress(stunAddressStr, stunPort);
        detector = new SimpleAddressDetector(stunAddr);

        System.out.println("Created a STUN Address detector for the following " + "STUN server: " + stunAddressStr + ":" + stunPort);

        detector.start();
        System.out.println("STUN server detector started;");

        StunAddress mappedAddress = null;
        try {
            mappedAddress = detector.getMappingFor(localPort);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("lala");
        detector.shutDown();
        if (mappedAddress != null) {
            System.out.println("stun: no nat detected");
            resolvedAddr = mappedAddress.getSocketAddress();

        } else {

            System.out.println("sun: nat detected, hitting the ip");
            String dstProperty = "78.22.22.61"; // put the ip of the target to hit
            InetAddress destination = null;
            try {
                destination = InetAddress.getByName(dstProperty);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            InetAddress publicHost = resolveInternetInterface(destination);
            resolvedAddr = new InetSocketAddress(publicHost, localPort);

        }

        return resolvedAddr;

    }

}
