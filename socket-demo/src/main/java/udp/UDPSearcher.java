package udp;

import lombok.SneakyThrows;
import udp.chat.MessageCreator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * @author : zuoyu
 * @project : socket-demo-1
 * @description : 消息接收
 * @date : 2020-01-03 17:51
 **/
public class UDPSearcher {

    private static final int LISTEN_PORT = 30000;

    public static void main(String[] args) throws IOException {

        System.out.println("UDPSearch Started");
        Listener listener = listen();
        sendBroadCast();

        System.in.read();

        List<Device> devices = listener.getDevicesAndClose();
        devices.forEach(device -> {
            System.out.println("Device:\t" + device.toString());
        });
        System.out.println("UDPSearch Finished");
    }

    @SneakyThrows
    private static Listener listen() {
        System.out.println("UDPSearch listen started");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT, countDownLatch);
        listener.start();
        countDownLatch.await();
        return listener;
    }

    @SneakyThrows
    private static void sendBroadCast() {
        System.out.println("UDPSearch sendBroadCast started");
        DatagramSocket datagramSocket = new DatagramSocket();

        // 发送消息
        String requestContent = MessageCreator.buildWithPort(LISTEN_PORT);
        byte[] contentBytes = requestContent.getBytes();
        DatagramPacket requestPacket = new DatagramPacket(contentBytes, contentBytes.length);
        // 20000端口，广播地址
        requestPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        requestPacket.setPort(20000);
        datagramSocket.send(requestPacket);
        datagramSocket.close();

        System.out.println("UDPSearch sendBroadCast Finished");
    }

    private static class Listener extends Thread {
        private final int listenerPort;
        private final CountDownLatch countDownLatch;
        private final List<Device> devices = new ArrayList<>();
        private boolean done = false;
        private DatagramSocket datagramSocket = null;

        private Listener(int listenerPort, CountDownLatch countDownLatch) {
            this.listenerPort = listenerPort;
            this.countDownLatch = countDownLatch;
        }

        @SneakyThrows
        @Override
        public void run() {
            super.run();
            // 通知已启动
            this.countDownLatch.countDown();
            System.out.println("UDPSearch Listener is Started");
            try {
                this.datagramSocket = new DatagramSocket(this.listenerPort);
                byte[] data = new byte[521];
                DatagramPacket datagramPacket = new DatagramPacket(data, data.length);
                // 接收消息
                this.datagramSocket.receive(datagramPacket);
                int length = datagramPacket.getLength();
                int offset = datagramPacket.getOffset();
                byte[] packetData = datagramPacket.getData();
                String content = new String(packetData, offset, length, Charset.defaultCharset());
                String ip = datagramPacket.getAddress().getHostAddress();
                int port = datagramPacket.getPort();
                String message = "收到消息（" + ip + ":" + port + "）\t内容：" + content;
                System.out.println(message);
                String sn = MessageCreator.parseSn(content);
                if (sn != null) {
                    Device device = new Device(port, ip, sn);
                    this.devices.add(device);
                }
            } catch (SocketException e) {
                e.printStackTrace();
            } finally {
                this.close();
            }
            System.out.println("UDPSearch Listener is Finished");
        }

        private void close() {
            if (!Objects.isNull(this.datagramSocket)) {
                this.datagramSocket.close();
                this.datagramSocket = null;
            }
        }

        List<Device> getDevicesAndClose() {
            this.done = true;
            this.close();
            return this.devices;
        }
    }

    private static class Device {
        final int port;
        final String ip;
        final String sn;

        private Device(int port, String ip, String sn) {
            this.port = port;
            this.ip = ip;
            this.sn = sn;
        }

        @Override
        public String toString() {
            return "Device{" + "port=" + port + ", ip='" + ip + '\'' + ", sn='" + sn + '\'' + '}';
        }
    }
}
