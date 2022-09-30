package me.kokoniara.p2p4pussies.websocket;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.function.Consumer;

public class myChannel {

    private final String host;
    private Consumer<String> listener;
    private Consumer<String> sdpListner;

    private Socket mSocket;


    /**
     * @param host     The host we are connecting to
     * @param listener only for tests, check if anything is comming tru the websocket
     */
    public myChannel(String host, Consumer<String> listener) {
        this.host = host;
        this.sdpListner = listener;
    }

    /**
     * chat socket connection methods
     */
    public void socketConnect() {
        try {
            mSocket = IO.socket(host);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.on("message", onSocketConnectionListener);
            mSocket.on("onPeerSdp", onPeerSdp);
            mSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            while (!mSocket.connected()) {
                //do nothing
            }
//            sendConnectData();
        }).start();
    }

    /**
     * Send Data to connect to chat server
     */
    public void sendConnectData(String subject, String contents) {
//        JSONObject msgToSend = new JSONObject();
//        try {
//            msgToSend.put("Type", 1);
//            msgToSend.put("userid", "1");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        mSocket.emit(subject, contents);
    }

    public void sendConnectData(String contents) {
        mSocket.emit("message", contents);
    }

    /**
     * Listener for socket connection error. listener registered at the time of socket connection
     */
    private final Emitter.Listener onConnectError = args -> {

        if (mSocket != null && !mSocket.connected()) {
            socketConnect();
        }


    };


    /**
     * Lister for Peers sdp :)
     */
    private final Emitter.Listener onPeerSdp = args -> {
        System.out.println("Recived peer sdp");
        sdpListner.accept((String) args[0]);
    };

    /**
     * Listener to handle messages received from chat server of any type... Listener registered at the time of socket connected
     */
    private Emitter.Listener onSocketConnectionListener = args -> {
        System.out.println(args[0]);
        System.out.println("FUCKK ALL YALL ARIANAS GRANDE STANS");
        if (listener != null && args[0] instanceof String) {
            listener.accept((String) args[0]);
        }
    };

    public void sendSdp(String localsdp) {
        JSONObject msgToSend = new JSONObject();
        try {
            msgToSend.put("sdp", localsdp);

        } catch (Exception e) {
            e.printStackTrace();
        }

        mSocket.emit("join", msgToSend);
        System.out.println("localsdp sent to websocket server");
    }
}
