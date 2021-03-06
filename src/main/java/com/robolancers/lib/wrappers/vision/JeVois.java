package com.robolancers.lib.wrappers.vision;

import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoMode.PixelFormat;
import edu.wpi.cscore.VideoSource;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.Timer;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"FieldCanBeLocal", "unused", "WeakerAccess", "UnusedReturnValue", "SameParameterValue"})
public class JeVois {
    // Serial Port Constants
    private static final int BAUD_RATE = 115200;

    // MJPG Streaming Constants
    private static AtomicInteger MJPG_STREAM_PORT = new AtomicInteger(1180);
    private static AtomicInteger CAMERA_NUMBER = new AtomicInteger(0);

    // Packet format constants
    private static final String PACKET_START_CHAR = "{";
    private static final String PACKET_END_CHAR = "}";
    private static final String PACKET_DILEM_CHAR = ",";
    private static final int PACKET_NUM_EXPECTED_FIELDS = 2;

    // When streaming, use this set of configuration
    private static final int STREAM_WIDTH_PX = 320;
    private static final int STREAM_HEIGHT_PX = 240;
    private static final int STREAM_RATE_FPS = 30;

    // Serial port used for getting target data from JeVois
    private SerialPort visionPort;

    // USBCam and server used for broadcasting a webstream of what is seen
    private UsbCamera visionCam;
    private MjpegServer camServer;

    // Status variables
    private boolean camStreamRunning;
    private boolean visionOnline;

    // Most recently seen target information
    private boolean targetVisible;
    private double targetDistance;
    private double targetAngle;

    /**
     * Constructor (more complex). Opens a USB serial port to the JeVois camera, sends a few test commands checking for error,
     * then fires up the user's program and begins listening for target info packets in the background.
     * Pass TRUE to additionally enable a USB camera stream of what the vision camera is seeing.
     */
    public JeVois(SerialPort.Port port) {
        int retry_counter = 0;

        //Retry strategy to get this serial port open.
        //I have yet to see a single retry used assuming the camera is plugged in
        // but you never know.
        while(visionPort == null && retry_counter++ < 10){
            try {
                System.out.print("Creating JeVois SerialPort...");
                visionPort = new SerialPort(BAUD_RATE, port);
                System.out.println("SUCCESS!!");
            } catch (Exception e) {
                System.out.println("FAILED!!");
                e.printStackTrace();
                sleep(500);
                System.out.println("Retry " + retry_counter);
            }
        }


        //Report an error if we didn't get to open the serial port
        if(visionPort == null){
            DriverStation.reportError("Cannot open serial port to JeVois. Not starting vision system.", false);
            return;
        }

        //Test to make sure we are actually talking to the JeVois
        if(sendPing() != 0){
            DriverStation.reportError("JeVois ping test failed. Not starting vision system.", false);
            return;
        }

        startCameraStream();

        //Start listening for packets
        packetListenerThread.setDaemon(true);
        packetListenerThread.start();
    }

    /*
     * Main getters/setters
     */

    /**
     * Returns the most recently seen target's distance in inches
     */
    public double getTargetDistance(){
        return targetDistance;
    }

    /**
     * Returns the most recently seen target's angle relative to the camera in degrees
     * Positive means to the left of center, negative means to the right
     */
    public double getTargetAngle() {
        return targetAngle;
    }

    /**
     * Returns true when the roboRIO is receiving packets from the JeVois, false if no packets have been received.
     * Other modules should not use the vision processing results if this returns false.
     */
    public boolean isVisionOnline() {
        return visionOnline;
    }

    /**
     * Returns true when the JeVois sees a target and is tracking it, false otherwise.
     */
    public boolean isTargetVisible() {
        return targetVisible;
    }

    public VideoSource getVisionCam(){
        return visionCam;
    }

    /**
     * This is the main periodic update function for the Listener. It is intended
     * to be run in a background task, as it will block until it gets packets.
     */
    private void backgroundUpdate(){
        // Grab packets and parse them.
        sendCmd("target");
        String packet = blockAndGetPacket(2.0);

        if(packet != null){
            visionOnline = parsePacket(packet) == 0;
        } else {
            visionOnline = false;
            DriverStation.reportWarning("Cannot get packet from JeVois Vision Processor", false);
        }
    }

    /**
     * Send the ping command to the JeVois to verify it is connected
     * @return 0 on success, -1 on unexpected response, -2 on timeout
     */
    private int sendPing() {
        int retval = -1;
        if (visionPort != null){
            retval = sendCmdAndCheck("ping");
        }
        return retval;
    }

    /**
     * Open an Mjpeg streamer from the JeVois camera
     */
    private void startCameraStream(){
        try{
            System.out.println("Starting JeVois Cam Stream...");

            visionCam = CameraServer.getInstance().startAutomaticCapture();
            visionCam.setVideoMode(PixelFormat.kMJPEG, STREAM_WIDTH_PX, STREAM_HEIGHT_PX, STREAM_RATE_FPS);

            camStreamRunning = true;
            System.out.println("SUCCESS!!");
        } catch (Exception e) {
            DriverStation.reportError("Cannot start camera stream from JeVois", false);
            e.printStackTrace();
        }
    }

    /**
     * Sends a command over serial to JeVois and returns immediately.
     * @param cmd String of the command to send (ex: "ping")
     * @return number of bytes written
     */
    private int sendCmd(String cmd){
        int bytes = 0;

        if(visionPort != null) {
            bytes = visionPort.writeString(cmd + "\n");
            // System.out.println("wrote " + bytes + "/" + (cmd.length() + 1) + " bytes, cmd: " + cmd);
        }

        return bytes;
    }

    /**
     * Sends a command over serial to the JeVois, waits for a response, and checks that response
     * Automatically ends the line termination character.
     * @param cmd String of the command to send (ex: "ping")
     * @return 0 if OK detected, -1 if ERR detected, -2 if timeout waiting for response
     */
    public int sendCmdAndCheck(String cmd){
        sendCmd(cmd);
        int retval = blockAndCheckForOK(1.0);
        if(retval == -1){
            System.out.println(cmd + " Produced an error");
        } else if (retval == -2) {
            System.out.println(cmd + " timed out");
        }
        return retval;
    }

    //Persistent but "local" variables for getBytesPeriodic()
    private String getBytesWork = "";
    private int loopCount = 0;

    /**
     * Read bytes from the serial port in a non-blocking fashion
     * Will return the whole thing once the first "OK" or "ERR" is seen in the stream.
     * Returns null if no string read back yet.
     */
    private String getCmdResponseNonBlock() {
        String retval =  null;
        if (visionPort != null){
            if (visionPort.getBytesReceived() > 0) {
                String rxString = visionPort.readString();
                System.out.println("Waited: " + loopCount + " loops, Rcv'd: " + rxString);
                getBytesWork += rxString;
                if(getBytesWork.contains("OK") || getBytesWork.contains("ERR")){
                    retval = getBytesWork;
                    getBytesWork = "";
                    System.out.println(retval);
                }
                loopCount = 0;
            } else {
                ++loopCount;
            }
        }
        return retval;
    }

    /**
     * Blocks thread execution till we get a response from the serial line
     * or timeout.
     * Return values:
     *  0 = OK in response
     * -1 = ERR in response
     * -2 = No token found before timeout_s
     */
    private int blockAndCheckForOK(double timeout_s){
        int retval = -2;
        double startTime = Timer.getFPGATimestamp();
        StringBuilder testStr = new StringBuilder();
        if (visionPort != null){
            while(Timer.getFPGATimestamp() - startTime < timeout_s){
                if (visionPort.getBytesReceived() > 0) {
                    testStr.append(visionPort.readString());
                    if(testStr.toString().contains("OK")){
                        retval = 0;
                        break;
                    }else if(testStr.toString().contains("ERR")){
                        DriverStation.reportError("JeVois reported error:\n" + testStr.toString(), false);
                        retval = -1;
                        break;
                    }
                } else {
                    sleep(10);
                }
            }
        }
        return retval;
    }


    // buffer to contain data from the port while we gather full packets
    private StringBuffer packetBuffer = new StringBuffer(100);
    /**
     * Blocks thread execution till we get a valid packet from the serial line
     * or timeout.
     * Return values:
     *  String = the packet
     *  null = No full packet found before timeout_s
     */
    private String blockAndGetPacket(double timeout_s){
        String retval = null;
        double startTime = Timer.getFPGATimestamp();
        int endIdx;
        int startIdx;

        if (visionPort != null){
            while(Timer.getFPGATimestamp() - startTime < timeout_s){
                // Keep trying to get bytes from the serial port until the timeout expires.
                if (visionPort.getBytesReceived() > 0) {
                    // If there are any bytes available, read them in and
                    //  append them to the buffer.
                    packetBuffer.append(visionPort.readString());

                    // Attempt to detect if the buffer currently contains a complete packet
                    if(packetBuffer.indexOf(PACKET_START_CHAR) != -1){
                        endIdx = packetBuffer.lastIndexOf(PACKET_END_CHAR);
                        if(endIdx != -1){
                            // Buffer also contains at least one start & end character.
                            // But we don't know if they're in the right order yet.
                            // Start by getting the most-recent packet end character's index


                            // Look for the index of the start character for the packet
                            //  described by endIdx. Note this line of code assumes the
                            //  start character for the packet must come _before_ the
                            //  end character.
                            startIdx = packetBuffer.lastIndexOf(PACKET_START_CHAR, endIdx);

                            if(startIdx == -1){
                                // If there was no start character before the end character,
                                //  we can assume that we have something a bit wacky in our
                                //  buffer. For example: ",abc}garbage{1,2".
                                // Since we've started to receive a good packet, discard
                                //  everything prior to the start character.
                                startIdx = packetBuffer.lastIndexOf(PACKET_START_CHAR);
                                packetBuffer.delete(0, startIdx);
                            } else {
                                // Buffer contains a full packet. Extract it and clean up buffer
                                retval = packetBuffer.substring(startIdx + 1, endIdx - 1);
                                packetBuffer.delete(0, endIdx+1);
                                break;
                            }
                        } else {
                            // In this case, we have a start character, but no end to the buffer yet.
                            //  Do nothing, just wait for more characters to come in.
                            sleep(5);
                        }
                    } else {
                        // Buffer contains no start characters. None of the current buffer contents can
                        //  be meaningful. Discard the whole thing.
                        packetBuffer.delete(0, packetBuffer.length());
                        sleep(5);
                    }
                } else {
                    sleep(5);
                }
            }
        }
        return retval;
    }

    /**
     * Private wrapper around the Thread.sleep method, to catch that interrupted error.
     * @param time_ms Time to wait
     */
    private void sleep(int time_ms){
        try {
            Thread.sleep(time_ms);
        } catch (InterruptedException e) {
            System.out.println("DO NOT WAKE THE SLEEPY BEAST");
            e.printStackTrace();
        }
    }

    /**
     * Mostly for debugging. Blocks execution forever and just prints all serial
     * characters to the console. It might print a different message too if nothing
     * comes in.
     */
    public void blockAndPrintAllSerial(){
        if (visionPort != null){
            while(!Thread.interrupted()){
                if (visionPort.getBytesReceived() > 0) {
                    System.out.print(visionPort.readString());
                } else {
                    System.out.println("Nothing Rx'ed");
                    sleep(100);
                }
            }
        }

    }

    /**
     * Parse individual numbers from a packet
     * @param pkt Packet received
     */
    public int parsePacket(String pkt){
        //Parsing constants. These must be aligned with JeVois code.
        final int TGT_VISIBLE_TOKEN_IDX = 0;
        final int TGT_DISTANCE_TOKEN_IDX = 2;
        final int TGT_ANGLE_TOKEN_IDX = 3;

        //Split string into many substrings, presuming those strings are separated by commas
        String[] tokens = pkt.split(",");

        //Check there were enough substrings found
        if(tokens.length < PACKET_NUM_EXPECTED_FIELDS){
            DriverStation.reportError("Got malformed vision packet. Expected 2 tokens, but only found " + tokens.length + ". Packet Contents: " + pkt, false);
            return -1;
        }

        //Convert each string into the proper internal value
        try {
            //Use Java built-in double to string conversion on most of the rest
            targetVisible = Double.parseDouble(tokens[TGT_VISIBLE_TOKEN_IDX]) == 1;
            targetDistance = Double.parseDouble(tokens[TGT_DISTANCE_TOKEN_IDX]);
            targetAngle = Double.parseDouble(tokens[TGT_ANGLE_TOKEN_IDX]);
        } catch (Exception e) {
            DriverStation.reportError("Unhandled exception while parsing Vision packet: " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()), false);
            return -1;
        }
        return 0;
    }

    /**
     * This thread runs a periodic task in the background to listen for vision camera packets.
     */
    Thread packetListenerThread = new Thread(() -> {
        while(!Thread.interrupted()){
            backgroundUpdate();
        }
    });
}