package org.fiserv;

import java.io.*;
import java.net.*;

public class TcpClient {

    private String fileName, ipAddress;
    private int targetPort;

    public TcpClient(String _fileName, String _ipAddress, String _targetPort) throws FileNotFoundException {
        fileName = _fileName; // Predefined text file name
        targetPort = Integer.parseInt(_targetPort);
        ipAddress = _ipAddress;
    }
    public void process() throws IOException {

        StringBuilder xmlBuilder = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        // Read XML content from file
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                xmlBuilder.append(line);
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + fileName);
            return;
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return;
        }

        String xmlMessage = xmlBuilder.toString();
        System.out.println("Loaded XML message:\n" + xmlMessage);

        Socket socket = new Socket(ipAddress, targetPort);
        System.out.println("Connected to forwarder.");

        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());

        byte[] xmlBytes = xmlMessage.getBytes("UTF-8");
        out.write(xmlBytes);
        out.flush();

        int retries = 3;
        boolean success = false;

        for (int i = 0; i < retries; i++) {
            try {
                socket.setSoTimeout(1000000);
                int ackLength = in.readInt();
                byte[] ackBytes = new byte[ackLength];
                in.readFully(ackBytes);
                String ackMessage = new String(ackBytes, "UTF-8");
                System.out.println("Received acknowledgment:\n" + ackMessage);
                success = true;
                break;
            } catch (SocketTimeoutException e) {
                System.err.println("Attempt " + (i + 1) + ": Timeout waiting for acknowledgment.");
            }
        }

        // Close resources
        out.close();
        in.close();
        socket.close();

        if (!success) {
            System.err.println("Failed to receive acknowledgment after " + retries + " attempts.");
        }
    }
}