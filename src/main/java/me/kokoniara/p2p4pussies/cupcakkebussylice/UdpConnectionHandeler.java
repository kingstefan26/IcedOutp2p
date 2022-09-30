package me.kokoniara.p2p4pussies.cupcakkebussylice;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class UdpConnectionHandeler {


    private final LinkedBlockingQueue<String> sendqueue;
    private SocketAddress remoteAddress;
    private DatagramSocket socket;
    private Consumer<Object> receiveCallBack;
    private Consumer<Object> connectCallBack;

    public UdpConnectionHandeler(SocketAddress remoteAddress, DatagramSocket socket, LinkedBlockingQueue<String> sendqueue, Consumer<Object> receiveCallBack, Consumer<Object> connectCallBack) {
        this.remoteAddress = remoteAddress;
        this.socket = socket;
        this.receiveCallBack = receiveCallBack;
        this.connectCallBack = connectCallBack;
        this.sendqueue = sendqueue;
    }

    public void startConnection() {

        if (remoteAddress != null) {

            AtomicBoolean connectionAlive = new AtomicBoolean();
            var ref = new Object() {
                AtomicLong keepAliveTimer = new AtomicLong();
            };


            connectionAlive.set(true);
            ref.keepAliveTimer.set((long) (System.currentTimeMillis() + 500F));

            new Thread(() -> {
                while (connectionAlive.get()) {
                    if (!connectionAlive.get()) {
//                        System.out.println("Time-out of 5 seconds reached");
                    }
                    String a = String.valueOf(System.currentTimeMillis());
                    String b = String.valueOf(ref.keepAliveTimer);
//                    System.out.println(a + " > " + b);
                    if (System.currentTimeMillis() > ref.keepAliveTimer.get() && ref.keepAliveTimer.get() != 0) {
//                        connectionAlive.set(false);
//                        System.out.println("Time-out of 5 seconds reached");
                    }
                    try {




                        String m = sendqueue.poll();

                        if (m != null) {
//                            System.out.println("SEND QUEUE POOL IS NOT NULL: " + m);
                            byte[] newbuf = (m).getBytes();
                            DatagramPacket newpacket = new DatagramPacket(newbuf, newbuf.length);
                            newpacket.setSocketAddress(remoteAddress);
                            socket.send(newpacket);
                        } else {
                            String messagetosend = "keepAlive";

                            byte[] buf = (messagetosend).getBytes();
                            DatagramPacket packet = new DatagramPacket(buf, buf.length);
                            packet.setSocketAddress(remoteAddress);
                            socket.send(packet);
                        }


//                    System.out.println("sent a keepAlive");
//                    System.out.println("Keep Alive: " + connectionAlive);
                        Thread.sleep(50);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();


            new Thread(() -> {

                while (connectionAlive.get()) {
                    if (!connectionAlive.get()) {
//                        System.out.println("Time-out of 5 seconds reached");
                    }
                    String a = String.valueOf(System.currentTimeMillis());
                    String b = String.valueOf(ref.keepAliveTimer.get());
//                    System.out.println(a + " > " + b);
                    if (System.currentTimeMillis() > ref.keepAliveTimer.get() && ref.keepAliveTimer.get() != 0) {
//                        connectionAlive.set(false);
//                        System.out.println("Time-out of 5 seconds reached");
                    }
                    try {

                        byte[] buf = new byte[65535];
                        DatagramPacket packet = new DatagramPacket(buf,
                                buf.length);
                        socket.receive(packet);
                        String messagefromotherside = new String(packet.getData(), 0, packet.getLength());
//                        System.out.println(packet.getAddress() + ":" + packet.getPort() + " says: " + messagefromotherside);
                        if (packet.getLength() > 1024) {
                            continue;
                        }
                        if (messagefromotherside.equals("keepAlive")) {
                            ref.keepAliveTimer.set((long) (System.currentTimeMillis() + 5000f));
//                            System.out.println("recived keepAlive, timer reset to " + (System.currentTimeMillis() + 5000f));
                        } else {

//                            System.out.println("THIS IS FALSE EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
                            if (receiveCallBack != null) {
//                                System.out.println("CALLLED RECIVED CALLBACK");
                                receiveCallBack.accept(messagefromotherside);

                            }

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }).start();


            if (connectCallBack != null) {
                System.out.println("Accepted connected callback");
                connectCallBack.accept(remoteAddress);
            }

        }

    }
}
