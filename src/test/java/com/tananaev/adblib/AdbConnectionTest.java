package com.tananaev.adblib;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.net.Socket;

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

}
