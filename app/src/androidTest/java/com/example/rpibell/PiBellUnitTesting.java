package com.example.rpibell;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import static org.junit.Assert.*;

/**
 * This is the PiBellUnitTesting clas for the PiBell Android Application. Here we will be able to conduct the J-Unit testing
 * for the application.
 *
 * Preface: Much of the testing of the app requires the use of Firebase. One main issue is the lack of support in J-Unit testing
 * for Firebase (even within the Instrumented Testing as this). So these tests cases are ones that do not rely on Firebase and soley use
 * the connection between the app and the PiBell in order to test some of the requirements and standards that we have to meet.
 *
 * Pre-Condition: PiBell needs to be in a clean,fresh state (just started up) and have no pictures or message logs saved in the
 * temporary storage.
 */
@RunWith(AndroidJUnit4.class)
public class PiBellUnitTesting {

    final String SUCCESS = "DONE";              // Success message for all the test cases
    final String hostName = "czpi1";            // the hostname of the PiBell used for testing
    final String IP = getIP(hostName);          // the IP of the PiBell used for testing
    final int CONNECTION_PORT = 9000;           // the specific port used to connect to the server on the PiBell




    /**
     * Testcase: T1
     * Purpose: Will turn on the live stream on the PiBell. Also tests the ability of the PiBell server to remain in contact to the app
     * even when the live stream (a concurrent process) is running.
     * Expected Output: "DONE" if successful, "FAIL" otherwise
     * @throws Exception when connection to the server fails
     */
    @Test
    public void T1() throws Exception {
        assertEquals(SUCCESS,runLiveStream());
    }




    /**
     * Testcase: T2
     * Purpose: Armed PiBell will take a picture once a hand is detected. This is another concurrent process that must take place.
     * The server must still accept new connections and send status reports while it is armed.
     * Expected Output: "DONE" if successful, "FAIL" otherwise
     * @throws Exception when connection to the server fails
     */
    @Test
    public void T2() throws Exception {
        assertEquals(SUCCESS,detectHand());
    }




    /**
     * Testcase: T3
     * Purpose: Armed camera will only take a picture if a hand is detected. If no hand is in frame, then it should not
     * detect anything and not take a picture.
     * Expected Output: "DONE" if successful, "FAIL" otherwise
     * @throws Exception when connection to the server fails
     */
    @Test
    public void T3() throws Exception {
        assertEquals(SUCCESS, noHandDetect());
    }




    /**
     * Testcase: T4
     * Purpose: Simulate the server sending over all the media. The bytes of the picture should be sent in full.
     * Once the process is done, the server must still be active and ready to send back message logs for the next test.
     * Expected Output: "DONE" if successful, "FAIL" otherwise
     * @throws Exception when connection to the server fails
     */
    @Test
    public void T4() throws Exception {
        assertEquals(SUCCESS,getMediaFromServer());
    }




    /**
     * Testcase: T5
     * Purpose: Simulate the server sending over all the message logs. Once this method sends
     * of the bytes of the message logs, the server should still be active for any new incoming
     * messages without the server turning off.
     * Expected Output: "DONE" if successful, "FAIL" otherwise
     * @throws Exception when connection to the server fails
     */
    @Test
    public void T5() throws Exception {
        assertEquals(SUCCESS,getLogsFromServer());
    }




    /**
     * Testcase: T6
     * Purpose: Simulate the user turning off picture capture. Hand must be detected with only a
     * message log to follow.
     * Expected Output: "DONE" if successful, "FAIL" otherwise
     * @throws Exception when connection to the server fails
     */
    @Test
    public void T6() throws Exception {
        assertEquals(SUCCESS, detectHandNoPic());
    }




    /**
     * This method is used to find the IP address of the PiBell used for testing each of the other
     * methods in the Unit Testing class.
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
     * This method is used in T1 to turn the live stream on the PiBell on.
     * @return "DONE" if successful, "FAIL" otherwise
     * @throws Exception in the case the server gets disconnected
     */
    public String runLiveStream() throws Exception {
        try {
            // set local variables
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(IP,CONNECTION_PORT),2000);     // connection fails after 2 seconds of trying

            // set up the input and output streams
            DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
            DataInputStream din=new DataInputStream(socket.getInputStream());

            // tell the PiBell to start the live
            dout.writeUTF("StartLive");
            dout.flush();

            // server responds : "OK"
            din.readUTF();

            // close all
            dout.close();
            din.close();
            socket.close();
        } catch (Exception e) {
            return "FAIL";
        }

        // wait for 5 seconds for the PiBell live stream to get adjusted and then ask the PiBell if the server is running
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            throw new Exception();
        }

        // ask the PiBell if the server is running (save answer into String and return)
        String liveViewIsRunning = "";
        try {
            // set local variables
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(IP,CONNECTION_PORT),2000);     // connection fails after 2 seconds of trying

            // set up the input and output streams
            DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
            DataInputStream din=new DataInputStream(socket.getInputStream());

            // tell the PiBell to start the live
            dout.writeUTF("isLiveRunning");
            dout.flush();

            // server responds : "DONE" or "FAIL"
            liveViewIsRunning = din.readUTF();

            // close all
            dout.close();
            din.close();
            socket.close();
        } catch (Exception e) {
            return "FAIL";
        }

        return liveViewIsRunning;
    } // ends the runLiveStream() method




    /**
     * This method is used in T2 for successful detection of a hand.
     * @return "DONE" if successful, "FAIL" otherwise
     * @throws Exception in the case the server gets disconnected
     */
    public String detectHand() throws Exception {
        try {
            // set local variables
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(IP,CONNECTION_PORT),2000);     // connection fails after 2 seconds of trying

            // set up the input and output streams
            DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
            DataInputStream din=new DataInputStream(socket.getInputStream());

            // tell the PiBell to start the live
            dout.writeUTF("Arm Doorbell");
            dout.flush();

            // server responds : "OK"
            din.readUTF();

            // close all
            dout.close();
            din.close();
            socket.close();
        } catch (Exception e) {
            return "FAIL";
        }

        // wait for 30 seconds for the PiBell to detect the hand and take a picture
        try {
            Thread.sleep(30000);
        } catch (Exception e) {
            throw new Exception();
        }

        // ask the PiBell for the number of pics that have been saved
        String numberOfPics = "";
        try {
            // set local variables
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(IP,CONNECTION_PORT),2000);     // connection fails after 2 seconds of trying

            // set up the input and output streams
            DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
            DataInputStream din=new DataInputStream(socket.getInputStream());

            // tell the PiBell
            dout.writeUTF("Send Number of Pics");
            dout.flush();

            // server responds : # of pics
            numberOfPics = din.readUTF();

            // close all
            dout.close();
            din.close();
            socket.close();
        } catch (Exception e) {
            return "FAIL";
        }

        // save the number of pics (should == 1)
        int numPicsInt = Integer.parseInt(numberOfPics);
        if (numPicsInt == 1) {
            return "DONE";
        } else {
            return "FAIL";
        }
    } // ends the detectHand() method




    /**
     * This method is used in T3 in order to show that the armed PiBell will only
     * take a picture if a open hand is detected.
     * @return "DONE" if successful, "FAIL" otherwise
     * @throws Exception in the case the server gets disconnected
     */
    public String noHandDetect() throws Exception {
        try {
            // set local variables
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(IP,CONNECTION_PORT),2000);     // connection fails after 2 seconds of trying

            // set up the input and output streams
            DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
            DataInputStream din=new DataInputStream(socket.getInputStream());

            // tell the PiBell to start the live
            dout.writeUTF("Arm Doorbell");
            dout.flush();

            // server responds : "OK"
            din.readUTF();

            // close all
            dout.close();
            din.close();
            socket.close();
        } catch (Exception e) {
            return "FAIL";
        }

        // wait for 2 seconds before sending new request
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            throw new Exception();
        }

        // wait for 30 seconds for the PiBell to detect the hand
        try {
            Thread.sleep(30000);
        } catch (Exception e) {
            throw new Exception();
        }

        // ask the PiBell for the number of pics that have been saved
        String numberOfPics = "";
        try {
            // set local variables
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(IP,CONNECTION_PORT),2000);     // connection fails after 2 seconds of trying

            // set up the input and output streams
            DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
            DataInputStream din=new DataInputStream(socket.getInputStream());

            // tell the PiBell
            dout.writeUTF("Send Number of Pics");
            dout.flush();

            // server responds : # of pics
            numberOfPics = din.readUTF();

            // close all
            dout.close();
            din.close();
            socket.close();
        } catch (Exception e) {
            return "FAIL";
        }

        // should not have taken any new pics
        int numPicsTaken = Integer.parseInt(numberOfPics);
        if (numPicsTaken == 1) {        // There should only be one taken from Testcase T2
            return "DONE";
        } else {
            return "FAIL";
        }
    } // ends the noHandDetect() method




    /**
     * This method will be used in T4 to get the media from the server.
     * Server will send copy of the bytes of the picture taken. The process must be replicated.
     * @return "DONE" if successful, "FAIL" otherwise
     * @throws Exception in the case the server gets disconnected
     */
    public String getMediaFromServer() throws Exception {
        try {
            // set socket connection
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(IP,CONNECTION_PORT),2000);
            DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
            DataInputStream din=new DataInputStream(socket.getInputStream());

            // tell the server to end the live
            dout.writeUTF("Send Pics");
            dout.flush();

            // server responds : number of pics
            String numberOfPics = din.readUTF();
            Log.e("NUM PICS",numberOfPics);

            // say OK
            dout.writeUTF("OK");
            dout.flush();

            // now server will send those pics here
            int num = Integer.parseInt(numberOfPics);
            for (int i = 1 ; i <= num ; ++i) {
                // get the name of the pic
                din.readUTF();

                // say OK
                dout.writeUTF("OK");
                dout.flush();

                // get the size of the file
                String picSize = din.readUTF();

                // say OK
                dout.writeUTF("OK");

                // copy the bytes to buffer
                int count;
                int fileSize = Integer.parseInt(picSize);
                byte[] buffer = new byte[4096]; // or 4096, or more
                while ((count = din.read(buffer)) > 0)
                {
                    fileSize -= count;
                    if (fileSize <= 0) {
                        break;
                    }
                } // ends the while-loop

                // say OK
                dout.writeUTF("OK");
                dout.flush();
            } // ends the for-loop

            // server sends last OK
            din.readUTF();

            // close all
            dout.close();
            din.close();
            socket.close();
        } catch (Exception e) {
            return "FAIL";
        }

        // wait for 2 seconds before sending new request
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            throw new Exception();
        }

        // ask the PiBell for the number of pics that have been saved
        String numberOfPics = "";
        try {
            // set local variables
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(IP,CONNECTION_PORT),2000);     // connection fails after 2 seconds of trying

            // set up the input and output streams
            DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
            DataInputStream din=new DataInputStream(socket.getInputStream());

            // tell the PiBell
            dout.writeUTF("Send Number of Pics");
            dout.flush();

            // server responds : # of pics
            numberOfPics = din.readUTF();

            // close all
            dout.close();
            din.close();
            socket.close();
        } catch (Exception e) {
            return "FAIL";
        }

        // should not have taken any new pics
        int numPicsTaken = Integer.parseInt(numberOfPics);
        if (numPicsTaken == 0) {        // All pics must be removed after sending from server to app
            return "DONE";
        } else {
            return "FAIL";
        }
    } // ends the getMediaFromServer() method




    /**
     * This method will be used in T5 to get the logs from the server.
     * Log name will be sent. That is the most important since it is the timestamp needed.
     * @return "DONE" if successful, "FAIL" otherwise
     * @throws Exception in the case the server gets disconnected
     */
    public String getLogsFromServer() throws Exception {
        try {
            // set up socket connection
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(IP,CONNECTION_PORT),2000);
            DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
            DataInputStream din=new DataInputStream(socket.getInputStream());

            // tell the server to send message logs
            dout.writeUTF("Send Notifs");
            dout.flush();

            // server responds : number of notifications
            String numberOfNotifs = din.readUTF();

            // say OK
            dout.writeUTF("OK");
            dout.flush();

            // now server will send those pics here
            int num = Integer.parseInt(numberOfNotifs);
            for (int i = 1 ; i <= num ; ++i) {
                // get the notif timestamp (the name ... important part of the file)
                din.readUTF();

                // say ok
                dout.writeUTF("OK");
                dout.flush();
            } // ends the for-loop

            // server sends last OK
            din.readUTF();

            // close all
            dout.close();
            din.close();
            socket.close();
        } catch (Exception e) {
            return "FAIL";
        }

        // wait for 2 seconds before sending new request
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            throw new Exception();
        }

        // ask the PiBell for the number of logs that have been saved
        String numberOfLogs = "";
        try {
            // set local variables
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(IP,CONNECTION_PORT),2000);     // connection fails after 2 seconds of trying

            // set up the input and output streams
            DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
            DataInputStream din=new DataInputStream(socket.getInputStream());

            // tell the PiBell
            dout.writeUTF("Send Number of Messages");
            dout.flush();

            // server responds : # of logs
            numberOfLogs = din.readUTF();

            // close all
            dout.close();
            din.close();
            socket.close();
        } catch (Exception e) {
            return "FAIL";
        }

        // count number of logs
        int numLogs = Integer.parseInt(numberOfLogs);
        if (numLogs == 1) {        // Log from T2
            return "DONE";
        } else {
            return "FAIL";
        }
    } // ends the getLogsFromServer() method




    /**
     * This method is used in T6 in order to test the picture capture feature. Here the user will turn the
     * picture capture off and expect only a timestamp log to be made
     * @return "DONE" if successful, "FAIL" otherwise
     * @throws Exception in the case the server gets disconnected
     */
    public String detectHandNoPic() throws Exception{
        try {
            // set local variables
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(IP,CONNECTION_PORT),2000);     // connection fails after 2 seconds of trying

            // set up the input and output streams
            DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
            DataInputStream din=new DataInputStream(socket.getInputStream());

            // tell the PiBell to start the live
            dout.writeUTF("Arm Doorbell");
            dout.flush();

            // server responds : "OK"
            din.readUTF();

            // close all
            dout.close();
            din.close();
            socket.close();
        } catch (Exception e) {
            return "FAIL";
        }

        // wait for 2 seconds before sending new request
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            throw new Exception();
        }

        try {
            // set local variables
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(IP,CONNECTION_PORT),2000);     // connection fails after 2 seconds of trying

            // set up the input and output streams
            DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
            DataInputStream din=new DataInputStream(socket.getInputStream());

            // tell the PiBell to start the live
            dout.writeUTF("Pic Capture OFF");
            dout.flush();

            // server responds : "OK"
            din.readUTF();

            // close all
            dout.close();
            din.close();
            socket.close();
        } catch (Exception e) {
            return "FAIL";
        }

        // wait for 30 seconds for the PiBell to detect the hand
        try {
            Thread.sleep(30000);
        } catch (Exception e) {
            throw new Exception();
        }

        // ask the PiBell for the number of pics that have been saved
        String numberOfPics = "";
        try {
            // set local variables
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(IP,CONNECTION_PORT),2000);     // connection fails after 2 seconds of trying

            // set up the input and output streams
            DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
            DataInputStream din=new DataInputStream(socket.getInputStream());

            // tell the PiBell
            dout.writeUTF("Send Number of Pics");
            dout.flush();

            // server responds : # of pics
            numberOfPics = din.readUTF();

            // close all
            dout.close();
            din.close();
            socket.close();
        } catch (Exception e) {
            return "FAIL";
        }

        // should not have taken any new pics
        int numPicsTaken = Integer.parseInt(numberOfPics);
        if (numPicsTaken == 0) {        // There should new pics taken
            return "DONE";
        } else {
            return "FAIL";
        }
    } // ends the detectHandNoPic() method




} // ends the PiBellUnitTesting class

