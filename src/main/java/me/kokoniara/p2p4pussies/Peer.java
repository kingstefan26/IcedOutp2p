package me.kokoniara.p2p4pussies;


public class Peer {

    public void start() {
        try {
//            System.setProperty(StackProperties.DISABLE_IPv6, "true");
            IceClient client = new IceClient(8888, "text", new String[]{"192.168.1.38:3478"}, new String[]{"192.168.1.38:3478"});
            client.init();
            client.exchangeSdpWithPeer();

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        new Peer().start();
    }

}
