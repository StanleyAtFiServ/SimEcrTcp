package org.fiserv;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.*;

public class Rs232Client {
    private String fileName;
    private String comPort;

    public Rs232Client(String _comPort, String _fileName) {
        comPort = _comPort;
        fileName = _fileName;
    }

    public void process() {
        // Read XML content from file
        StringBuilder xmlBuilder = new StringBuilder();
        BufferedReader fileReader = null;
        try {
            fileReader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = fileReader.readLine()) != null) {
                xmlBuilder.append(line);
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + fileName);
            return;
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return;
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    System.err.println("Error closing file: " + e.getMessage());
                }
            }
        }

        String xmlMessage = xmlBuilder.toString();
        System.out.println("Loaded XML message:\n" + xmlMessage);

        SerialPort serialPort = null;
        InputStream in = null;
        OutputStream out = null;

        try {
            // Connect to RS232 serial port
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(comPort);
            CommPort commPort = portIdentifier.open("POSAppClient", 2000);

            if (!(commPort instanceof SerialPort)) {
                System.err.println("Error: Specified port is not a serial port - " + comPort);
                return;
            }

            serialPort = (SerialPort) commPort;
            serialPort.setSerialPortParams(
                    9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE
            );
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

            in = serialPort.getInputStream();
            out = serialPort.getOutputStream();

            System.out.println("Connected to RS232 device on " + comPort);

            // Send XML message
            out.write(xmlMessage.getBytes("UTF-8"));
            out.flush();

            // Receive acknowledgment
            int retries = 3;
            boolean success = false;
            byte[] buffer = new byte[1024];

            for (int i = 0; i < retries; i++) {
                try {
                    serialPort.enableReceiveTimeout(10000);
                    int bytesRead = in.read(buffer);
                    if (bytesRead > 0) {
                        String ackMessage = new String(buffer, 0, bytesRead, "UTF-8");
                        System.out.println("Received acknowledgment:\n" + ackMessage);
                        success = true;
                        break;
                    } else {
                        System.err.println("Attempt " + (i + 1) + ": No data received");
                    }
                } catch (IOException e) {
                    System.err.println("Attempt " + (i + 1) + ": " + e.getMessage());
                }
            }

            if (!success) {
                System.err.println("Failed to receive acknowledgment after " + retries + " attempts.");
            }

        } catch (Exception e) {
            System.err.println("RS232 communication error: " + e.toString());
        } finally {
            // Close resources
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (serialPort != null) serialPort.close();
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}