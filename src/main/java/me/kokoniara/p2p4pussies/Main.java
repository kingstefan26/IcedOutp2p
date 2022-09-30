package me.kokoniara.p2p4pussies;

import me.kokoniara.p2p4pussies.cupcakkebussylice.Peer;
import me.kokoniara.p2p4pussies.cupcakkebussylice.PeerBuilder;
import me.kokoniara.p2p4pussies.websocket.myChannel;

public class Main {
    public static void main(String[] args) {

        PeerBuilder builder = new PeerBuilder();

        builder.setDefaultConnectionPort(8888);

        builder.setStunServers("192.168.1.38:19302");

        Peer peer = builder.getPeer();

        peer.init();



        peer.on(Peer.event.receive, message -> {
            System.out.println("Main: "+ message);
        });


        peer.on(Peer.event.connect, event -> {
           peer.emmit("HAHA YOU FATTIES");

           new Thread(() -> {
               while (true){
                   peer.emmit("FATTIE");
                   System.out.println("Send a fattie");

                   try {
                       Thread.sleep(500);
                   } catch (InterruptedException e) {
                       throw new RuntimeException(e);
                   }
               }
           }).start();

        });

        myChannel m = new myChannel("http://192.168.1.38:3000", (remotespd -> {
            peer.setRemotespd(remotespd);

            peer.connect();
        }));
        m.socketConnect();
        m.sendSdp(peer.getlocalsdp());






    }

}
