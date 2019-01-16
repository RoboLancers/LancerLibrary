package com.robolancers.lib.vision;

import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoMode.PixelFormat;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.Timer;

import java.util.Arrays;

@SuppressWarnings({"FieldCanBeLocal", "unused", "WeakerAccess", "UnusedReturnValue", "SameParameterValue"})
public class JeVois {
    // Serial Port Constants
    private static final int BAUD_RATE = 115200;

    // MJPG Streaming Constants
    private static final int MJPG_STREAM_PORT = 1180;

    // Packet format constants
    private static final String PACKET_START_CHAR = "{";
    private static final String PACKET_END_CHAR = "}";
    private static final String PACKET_DILEM_CHAR = ",";
    private static final int PACKET_NUM_EXPECTED_FIELDS = 3;

    // Configure the camera to stream debug images or not.
    private boolean broadcastUSBCam = false;

    // When not streaming, use this mapping
    private static final int NO_STREAM_MAPPING = 1;

    // When streaming, use this set of configuration
    private static final int STREAM_WIDTH_PX = 320;
    private static final int STREAM_HEIGHT_PX = 240;
    private static final int STREAM_RATE_FPS = 30;

    // Serial port used for getting target data from JeVois
    private SerialPort visionPort = null;

    // USBCam and server used for broadcasting a webstream of what is seen
    private UsbCamera visionCam;
    private MjpegServer camServer;

    // Status variables
    private boolean dataStreamRunning;
    private boolean camStreamRunning;
    private boolean visionOnline;

    // Packet rate performance tracking
    private double packetRxTime = 0;
    private double prevPacketRxTime = 0;
    private double packetRate_PPS = 0;

    // Most recently seen target information
    private boolean targetVisible;
    private double targetDistance;
    private double targetAngle;

    // Info about the JeVois performance & status
    private double jeVoisCpuTempC;
    private double jeVoisCpuLoadPct;
    private double jeVoisFramerateFPS;
    private double packetRxRatePPS;

    /**
     * Constructor (simple). Opens a USB serial port to the JeVois camera, sends a few test commands checking for error,
     * then fires up the user's program and begins listening for target info packets in the background
     */
    public JeVois() {
        this(false); //Default - stream disabled, just run serial.
    }

    /**
     * Constructor (more complex). Opens a USB serial port to the JeVois camera, sends a few test commands checking for error,
     * then fires up the user's program and begins listening for target info packets in the background.
     * Pass TRUE to additionaly enable a USB camera stream of what the vision camera is seeing.
     */
    public JeVois(boolean useUSBStream) {
        int retry_counter = 0;

        //Retry strategy to get this serial port open.
        //I have yet to see a single retry used assuming the camera is plugged in
        // but you never know.
        while(visionPort == null && retry_counter++ < 10){
            try {
                System.out.print("Creating JeVois SerialPort...");
                visionPort = new SerialPort(BAUD_RATE,SerialPort.Port.kUSB);
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

        //Ensure the JeVois is starting with the stream off.
        stopDataOnlyStream();

        setCameraStreamActive(useUSBStream);
        start();

        //Start listening for packets
        packetListenerThread.setDaemon(true);
        packetListenerThread.start();
    }

    public void start(){
        if(broadcastUSBCam){
            //Start streaming the JeVois via webcam
            //This auto-starts the serial stream
            startCameraStream();
        } else {
            startDataOnlyStream();
        }
    }

    public void stop(){
        if(broadcastUSBCam){
            //Start streaming the JeVois via webcam
            //This auto-starts the serial stream
            stopCameraStream();
        } else {
            stopDataOnlyStream();
        }
    }

    /**
     * Send commands to the JeVois to configure it for image-processing friendly parameters
     */
    public void setCamVisionProcMode() {
        if (visionPort != null){
            sendCmdAndCheck("setcam autoexp 1"); //Disable auto exposure
            sendCmdAndCheck("setcam absexp 25"); //Force exposure to a low value for vision processing
        }
    }

    /**
     * Send parameters to the camera to configure it for a human-readable image
     */
    public void setCamHumanDriverMode() {
        if (visionPort != null){
            sendCmdAndCheck("setcam autoexp 0"); //Enable AutoExposure
        }
    }

    /*
     * Main getters/setters
     */

    /**
     * Set to true to enable the camera stream, or set to false to stream serial-packets only.
     * Note this cannot be changed at runtime due to jevois constraints. You must stop whatever processing
     * is going on first.
     */
    public void setCameraStreamActive(boolean active){
        if(!dataStreamRunning){
            broadcastUSBCam = active;
        } else {
            DriverStation.reportError("Attempt to change cal stream mode while JeVois is still running. This is disallowed.", false);
        }
    }

    /**
     * Returns the distance the target is away from the camera
     */
    public double getTargetDistance() {
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

    /**
     * Returns the JeVois's most recently reported CPU Temperature in deg C
     */
    public double getJeVoisCPUTemp_C(){
        return jeVoisCpuTempC;
    }

    /**
     * Returns the JeVois's most recently reported CPU Load in percent of max
     */
    public double getJeVoisCpuLoad_pct(){
        return jeVoisCpuLoadPct;
    }

    /**
     * Returns the JeVois's most recently reported pipeline framerate in Frames per second
     */
    public double getJeVoisFramerate_FPS(){
        return jeVoisFramerateFPS;
    }

    /**
     * Returns the roboRIO measured serial packet receive rate in packets per second
     */
    public int getPacketRxRate_PPS(){
        if(visionOnline){
            return (int)Math.round(packetRxRatePPS);
        } else {
            return 0;
        }
    }


    /**
     * This is the main periodic update function for the Listener. It is intended
     * to be run in a background task, as it will block until it gets packets.
     */
    private void backgroundUpdate(){
        // Grab packets and parse them.
        String packet;

        prevPacketRxTime = packetRxTime;

        sendCmd("target");
        packet = blockAndGetPacket(2.0);

        if(packet != null){
            packetRxTime = Timer.getFPGATimestamp();
            if(parsePacket(packet, packetRxTime) == 0){
                visionOnline = true;
                packetRxRatePPS = 1.0/(packetRxTime - prevPacketRxTime);
            } else {
                visionOnline = false;
            }
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

    private void startDataOnlyStream(){
        //Send serial commands to start the streaming of target info
        sendCmdAndCheck("setmapping " + NO_STREAM_MAPPING);
        sendCmdAndCheck("streamon");
        dataStreamRunning = true;
    }

    private void stopDataOnlyStream(){
        //Send serial commands to stop the streaming of target info
        sendCmdAndCheck("streamoff");
        dataStreamRunning = false;
    }


    /**
     * Open an Mjpeg streamer from the JeVois camera
     */
    private void startCameraStream(){
        try{
            System.out.print("Starting JeVois Cam Stream...");
            visionCam = new UsbCamera("VisionProcCam", 0);
            visionCam.setVideoMode(PixelFormat.kYUYV, STREAM_WIDTH_PX, STREAM_HEIGHT_PX, STREAM_RATE_FPS);
            camServer = new MjpegServer("VisionCamServer", MJPG_STREAM_PORT);
            camServer.setSource(visionCam);
            camStreamRunning = true;
            dataStreamRunning = true;
            System.out.println("SUCCESS!!");
        } catch (Exception e) {
            DriverStation.reportError("Cannot start camera stream from JeVois", false);
            e.printStackTrace();
        }
    }

    /**
     * Cease the operation of the camera stream. Unknown if needed.
     */
    private void stopCameraStream(){
        if(camStreamRunning){
            camServer.close();
            visionCam.close();
            camStreamRunning = false;
            dataStreamRunning = false;
        }
    }

    /**
     * Sends a command over serial to JeVois and returns immediately.
     * @param cmd String of the command to send (ex: "ping")
     * @return number of bytes written
     */
    private int sendCmd(String cmd){
        int bytes = visionPort.writeString(cmd + "\n");
        System.out.println("wrote " +  bytes + "/" + (cmd.length()+1) + " bytes, cmd: " + cmd);
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
    public int parsePacket(String pkt, double rx_Time){
        //Parsing constants. These must be aligned with JeVois code.
        final int NUM_EXPECTED_TOKENS = 3;
        final int TGT_VISIBLE_TOKEN_IDX = 0;
        final int TGT_DISTANCE_TOKEN_IDX = 1;
        final int TGT_ANGLE_TOKEN_IDX = 2;

        //Split string into many substrings, presuming those strings are separated by commas
        String[] tokens = pkt.split(",");

        //Check there were enough substrings found
        if(tokens.length < NUM_EXPECTED_TOKENS){
            DriverStation.reportError("Got malformed vision packet. Expected 8 tokens, but only found " + tokens.length + ". Packet Contents: " + pkt, false);
            return -1;
        }

        //Convert each string into the proper internal value
        try {
            //Boolean values should only have T or F characters
            if(tokens[TGT_VISIBLE_TOKEN_IDX].equals("F")){
                targetVisible = false;
            } else if (tokens[TGT_VISIBLE_TOKEN_IDX].equals("T")) {
                targetVisible = true;
            } else {
                DriverStation.reportError("Got malformed vision packet. Expected only T or F in " + TGT_VISIBLE_TOKEN_IDX + ", but got " + tokens[TGT_VISIBLE_TOKEN_IDX], false);
                return -1;
            }

            //Use Java built-in double to string conversion on most of the rest
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