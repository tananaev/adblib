package com.tananaev.adblib;

import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.net.Socket;

public class AdbConnectionTest {

    //@Test
    public void testConnection() throws Exception {

        Socket socket = new Socket("192.168.1.15", 5555);

        AdbCrypto crypto = AdbCrypto.generateAdbKeyPair(new AdbBase64() {
            @Override
            public String encodeToString(byte[] data) {
                return DatatypeConverter.printBase64Binary(data);
            }
        });

        AdbConnection connection = AdbConnection.create(socket, crypto);

        connection.connect();

        AdbStream stream = connection.open("shell");

    }

}
