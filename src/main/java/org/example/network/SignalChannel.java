package org.example.network;


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
        final io.socket.client.Socket socket = IO.socket("http://111.230.151.66:8080");
        socket.on(io.socket.client.Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                //socket.emit("news", "hi");
//                socket.disconnect();
                if (mSdp != null) {
                    sendSdp(mSdp);
                }
            }

        }).on(io.socket.client.Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                System.out.println("disconnect");
            }

        })
                .on(io.socket.client.Socket.EVENT_MESSAGE, new Emitter.Listener() {
            @Override
            public void call(Object... objects) {
                System.out.println("onmessage");
                System.out.println(objects[0]);
            }
        })
                .on("onPeerSdp", new Emitter.Listener() {
            @Override
            public void call(Object... objects) {
                System.out.println("onPeerSdp");
                System.out.println(objects[0]);
                if (mOnPeerSdp != null) {
                    mOnPeerSdp.onPeerSdk((String)objects[0]);
                }
            }
        });
        mSock = socket;
        socket.connect();
    }

    public static interface IOnPeerSdpListener {
        public void onPeerSdk(String sdp);
    }
}
