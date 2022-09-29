package me.kokoniara.p2p4pussies.cupcakkebussylice;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
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
import org.ice4j.pseudotcp.PseudoTCPBase;
import org.ice4j.pseudotcp.PseudoTcpSocket;
import org.ice4j.pseudotcp.PseudoTcpSocketFactory;
import org.ice4j.security.LongTermCredential;

import static org.ice4j.ice.KeepAliveStrategy.SELECTED_ONLY;

public class IceClient {

    private final int port;

    private final String streamName;

    private Agent agent;

    private String remoteSdp;

    private String websocketHost;
    private String[] turnServers;

    private String[] stunServers;

    private String username = "u1";

    private String password = "p1";

    private final IceProcessingListener listener;
    private boolean connectionAlive;
    private float keepAliveTimer;


    public IceClient(int port, String streamName, String websocketHost, String[] stunServers, String[] turnServers) {
        this.port = port;
        this.streamName = streamName;
        this.websocketHost = websocketHost;
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


    public String createLocalSdp() throws Throwable {
        agent = createAgent(port, streamName);

        agent.setNominationStrategy(NominationStrategy.NOMINATE_HIGHEST_PRIO);

        agent.addStateChangeListener(listener);

        agent.setControlling(false);

        agent.setTa(10000);

        return SdpUtils.createSDPDescription(agent);
    }

    String localSdp;

    public void init() throws Throwable {

        localSdp = createLocalSdp();

        System.out.println("=================== feed the following"
                + " to the remote agent ===================");

        System.out.println(localSdp);

        System.out.println("======================================"
                + "========================================\n");

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
        return (localCandidate).getDatagramSocket();

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

        myChannel m = new myChannel(websocketHost, localSdp, (peerSdp -> {

            this.remoteSdp = peerSdp;
            try {
                SdpUtils.parseSDP(agent, remoteSdp);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                startConnect();
                startChat(IceClient.this);
                System.out.println("start chat");
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }));
        m.socketConnection();
        m.sendSdp();


//        SignalChannel signalChannel = new SignalChannel(localSdp);
//        signalChannel.setPeerSdkListener(sdp -> {
//
//        });

//         RIP THE DOmAIN
//        signalChannel.start("cmdmac.xyz", 8080);
//        signalChannel.sendSdp(localSdp);


//        System.out.println("Paste remote SDP here. Enter an empty line to proceed:");
//        BufferedReader reader = new BufferedReader(new InputStreamReader(
//                System.in));
//
//        StringBuilder buff = new StringBuilder();
//        String line;
//
//        while (true) {
//            try {
//                if ((line = reader.readLine()) == null) break;
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            line = line.trim();
//            if (line.length() == 0) {
//                break;
//            }
//            buff.append(line);
//            buff.append("\r\n");
//        }
//
//        remoteSdp = buff.toString();
//
//        try {
//            SdpUtils.parseSDP(agent, remoteSdp);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }


    public void startConnect() throws InterruptedException {

        if (StringUtils.isBlank(remoteSdp)) {
            throw new NullPointerException(
                    "Please exchange sdp information with peer before start connect! ");
        }

        agent.startConnectivityEstablishment();

//        agent.runInStunKeepAliveThread();

        synchronized (listener) {
            listener.wait();
        }

    }


    public void startChat(IceClient client) {

        PseudoTcpSocketFactory fc = new PseudoTcpSocketFactory();
        PseudoTcpSocket socket = null;
        try {
            socket = fc.createSocket(client.getDatagramSocket());
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        final SocketAddress remoteAddress = client
                .getRemotePeerSocketAddress();
        if(socket == null) return;

        System.out.println(socket);
        if (remoteAddress != null) {


            try {
                socket.accept(remoteAddress, 10000);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            PseudoTcpSocket finalSocket = socket;
            new Thread(() -> {
                BufferedReader in = null;
                try {
                    in = new BufferedReader(new InputStreamReader(finalSocket.getInputStream()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                while (finalSocket.isConnected()) {
                    String data = null;
                    try {
                        data = in.readLine();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    if (data != null) {
                        System.out.println("Data: " + data);
                    }
                }
            }).start();




            new Thread(() -> {
                BufferedWriter out = null;
                try {
                    out = new BufferedWriter(new OutputStreamWriter(finalSocket.getOutputStream()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                float timer = System.currentTimeMillis() + 500;

                while (finalSocket.isConnected()) {
                    if(timer < System.currentTimeMillis()){
                        try {
                            out.write("KILL ME");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        timer = System.currentTimeMillis() + 500;
                    }
                }
            }).start();




//            connectionAlive = true;
//            keepAliveTimer = System.currentTimeMillis() + 5000;
//            ayncKeepRecevice(socket);
//            asyncKeepSend(socket, remoteAddress);
        }

    }


    public void ayncKeepRecevice(DatagramSocket socket) {
        new Thread(() -> {
            while (connectionAlive) {
                if (System.currentTimeMillis() > keepAliveTimer) {
                    this.connectionAlive = false;
                    System.out.println("Time-out of 5 seconds reached");
                }
                try {
                    byte[] buf = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buf,
                            buf.length);
                    socket.receive(packet);
                    String messagefromotherside = new String(packet.getData(), 0, packet.getLength());

                    if (messagefromotherside.equals("keepAlive")) {
                        keepAliveTimer = System.currentTimeMillis() + 5000f;
                        System.out.println("recived keepAlive, timer reset to " + keepAliveTimer);
                    } else {
                        System.out.println(packet.getAddress() + ":" + packet.getPort() + " says: " + messagefromotherside);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void asyncKeepSend(DatagramSocket socket, SocketAddress remoteAddress) {
        new Thread(() -> {
//            while (connectionAlive) {
//                try {
//
//                    String messagetosend = "keepAlive";
//
//                    byte[] buf = (messagetosend).getBytes();
//                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
//                    packet.setSocketAddress(remoteAddress);
//                    socket.send(packet);
//                    System.out.println("sent a keepAlive");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
        }).start();

        new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String line;
                // 从键盘读取
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.length() == 0) {
                        break;
                    }
                    byte[] buf = (line).getBytes();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    packet.setSocketAddress(remoteAddress);
                    socket.send(packet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }

    private Agent createAgent(int rtpPort, String streamName) throws IOException {
        return createAgent(rtpPort, streamName, false);
    }

    private Agent createAgent(int rtpPort, String streamName,
                              boolean isTrickling) throws IOException {

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
                                        Agent agent) throws IOException {
        long startTime = System.currentTimeMillis();
        IceMediaStream stream = agent.createMediaStream(streamName);
        // rtp
        Component component = null;
        component = agent.createComponent(stream, rtpPort + 50,
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