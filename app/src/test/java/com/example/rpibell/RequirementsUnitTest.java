package com.example.rpibell;

import android.util.Log;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class RequirementsUnitTest {

    String IP = getIP("czpi1");
    final int PORT = 9000;

    @Test
    public void check_IP() { assertNotEquals(null,IP); }

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    /**
     * EXAMPLE ....
     * Tests Requirements: REQ-000 , REQ-001
     */
    public void String_isCorrect() {
        assertEquals("DONE", testMethod(IP, PORT));
    }

    public String testMethod(String IP, int portNum) {
        return "DONE";
    }

    public String getIP(String hostname) {
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName(hostname);
            if (addr == null) {
                return null;
            }
        } catch (UnknownHostException e) {
            return null;
        }
        return addr.getHostAddress();
    }

}