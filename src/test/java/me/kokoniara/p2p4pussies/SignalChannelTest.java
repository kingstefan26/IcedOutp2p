package me.kokoniara.p2p4pussies;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.channels.Pipe;

import static java.time.Duration.ofSeconds;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

class SignalChannelTest {


    @Test
    @DisplayName("Test if websocket stuff is woking, MAKE SURE YOU HAVE LOCALHOST:7000 SERVER ON")
    void start() throws URISyntaxException {

        final String[] expectedout = {null};
        myChannel channel = new myChannel("http://192.168.1.38:3000", (s) -> {
            expectedout[0] = s;
        });
        channel.socketConnection();


        await().atMost(10, SECONDS).until(() -> expectedout[0] != null);

        assertNotNull(expectedout[0], expectedout[0]);
    }
}