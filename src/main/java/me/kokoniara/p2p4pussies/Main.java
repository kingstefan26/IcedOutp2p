package me.kokoniara.p2p4pussies;

import me.kokoniara.p2p4pussies.cupcakkebussylice.Peer;
import me.kokoniara.p2p4pussies.cupcakkebussylice.PeerBuilder;
import me.kokoniara.p2p4pussies.websocket.myChannel;

public class Main {
    public static void main(String[] args) {

        PeerBuilder builder = new PeerBuilder();

        Peer peer =  builder.getPeer();

        peer.start();



        peer.on(Peer.event.receive, message -> {
            System.out.println(message);
        });

        String minesdp = peer.getlocalsdp();
        myChannel m = new myChannel("http://192.168.1.38:3000", minesdp, (peer::setRemotespd));
        m.socketConnect();
        m.sendSdp();




    }

}
