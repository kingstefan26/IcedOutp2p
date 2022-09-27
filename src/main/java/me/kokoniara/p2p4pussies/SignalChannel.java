package me.kokoniara.p2p4pussies;


import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.net.URISyntaxException;

public class SignalChannel {
    String mSdp;
    Socket mSock;
    IOnPeerSdpListener mOnPeerSdp;

    public SignalChannel(String sdp) {
        mSdp = sdp;
    }

    public void setPeerSdkListener(IOnPeerSdpListener onPeerSdpListener) {
        mOnPeerSdp = onPeerSdpListener;
    }

    public void sendSdp(String msg) {
//        String format = "{\"sdp\":\"%s\"}";
        mSock.emit("join", msg);

    }

    private void onReceive(String data) {
        System.out.println(data);

    }

    public void start(String host, int port) throws URISyntaxException {

        // fr i though homeboy leaked his ip but this is tencent cloud ip
//        final Socket socket = IO.socket("http://111.230.151.66:8080");
        final Socket socket = IO.socket("http://" + host + ":" + port);
        socket.on(Socket.EVENT_CONNECT, args -> {
            //socket.emit("news", "hi");
//                socket.disconnect();
            if (mSdp != null) {
                sendSdp(mSdp);
            }
        });
        socket.on(Socket.EVENT_DISCONNECT, args -> System.out.println("disconnect"));
//        socket.on(Socket.EVENT_MESSAGE, objects -> {
//            System.out.println("onmessage");
//            System.out.println(objects[0]);
//        });
        socket.on("onPeerSdp", objects -> {
            System.out.println("onPeerSdp");
            System.out.println(objects[0]);
            if (mOnPeerSdp != null) {
                mOnPeerSdp.onPeerSdk((String) objects[0]);
            }
        });
        mSock = socket;
        socket.connect();
    }

    public static interface IOnPeerSdpListener {
        public void onPeerSdk(String sdp);
    }
}
