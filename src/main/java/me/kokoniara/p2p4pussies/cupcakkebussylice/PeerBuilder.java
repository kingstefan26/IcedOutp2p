package me.kokoniara.p2p4pussies.cupcakkebussylice;

public class PeerBuilder {

    private Peer peer;

    public PeerBuilder() {
        this.reset();
    }

    public void reset(){
        peer = new Peer();
    }


    public Peer getPeer(){
        Peer product = peer;
        this.reset();
        return product;
    }


    public void setTurnServers(String... turnServers){
        peer.turnServers = turnServers;
    }

    public void setStunServers(String... stunServers){
        peer.stunServers = stunServers;
    }


    public void setDefaultConnectionPort(int port){
        peer.deafultConnectionPort = port;
    }

    public void setWebSocketHost(String host){
        peer.websockethost = host;
    }



}
