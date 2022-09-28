package me.kokoniara.p2p4pussies;


import java.net.URISyntaxException;

public class Peer {

    String[] turnServers = {"192.168.1.38:3478"};
    String[] stunServers = {"192.168.1.38:3478"};


    public void start() {
//            System.setProperty(StackProperties.DISABLE_IPv6, "true");
        IceClient client = new IceClient(8888, "text", stunServers, turnServers);
        try {
            client.init();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        client.exchangeSdpWithPeer();


    }

}
