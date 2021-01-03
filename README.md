# ADB Library - adblib

## Overview

A Java library implementation of [the ADB (Android Debug Bridge) network protocol](https://android.googlesource.com/platform/system/core/+/master/adb/protocol.txt).

This project is a fork of the [original library](https://github.com/cgutman/AdbLib) developed by Cameron Gutman.

## Usage

Include dependency via Gradle:
```groovy
compile 'com.tananaev:adblib:1.3'
```
or Maven:
```xml
<dependency>
  <groupId>com.tananaev</groupId>
  <artifactId>adblib</artifactId>
  <version>1.3</version>
</dependency>
```

To be able to connect to the ADB daemon on Android phone, you need to enable it to listen to TCP connections. To do that, connect your phone via USB cable and run following adb command:
```
adb tcpip 5555
```

Disconnect USB cable before trying to connect using the library. Some phones have problems handling TCP connection when they are connected via USB as well.

More info about Android remote debugging can be found on the official [Android developer website](https://developer.android.com/studio/command-line/adb.html#wireless).

Sample library usage example:
```java
Socket socket = new Socket("192.168.1.42", 5555); // put phone IP address here

AdbCrypto crypto = AdbCrypto.generateAdbKeyPair(new AdbBase64() {
    @Override
    public String encodeToString(byte[] data) {
        return DatatypeConverter.printBase64Binary(data);
    }
});

AdbConnection connection = AdbConnection.create(socket, crypto);
connection.connect();

AdbStream stream = connection.open("shell:logcat");

...
```

## License

    Copyright (c) 2013, Cameron Gutman
    All rights reserved.

    Redistribution and use in source and binary forms, with or without modification,
    are permitted provided that the following conditions are met:

      Redistributions of source code must retain the above copyright notice, this
      list of conditions and the following disclaimer.

      Redistributions in binary form must reproduce the above copyright notice, this
      list of conditions and the following disclaimer in the documentation and/or
      other materials provided with the distribution.

      Neither the name of the {organization} nor the names of its
      contributors may be used to endorse or promote products derived from
      this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
    ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
