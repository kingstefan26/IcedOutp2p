package me.kokoniara.p2p4pussies.cupcakkebussylice;


public class Peer {

    String[] turnServers = {};
    String[] stunServers = {"stun.l.google.com:19302"};

    int deafultConnectionPort = 8888;

    String websockethost = "http://192.168.1.38:3000";

    public void start() {
        System.setProperty("org.ice4j.ipv6.DISABLED", "true");
        IceClient client = new IceClient(deafultConnectionPort, "text", websockethost, stunServers, turnServers);
        try {
            client.init();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        client.exchangeSdpWithPeer();


    }

}
