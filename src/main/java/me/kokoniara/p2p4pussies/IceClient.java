package me.kokoniara.p2p4pussies;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.Base64;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ice4j.Transport;
import org.ice4j.TransportAddress;
import org.ice4j.ice.Agent;
import org.ice4j.ice.Component;
import org.ice4j.ice.IceMediaStream;
import org.ice4j.ice.IceProcessingState;
import org.ice4j.ice.LocalCandidate;
import org.ice4j.ice.NominationStrategy;
import org.ice4j.ice.RemoteCandidate;
import org.ice4j.ice.harvest.StunCandidateHarvester;
import org.ice4j.ice.harvest.TurnCandidateHarvester;
import org.ice4j.security.LongTermCredential;

import static org.ice4j.ice.KeepAliveStrategy.SELECTED_ONLY;

public class IceClient {

    private final int port;

    private final String streamName;

    private Agent agent;

    private String remoteSdp;

    private String[] turnServers;

    private String[] stunServers;

    private String username = "u1";

    private String password = "p1";

    private final IceProcessingListener listener;


    public IceClient(int port, String streamName, String[] stunServers, String[] turnServers) {
        this.port = port;
        this.streamName = streamName;
        this.turnServers = turnServers;
        this.stunServers = stunServers;
        this.listener = new IceProcessingListener();
    }

    public static String encode(String input) {

        return Base64.getEncoder().encodeToString(input.getBytes());

    }

    //Decode:
    public static String decode(String input) {

        byte[] decodedBytes = Base64.getDecoder().decode(input);
        return new String(decodedBytes);

    }


    public String init() throws Throwable {

        agent = createAgent(port, streamName);

        agent.setNominationStrategy(NominationStrategy.NOMINATE_HIGHEST_PRIO);

        agent.addStateChangeListener(listener);

        agent.setControlling(false);

        agent.setTa(10000);

        String localSdp = SdpUtils.createSDPDescription(agent);

        System.out.println("=================== feed the following"
                + " to the remote agent ===================");

        System.out.println(localSdp);

        System.out.println("======================================"
                + "========================================\n");

        return localSdp;
    }

    public DatagramSocket getDatagramSocket() {

        LocalCandidate localCandidate = agent
                .getSelectedLocalCandidate(streamName);

        IceMediaStream stream = agent.getStream(streamName);
        List<Component> components = stream.getComponents();
        for (Component c : components) {
            System.out.println(c);
        }
        System.out.println(localCandidate.toString());
        return ((LocalCandidate) localCandidate).getDatagramSocket();

    }

    public SocketAddress getRemotePeerSocketAddress() {
        RemoteCandidate remoteCandidate = agent
                .getSelectedRemoteCandidate(streamName);
        System.out.println("Remote candinate transport address:"
                + remoteCandidate.getTransportAddress());
        System.out.println("Remote candinate host address:"
                + remoteCandidate.getHostAddress());
        System.out.println("Remote candinate mapped address:"
                + remoteCandidate.getMappedAddress());
        System.out.println("Remote candinate relayed address:"
                + remoteCandidate.getRelayedAddress());
        System.out.println("Remote candinate reflexive address:"
                + remoteCandidate.getReflexiveAddress());
        return remoteCandidate.getTransportAddress();
    }

    /**
     * Reads an SDP description from the standard input.In production
     * environment that we can exchange SDP with peer through signaling
     * server(SIP server)
     */
    public void exchangeSdpWithPeer() {

//        SignalChannel signalChannel = new SignalChannel(localSdp);
//        signalChannel.setPeerSdkListener(sdp -> {
//            try {
//                remoteSdp = sdp;
//                SdpUtils.parseSDP(agent, remoteSdp);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            try {
//                startConnect();
//                startChat(iceClient.this);
//                System.out.println("start chat");
//            } catch (Throwable e) {
//                e.printStackTrace();
//            }
//        });

        // RIP THE DOmAIN
//        signalChannel.start("cmdmac.xyz", 8080);
//        signalChannel.sendSdp(localSdp);


        System.out.println("Paste remote SDP here. Enter an empty line to proceed:");
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                System.in));

        StringBuilder buff = new StringBuilder();
        String line;

        while (true) {
            try {
                if ((line = reader.readLine()) == null) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            line = line.trim();
            if (line.length() == 0) {
                break;
            }
            buff.append(line);
            buff.append("\r\n");
        }

        remoteSdp = buff.toString();

        try {
            SdpUtils.parseSDP(agent, remoteSdp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void startConnect() throws InterruptedException {

        if (StringUtils.isBlank(remoteSdp)) {
            throw new NullPointerException(
                    "Please exchange sdp information with peer before start connect! ");
        }

        agent.startConnectivityEstablishment();

        // agent.runInStunKeepAliveThread();

        synchronized (listener) {
            listener.wait();
        }

    }

    public void ayncKeepRecevice(DatagramSocket socket) {
        new Thread(() -> {
            while (true) {
                try {
                    byte[] buf = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buf,
                            buf.length);
                    socket.receive(packet);
                    System.out.println(packet.getAddress() + ":" + packet.getPort() + " says: " + new String(packet.getData(), 0, packet.getLength()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void asyncKeepSend(DatagramSocket socket, SocketAddress remoteAddress) {
        new Thread(() -> {
            while (true) {
                try {

                    byte[] buf = ("hello " + System.currentTimeMillis()).getBytes();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    packet.setSocketAddress(remoteAddress);
                    socket.send(packet);
//                        System.out.println("keeSend");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void startChat(IceClient client) {

        final DatagramSocket socket = client.getDatagramSocket();
        final SocketAddress remoteAddress = client
                .getRemotePeerSocketAddress();
        System.out.println(socket.toString());
        if (remoteAddress != null) {
            ayncKeepRecevice(socket);
            asyncKeepSend(socket, remoteAddress);
        }

    }

    private Agent createAgent(int rtpPort, String streamName) throws Throwable {
        return createAgent(rtpPort, streamName, false);
    }

    private Agent createAgent(int rtpPort, String streamName,
                              boolean isTrickling) throws Throwable {

        long startTime = System.currentTimeMillis();

        Agent agent = new Agent();

        agent.setTrickling(isTrickling);

        // STUN
        for (String server : stunServers) {
            String[] pair = server.split(":");
            agent.addCandidateHarvester(new StunCandidateHarvester(
                    new TransportAddress(pair[0], Integer.parseInt(pair[1]),
                            Transport.UDP)));
        }

        // TURN
        LongTermCredential longTermCredential = new LongTermCredential(username,
                password);

        for (String server : turnServers) {
            String[] pair = server.split(":");
            agent.addCandidateHarvester(new TurnCandidateHarvester(
                    new TransportAddress(pair[0], Integer.parseInt(pair[1]), Transport.UDP),
                    longTermCredential));
        }
        // STREAMS
        createStream(rtpPort, streamName, agent);

        long endTime = System.currentTimeMillis();
        long total = endTime - startTime;

        System.out.println("Total harvesting time: " + total + "ms.");

        return agent;
    }

    private IceMediaStream createStream(int rtpPort, String streamName,
                                        Agent agent) throws Throwable {
        long startTime = System.currentTimeMillis();
        IceMediaStream stream = agent.createMediaStream(streamName);
        // rtp
        Component component = agent.createComponent(stream, rtpPort + 50,
                rtpPort, rtpPort + 100, SELECTED_ONLY);

        long endTime = System.currentTimeMillis();
        System.out.println("Component Name:" + component.getName());
        System.out.println("RTP Component created in " + (endTime - startTime) + " ms");

        return stream;
    }

    /**
     * Receive notify event when ice processing state has changed.
     */
    public static final class IceProcessingListener implements
            PropertyChangeListener {

        private long startTime = System.currentTimeMillis();

        public void propertyChange(PropertyChangeEvent event) {

            Object state = event.getNewValue();

            System.out.println("Agent entered the " + state + " state.");
            if (state == IceProcessingState.COMPLETED) {
                long processingEndTime = System.currentTimeMillis();
                System.out.println("Total ICE processing time: "
                        + (processingEndTime - startTime) + "ms");
                Agent agent = (Agent) event.getSource();
                List<IceMediaStream> streams = agent.getStreams();

                for (IceMediaStream stream : streams) {
                    System.out.println("Stream name: " + stream.getName());
                    List<Component> components = stream.getComponents();
                    for (Component c : components) {
                        System.out.println("------------------------------------------");
                        System.out.println("Component of stream:" + c.getName()
                                + ",selected of pair:" + c.getSelectedPair());
                        System.out.println("------------------------------------------");
                    }
                }

                System.out.println("Printing the completed check lists:");
                for (IceMediaStream stream : streams) {

                    System.out.println("Check list for  stream: " + stream.getName());

                    System.out.println("nominated check list:" + stream.getCheckList());
                }
                synchronized (this) {
                    this.notifyAll();
                }
            } else if (state == IceProcessingState.TERMINATED) {
                System.out.println("ice processing TERMINATED");
            } else if (state == IceProcessingState.FAILED) {
                System.out.println("ice processing FAILED");
                ((Agent) event.getSource()).free();
            }
        }
    }

    public String[] getTurnServers() {
        return turnServers;
    }

    public void setTurnServers(String[] turnServers) {
        this.turnServers = turnServers;
    }

    public String[] getStunServers() {
        return stunServers;
    }

    public void setStunServers(String[] stunServers) {
        this.stunServers = stunServers;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}