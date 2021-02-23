package com;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class WOL {


    public static void main(String[] args) throws IOException {
//        String macAddress = args[0];//54BF647ED56B
//        String targetIp = args[1];//115.171.85.136
//        String sport = args[2];//20000
        String macAddress = "54BF647ED56B";
        String targetIp = "115.171.85.136";
//        String targetIp = "www.qq.com";
        String sport = "20000";
        int port = Integer.parseInt(sport);
        wakeUp(macAddress, targetIp,port);
    }

    private static void wakeUp(String macAddress,String targetIp, int port) throws IOException {
        byte[] bytes = getMagicBytes(macAddress);
//        InetAddress address = getMulticastAddress();
//        InetAddress address = InetAddress.getLocalHost();
//        InetAddress address = InetAddress.getByName("www.qq.com");
        InetAddress address = InetAddress.getByName(targetIp);
        System.out.println(address.getHostName());
        System.out.println(address.getHostAddress());
        send(bytes, address, port);
    }

    private static void send(byte[] bytes, InetAddress addr, int port)
            throws IOException {
        DatagramPacket p = new DatagramPacket(bytes, bytes.length, addr, port);
        new DatagramSocket().send(p);
    }

    private static InetAddress getMulticastAddress()
            throws UnknownHostException {
        return InetAddress.getByAddress(new byte[] {-1, -1, -1, -1});
    }

    private static byte[] getMagicBytes(String macAddress) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        for (int i = 0; i < 6; i++)
            bytes.write(0xff);

        byte[] macAddressBytes = parseHexString(macAddress);
        for (int i = 0; i < 16; i++)
            bytes.write(macAddressBytes);

        bytes.flush();

        return bytes.toByteArray();
    }

    private static byte[] parseHexString(String string) {
        byte[] bytes = new byte[string.length() / 2];
        for (int i = 0, j = 0; i < string.length(); i += 2, j++)
            bytes[j] = (byte) Integer.parseInt(string.substring(i, i + 2), 16);
        return bytes;
    }

}
