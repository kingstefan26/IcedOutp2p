package me.kokoniara.p2p4pussies;

public class Main {
    public static void main(String[] args) {
//        new Peer().start();

//        SignalChannel channel = new SignalChannel("test");
//
//
//        try {
//            channel.start("http://192.168.1.38:3000");
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }
//
//        channel.sendSdp("ping");
//
//
//        channel.close();


        myChannel m = new myChannel("http://192.168.1.38:3000");
        m.socketConnection();


    }

}
