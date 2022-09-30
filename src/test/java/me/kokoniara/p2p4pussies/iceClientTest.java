package me.kokoniara.p2p4pussies;

import me.kokoniara.p2p4pussies.cupcakkebussylice.IceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class iceClientTest {


    IceClient iceClient;

    @BeforeEach
    void setup() {
        iceClient = new IceClient(8888, "text", new String[]{"localhost:3478"}, new String[]{"localhost:3478"});
    }

    @Test
    @DisplayName("Make sure sdp is not null")
    void testInit() throws Throwable {
        String localsdp = iceClient.createLocalSdp();
        assertNotEquals(localsdp, null);
    }
}