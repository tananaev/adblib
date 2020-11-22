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
            byte[] response = stream.read();
            String responseText = new String(response, StandardCharsets.UTF_8);
            Assert.assertEquals("Hello world", responseText.trim());
        }
    }

    @Test
    public void doesntDeliverRemainingDataOnLocalStreamClose() throws Exception {
        final AtomicBoolean receivedDataAfterClose = new AtomicBoolean(false);
        final CountDownLatch streamClosed = new CountDownLatch(1);

        connection.connect();

        final AdbStream stream = connection.open("shell:");

        Thread readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    streamClosed.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException("Timed out waiting for stream to close");
                }

                try {
                    stream.read();
                } catch (InterruptedException e) {
                    throw new RuntimeException("Unexpectedly interrupted in read thread");
                } catch (IOException ignored) {
                    return; // Stream closed, so we finished without receiving the data
                }

                receivedDataAfterClose.set(true);
            }
        });
        readThread.start();

        stream.write("echo Hello world");
        Thread.sleep(1000); // Giving the peer time to run the command and send the output back
        stream.close();
        streamClosed.countDown();

        readThread.join(1000);

        Assert.assertFalse("Read thread did not finish after we closed the stream", readThread.isAlive());
        Assert.assertFalse("Received more data after we closed the stream", receivedDataAfterClose.get());
    }

}
