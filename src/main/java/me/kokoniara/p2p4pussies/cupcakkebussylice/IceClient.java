package me.kokoniara.p2p4pussies.cupcakkebussylice;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    Logger logger = LogManager.getLogger("IceClient");

    private final int port;

    private final String streamName;

    private Agent agent;

    public void setRemoteSdp(String remoteSdp) {
        this.remoteSdp = remoteSdp;
    }

    private String remoteSdp;

    private final String[] turnServers;

    private final String[] stunServers;

    private final IceProcessingListener listener;

    private static final boolean verbose = false;


    public IceClient(int port, String streamName, String[] stunServers, String[] turnServers) {
        this.port = port;
        this.streamName = streamName;
        this.turnServers = turnServers;
        this.stunServers = stunServers;
        this.listener = new IceProcessingListener();
    }


    public String createLocalSdp() throws Throwable {
        agent = createAgent(port, streamName);

        agent.setNominationStrategy(NominationStrategy.NOMINATE_FIRST_VALID);

        agent.addStateChangeListener(listener);

        agent.setControlling(false);


        return SdpUtils.createSDPDescription(agent);
    }

    String localSdp;

    public String getLocalSdp() {
        return localSdp;
    }

    public void init() throws Throwable {
        localSdp = createLocalSdp();
    }

    public DatagramSocket getDatagramSocket() {

        LocalCandidate localCandidate = agent
                .getSelectedLocalCandidate(streamName);

        if(verbose){
            IceMediaStream stream = agent.getStream(streamName);
            List<Component> components = stream.getComponents();
            for (Component c : components) {
                logger.info(c);
            }
        }

        return (localCandidate).getDatagramSocket();

    }

    public SocketAddress getRemotePeerSocketAddress() {
        RemoteCandidate remoteCandidate = agent
                .getSelectedRemoteCandidate(streamName);
        if(verbose){
            logger.info("Remote candinate transport address: {}", remoteCandidate.getTransportAddress());
            logger.info("Remote candinate host address: {}", remoteCandidate.getHostAddress());
            logger.info("Remote candinate mapped address: {}", remoteCandidate.getMappedAddress());
            logger.info("Remote candinate relayed address: {}", remoteCandidate.getRelayedAddress());
            logger.info("Remote candinate reflexive address:{}", remoteCandidate.getReflexiveAddress());
        }

        return remoteCandidate.getTransportAddress();
    }


    public void exchangeSdpWithPeer() {
        try {
            SdpUtils.parseSDP(agent, remoteSdp);
            startConnect();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void close() {
        agent.free();

    }


    public void startConnect() throws InterruptedException {

        if (StringUtils.isBlank(remoteSdp)) {
            throw new NullPointerException(
                    "Please exchange sdp information with peer before start connect! ");
        }

        agent.startConnectivityEstablishment();

        synchronized (listener) {
            listener.wait();
        }
    }



    private Agent createAgent(int rtpPort, String streamName) throws IOException {

        long startTime = System.currentTimeMillis();

        this.agent = new Agent();

        agent.setTrickling(false);

        // STUN
        for (String server : stunServers) {
            String[] pair = server.split(":");
            agent.addCandidateHarvester(new StunCandidateHarvester(
                    new TransportAddress(pair[0], Integer.parseInt(pair[1]),
                            Transport.UDP)));
        }

        // TURN
        if (turnServers != null && turnServers.length > 0) {
            String username = "u1";
            String password = "p1";
            LongTermCredential longTermCredential = new LongTermCredential(username,
                    password);

            for (String server : turnServers) {
                String[] pair = server.split(":");
                agent.addCandidateHarvester(new TurnCandidateHarvester(
                        new TransportAddress(pair[0], Integer.parseInt(pair[1]), Transport.UDP),
                        longTermCredential));
            }
        }

        // STREAMS
        createStream(rtpPort, streamName, agent);


        long endTime = System.currentTimeMillis();
        long total = endTime - startTime;

        if(verbose)
            logger.info("Total harvesting time: {}ms.", total);

        return agent;
    }

    private void createStream(int rtpPort, String streamName,
                              Agent agent) throws IOException {
        long startTime = System.currentTimeMillis();
        IceMediaStream stream = agent.createMediaStream(streamName);
        // rtp
        Component component = agent.createComponent(stream, rtpPort + 50,
                rtpPort, rtpPort + 100, SELECTED_ONLY);
        long endTime = System.currentTimeMillis();

        if(verbose){
            logger.info("Component Name:{}", component.getName());
            logger.info("RTP Component created in {}ms", (endTime - startTime));
        }

    }

    /**
     * Receive notify event when ice processing state has changed.
     */
    public static final class IceProcessingListener implements
            PropertyChangeListener {

        Logger logger = LogManager.getLogger("IceProcessingListner");

        private final long startTime = System.currentTimeMillis();

        public void propertyChange(PropertyChangeEvent event) {

            Object state = event.getNewValue();


            logger.info("Agent entered the {} state.", state);


            if (state == IceProcessingState.COMPLETED) {

                if(verbose){
                    long processingEndTime = System.currentTimeMillis();
                    logger.info("Total ICE processing time: {}ms", (processingEndTime - startTime));
                    Agent agent = (Agent) event.getSource();
                    List<IceMediaStream> streams = agent.getStreams();

                    for (IceMediaStream stream : streams) {
                        logger.info("Stream name: {}", stream.getName());
                        List<Component> components = stream.getComponents();
                        for (Component c : components) {
                            logger.info("------------------------------------------");
                            logger.info("Component of stream: {},selected of pair:{}", c.getName(), c.getSelectedPair());
                            logger.info("------------------------------------------");
                        }
                    }

                    logger.info("Printing the completed check lists:");
                    for (IceMediaStream stream : streams) {

                        logger.info("Check list for  stream: {}", stream.getName());

                        logger.info("nominated check list: {}", stream.getCheckList());
                    }
                }

                synchronized (this) {
                    this.notifyAll();
                }
            } else if (state == IceProcessingState.TERMINATED) {
                logger.info("ice processing TERMINATED");
            } else if (state == IceProcessingState.FAILED) {
                logger.info("ice processing FAILED");
                ((Agent) event.getSource()).free();
            }
        }
    }
}