package com.tananaev.adblib;

import org.junit.*;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Ignore
public class AdbStreamTest {

    private static AdbCrypto crypto;

    private Socket socket;
    private AdbConnection connection;

    @BeforeClass
    public static void beforeClass() throws Exception {
        crypto = AdbCrypto.generateAdbKeyPair(new AdbBase64() {
            @Override
            public String encodeToString(byte[] data) {
                return DatatypeConverter.printBase64Binary(data);
            }
        });
    }

    @Before
    public void beforeTest() throws Exception {
        socket = new Socket("192.168.1.103", 5555);

        try {
            connection = AdbConnection.create(socket, crypto);
            connection.connect(Long.MAX_VALUE, TimeUnit.MILLISECONDS, true);
        } catch (AdbAuthenticationFailedException e) {
            System.out.println("On the target device, check 'Always allow from this computer' and press Allow");
            connection = AdbConnection.create(socket, crypto);
            connection.connect();
        }
    }

    @After
    public void afterTest() throws Exception {
        connection.close();
        connection = null;
        socket.close();
        socket = null;
    }

    @Test
    public void deliversRemainingDataOnRemoteStreamClose() throws Exception {
        try (AdbStream stream = connection.open("shell:echo Hello world")) {
            Thread.sleep(1000); // Giving the peer time to send us the "close stream" message
            Assert.assertFalse("Stream showed as closed before we read the data", stream.isClosed());
            byte[] response = stream.read();
            String responseText = new String(response, StandardCharsets.UTF_8);
            Assert.assertEquals("Hello world", responseText.trim());
        }
    }

    @Test
    public void showsAsClosedOnRemoteStreamCloseWithoutPendingData() throws Exception {
        try (AdbStream stream = connection.open("shell:echo Hello world")) {
            // Emptying the stream read queue
            byte[] response = stream.read();
            String responseText = new String(response, StandardCharsets.UTF_8);
            Assert.assertEquals("Hello world", responseText.trim());

            // Waiting for close message to arrive
            Thread.sleep(1000);

            Assert.assertTrue("Stream doesn't show as closed after the peer closed it and we emptied the read queue", stream.isClosed());
        }
    }

    @Test
    public void immediatelyShowsAsClosedOnLocalStreamClose() throws Exception {
        AdbStream stream = connection.open("shell:"); // Starting empty shell so it won't self-close
        stream.write("echo Hello world");
        Thread.sleep(1000); // Giving the peer time to run the command and send the output back
        stream.close();

        Assert.assertTrue("Stream not showing as closed after we closed it", stream.isClosed());
    }

    @Test
    public void doesntDeliverRemainingDataOnLocalStreamClose() throws Exception {
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

    @Test
    public void showsAsClosedWhenClosedByPeerWhileEmpty() throws Exception {
        AdbStream stream = connection.open("shell: Hello world");
        stream.read(); // Emptying the stream
        Thread.sleep(1000); // Allowing time for the peer to send the close message
        Assert.assertTrue("Empty stream not showing as closed after close message received", stream.isClosed());
    }

}
