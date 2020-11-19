package com.tananaev.adblib;

/**
 * Thrown when the peer rejects our initial authentication attempt,
 * which typically means that the peer has not previously saved our
 * public key.
 *
 * This is an unchecked exception for backwards-compatibility.
 */
public class AdbAuthenticationFailedException extends RuntimeException {

    public AdbAuthenticationFailedException() {
        super("Initial authentication attempt rejected by peer");
    }

}
