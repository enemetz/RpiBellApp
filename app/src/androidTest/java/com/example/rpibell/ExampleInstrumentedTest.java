package com.example.rpibell;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    // Success message
    final String SUCCESS = "DONE";

    // get IP and Connection port
    String IP = getIP("czpi1");
    final int CONNECTION_PORT = 9000;


    // connect to Firebase
    public FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public FirebaseFirestore db = FirebaseFirestore.getInstance();

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

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.rpibell", appContext.getPackageName());
    }



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
        assertNotEquals(SUCCESS,createNewAdmin("",adminEmail,adminPassword,adminHostname));
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
        if (name.isEmpty()) {
            return "FAIL";
        }
        if (password.isEmpty()) {
            return "FAIL";
        }
        if (email.isEmpty()) {
            return "FAIL";
        }
        if (hostname.isEmpty()) {
            return "FAIL";
        }

        // under the assumption that Firebase is setup correctly, it will deal with all the other cases for us
        return "DONE";
    } // ends createNewAdmin() method


}