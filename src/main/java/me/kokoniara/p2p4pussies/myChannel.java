package me.kokoniara.p2p4pussies;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.function.Consumer;

public class myChannel {

    private final String host;
    private Consumer<String> listener;
    private Socket mSocket;

    public myChannel(String host) {
        this.host = host;
    }

    public myChannel(String host, Consumer<String> listener) {
        this.host = host;
        this.listener = listener;
    }

    /**
     * chat socket connection methods
     */
    public void socketConnection() {
        try {
            mSocket = IO.socket(host);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.on("message", onSocketConnectionListener);
            mSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            while (!mSocket.connected()) {
                //do nothing
            }
            sendConnectData();
        }).start();
    }

    /**
     * Send Data to connect to chat server
     */
    public void sendConnectData() {
//        JSONObject msgToSend = new JSONObject();
//        try {
//            msgToSend.put("Type", 1);
//            msgToSend.put("userid", "1");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        mSocket.emit("message", "RIRI FLOPED");
    }

    /**
     * Listener for socket connection error. listener registered at the time of socket connection
     */
    private final Emitter.Listener onConnectError = args -> {

        if (mSocket != null) {
            if (!mSocket.connected()) {
                socketConnection();
            }
        }


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
}
