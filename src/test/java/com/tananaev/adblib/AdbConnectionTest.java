package com.tananaev.adblib;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdbConnectionTest {

    private Socket socket;
    private AdbConnection connection;

    @Before
    public void beforeTest() throws Exception {
        socket = new Socket("192.168.1.103", 5555);

        AdbCrypto crypto = AdbCrypto.generateAdbKeyPair(new AdbBase64() {
            @Override
            public String encodeToString(byte[] data) {
                return DatatypeConverter.printBase64Binary(data);
            }
        });

        connection = AdbConnection.create(socket, crypto);
    }

    @After
    public void afterTest() throws Exception {
        connection.close();
        connection = null;
        socket.close();
        socket = null;
    }

    @Test
    public void testConnection() throws Exception {

        connection.connect();

        AdbStream stream = connection.open("shell:");

    }

    @Test
    public void deliversRemainingDataOnRemoteStreamClose() throws Exception {
        connection.connect();

        try (AdbStream stream = connection.open("shell:echo Hello world")) {
            Thread.sleep(1000);
            Assert.assertFalse("Stream showed as closed before we read the data", stream.isClosed());
            byte[] response = stream.read();
            String responseText = new String(response, StandardCharsets.UTF_8);
            Assert.assertEquals("Hello world", responseText.trim());
        }
    }

    @Test
    public void doesntDeliverRemainingDataOnLocalStreamClose() throws Exception {
        connection.connect();

        AdbStream stream = connection.open("shell:"); // Starting empty shell so it won't self-close
        stream.write("echo Hello world");
        Thread.sleep(1000); // Giving the peer time to run the command and send the output back
        stream.close();

        boolean receivedDataAfterClose;
        try {
            stream.read();
            receivedDataAfterClose = true;
        } catch (IOException ignored) {
            receivedDataAfterClose = false;
        }

        Assert.assertFalse("Received data after we closed the stream", receivedDataAfterClose);
    }

}
