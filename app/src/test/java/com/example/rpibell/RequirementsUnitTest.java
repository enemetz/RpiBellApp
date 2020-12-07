package com.example.rpibell;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is the J-Unit Test Class for this PiBell Android Application.
 */
public class RequirementsUnitTest {
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


    /**
     * Testcase: T01
     * Purpose: Create an admin account
     * Pre-Condition(s): Admin Account does not already exist
     * Inputs: Name, Email, Password, PiBell Hostname
     * Expected Outputs: "DONE", which means that the admin was successfully added.
     * Requirements Tested: REQ-000, REQ-011, REQ-100, REQ-102, REQ-104, REQ-105
     */
    @Test
    public void T01() {
        assertEquals(SUCCESS,createNewAdmin(adminName,adminEmail,adminPassword,adminHostname));
    }




    /**
     * This method is used to create a new admin account.
     * @param name new admin's name
     * @param email new admin's email
     * @param password new admin's password
     * @param hostname new admin's PiBell hostname
     * @return "DONE" on success, "FAIL" otherwise
     */
    public String createNewAdmin(String name, String email, String password, String hostname) {
        // Firebase connections
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (mAuth.createUserWithEmailAndPassword(email, password).isSuccessful()) {
            Map<String, Object> profile = new HashMap<>();
            profile.put("email", email);
            profile.put("hostname", hostname);
            profile.put("name", name);
            profile.put("password",password);
            profile.put("role","admin");
            if (db.collection("admins").document(mAuth.getCurrentUser().getUid()).set(profile).isSuccessful()) {
                return "DONE";
            } else {
                return "FAIL";
            }
        } else {
            return "FAIL";
        }
    } // ends createNewAdmin() method





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


   // @Test
    //public void addition_isCorrect() {
        //assertEquals(4, 2 + 2);
    //}

} // ends RequirementsUnitTest Class