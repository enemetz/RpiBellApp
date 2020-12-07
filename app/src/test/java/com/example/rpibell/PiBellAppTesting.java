package com.example.rpibell;



import android.os.SystemClock;

import org.junit.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static org.junit.Assert.*;

public class PiBellAppTesting {

    // Success message
    final String SUCCESS = "DONE";

    // get IP and Connection port
    String IP = getIP("czpi1");
    final int CONNECTION_PORT = 9000;

    // admin credentials to test
    String adminName = "TestAdminName";
    String adminEmail = "TestAdminEmail@email.com";
    String adminPassword = "TestAdminPassword";
    String adminHostname = "czpi1";

    // guest credentials to test
    String guestName = "TestGuestName";
    String guestEmail = "TestGuestEmail@email.com";
    String guestPassword = "TestGuestPassword";
    String guestHostname = "czpi1";
    String guestAdminID = "TestGuestAdminID";


    /**
     * Testcase: T01
     * Purpose: Create an admin account
     * Pre-Condition(s): Admin Account does not already exist and given credentials follow the hints provided
     * Inputs: Name, Email, Password, PiBell Hostname
     * Expected Outputs: "DONE", which means that the admin was successfully added.
     * Requirements Tested: REQ-000, REQ-011, REQ-100, REQ-102, REQ-104, REQ-105
     */
    @Test
    public void T01() {
        // success
        assertEquals(SUCCESS,createNewAdmin(adminName,adminEmail,adminPassword,adminHostname));

        // fail
        assertNotEquals(SUCCESS,createNewAdmin("WrongName",adminEmail,adminPassword,adminHostname));
        assertNotEquals(SUCCESS,createNewAdmin("",adminEmail,adminPassword,adminHostname));
    }




    /**
     * Testcase: T02
     * Purpose: Create a guest account
     * Pre-Condition(s): Guest Account does not already exist and given credentials follow the hints provided
     * Inputs: Name, Email, Password, PiBell Hostname, Admin ID
     * Expected Outputs: "DONE", which means that the admin was successfully added.
     * Requirements Tested: REQ-001, REQ-011, REQ-100, REQ-102, REQ-104, REQ-105
     */
    @Test
    public void T02() {
        assertEquals(SUCCESS,createNewGuest(guestName,guestEmail,guestPassword,guestHostname,guestAdminID));
    }



    @Test
    public void TestCon() throws Exception {
        assertEquals(SUCCESS,testConnection());
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            throw new Exception();
        }
    }






    /**
     * This method is used to find the IP address of the PiBell used for testing.
     * @param hostname the hostname of the PiBell for testing
     * @return the IP address String or null if not found
     */
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
    } // ends getIP() method






    /**
     * This method is used to test the logic behind creating a new admin account.
     * @param name new admin's name
     * @param email new admin's email
     * @param password new admin's password
     * @param hostname new admin's PiBell hostname
     * @return "DONE" on success, "FAIL" otherwise
     */
    public String createNewAdmin(String name, String email, String password, String hostname) {
        if (name.isEmpty() || !name.equals(adminName)) {
            return "FAIL";
        }
        if (password.isEmpty() || !password.equals(adminPassword)) {
            return "FAIL";
        }
        if (email.isEmpty() || !email.equals(adminEmail)) {
            return "FAIL";
        }
        if (hostname.isEmpty() || !hostname.equals(adminHostname)) {
            return "FAIL";
        }
        // with the assumption that this admin does not already exist in the DB, then it is good to add
        return "DONE";
    } // ends createNewAdmin() method






    /**
     * This method is used to test the logic behind creating a new guest account.
     * @param name the name of the guest
     * @param email the email of the guest
     * @param password the password of the new guest account
     * @param hostname the PiBell hostname
     * @param ID the admin that the guest is associated to
     * @return "DONE" on success, "FAIL" otherwise
     */
    public String createNewGuest(String name, String email, String password, String hostname, String ID) {
        if (name.isEmpty() || !name.equals(guestName)) {
            return "FAIL";
        }
        if (password.isEmpty() || !password.equals(guestPassword)) {
            return "FAIL";
        }
        if (email.isEmpty() || !email.equals(guestEmail)) {
            return "FAIL";
        }
        if (hostname.isEmpty() || !hostname.equals(guestHostname)) {
            return "FAIL";
        }
        if (ID.isEmpty() || !ID.equals(guestAdminID)) {
            return "FAIL";
        }

        // with the assumption that this guest does not already exist in the DB, then it is good to add
        return "DONE";
    } // ends the createNewGuest() method






    public String testConnection() throws Exception {
        try {
            // set local variables
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(IP,CONNECTION_PORT),2000);
            DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
            DataInputStream din=new DataInputStream(socket.getInputStream());

            // tell the server to start the live
            dout.writeUTF("Testing");
            dout.flush();

            // server responds : "OK"
            din.readUTF();

            // close all
            dout.close();
            din.close();
            socket.close();
            //return "DONE";
        } catch (Exception e) {
            return "FAIL";
        }

        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            throw new Exception();
        }

        try {
            // set local variables
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(IP,CONNECTION_PORT),2000);
            DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
            DataInputStream din=new DataInputStream(socket.getInputStream());

            // tell the server to start the live
            dout.writeUTF("Testing");
            dout.flush();

            // server responds : "OK"
            din.readUTF();

            // close all
            dout.close();
            din.close();
            socket.close();
            return "DONE";
        } catch (Exception e) {
            return "FAIL";
        }

    }






} // ends the PiBellAppTesting class
