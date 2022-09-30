package me.kokoniara.p2p4pussies;

import me.kokoniara.p2p4pussies.cupcakkebussylice.SdpUtils;
import org.ice4j.Transport;
import org.ice4j.ice.Agent;
import org.ice4j.ice.IceMediaStream;

import java.io.IOException;

public class test {

    static Agent createAgent(int port) throws IOException {
        Agent agent = new Agent();


        IceMediaStream data = agent.createMediaStream("data");
        agent.createComponent(data, port, port, port + 100);

        return agent;
    }

    public static void main(String[] args) throws Throwable {

        Agent a1 = createAgent(8081);
        Agent a2 = createAgent(8082);
        String sdp1 = SdpUtils.createSDPDescription(a1);
        String sdp2 = SdpUtils.createSDPDescription(a2);
        SdpUtils.parseSDP(a1, sdp2);
        SdpUtils.parseSDP(a2, sdp1);

        new Thread(() -> {
            a1.startConnectivityEstablishment();
        }).start();
        new Thread(() -> {
            a2.startConnectivityEstablishment();
        }).start();

        Thread.sleep(10_100);
    }

}
