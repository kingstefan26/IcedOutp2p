package me.kokoniara.p2p4pussies.cupcakkebussylice;


import java.util.function.Consumer;

public class Peer {
    public enum event {
        connect,
        receive,
        disconnected
    }

    String[] turnServers = {};
    String[] stunServers = {"stun.l.google.com:19302"};

    int deafultConnectionPort = 8888;


    IceClient client;

    public void start() {
        System.setProperty("org.ice4j.ipv6.DISABLED", "true");

        client = new IceClient(deafultConnectionPort, "text", stunServers, turnServers);
        try {
            client.init();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
//        client.exchangeSdpWithPeer();


    }



    public void setRemotespd(String sdp){
        client.exchangeSdpWithPeer(sdp);
    }

    public String getlocalsdp(){
        return client.getLocalSdp();
    }


    public void emmit(String content){
        client.emmit(content);
    }


    public void on(event event, Consumer<Object> callback){
        switch (event){
            case connect -> {
                client.setConnectCallBack(callback);
            }
            case receive -> {
                client.setReceiveCallBack(callback);
            }
            case disconnected -> {
                client.setDisconnectedCallBack(callback);
            }
        }
    }




}
