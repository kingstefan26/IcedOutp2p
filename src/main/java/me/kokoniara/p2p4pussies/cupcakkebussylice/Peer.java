package me.kokoniara.p2p4pussies.cupcakkebussylice;


import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class Peer {
    public enum event {
        connect,
        receive,
        disconnected
    }


    public enum state {
        IDLE,
        CONNECTING,
        CONNECTED,
        ERROR
    }

    String[] turnServers = {};
    String[] stunServers = {"stun.l.google.com:19302"};

    int deafultConnectionPort = 8888;


    IceClient client;

    public void init() {
        System.setProperty("org.ice4j.ipv6.DISABLED", "true");

        client = new IceClient(deafultConnectionPort, "data", stunServers, turnServers);
        try {
            client.init();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
//        client.exchangeSdpWithPeer();


    }

    public void connect(){
        long start = System.currentTimeMillis();
        long stop = System.currentTimeMillis();

        client.exchangeSdpWithPeer();
        
        UdpConnectionHandeler handeler = new UdpConnectionHandeler(client.getRemotePeerSocketAddress(), client.getDatagramSocket(), emmitqueue, receiveCallBack, connectCallBack);
        
        handeler.startConnection();
    }



    public void setRemotespd(String remotespd){
        client.setRemoteSdp(remotespd);
    }

    public String getlocalsdp(){
        return client.getLocalSdp();
    }

    LinkedBlockingQueue<String> emmitqueue = new LinkedBlockingQueue<>();

    public void emmit(String content){
        emmitqueue.add(content);
    }


    public void on(event event, Consumer<Object> callback){
        switch (event){
            case connect:
                this.connectCallBack = callback;
            break;
            case receive:
                this.receiveCallBack = callback;
            break;
            case disconnected:
                this.disconnectedCallBack = callback;
            break;
        }
    }

    Consumer<Object> receiveCallBack;

    Consumer<Object> disconnectedCallBack;

    Consumer<Object> connectCallBack;


}
