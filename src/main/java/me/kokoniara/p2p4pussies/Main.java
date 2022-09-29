package me.kokoniara.p2p4pussies;

import me.kokoniara.p2p4pussies.cupcakkebussylice.Peer;
import me.kokoniara.p2p4pussies.cupcakkebussylice.PeerBuilder;

public class Main {
    public static void main(String[] args) {

        PeerBuilder builder = new PeerBuilder();

        Peer peer =  builder.getPeer();

        peer.start();

    }

}
