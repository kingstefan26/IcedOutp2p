package me.kokoniara.p2p4pussies;

import java.net.URISyntaxException;


public class Peer
{

    public void start() {
        try
        {
//            System.setProperty(StackProperties.DISABLE_IPv6, "true");
            iceClient client = new iceClient(8888, "text");
            client.init();
            client.exchangeSdpWithPeer();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }

    public void test() {

    }

    public static void main(String[] args) throws URISyntaxException {
        new Peer().start();
    }

}
