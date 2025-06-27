package org.fiserv;

import java.io.*;
import java.net.*;

public class TcpIpClient {

    private String fileName, ipAddress;
    private int targetPort;

    public TcpIpClient(String _fileName, String _ipAddress, String _targetPort) {
        fileName = _fileName;
        targetPort = Integer.parseInt(_targetPort);
        ipAddress = _ipAddress;
    }

    private int byteArrayToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8)  |
                (bytes[3] & 0xFF);
    }

    public void process() {
        StringBuilder xmlBuilder = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fileName));
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
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Error closing file reader: " + e.getMessage());
                }
            }
        }

        String xmlMessage = xmlBuilder.toString();
        System.out.println("Loaded XML message:\n" + xmlMessage);
        byte[] xmlBytes;
        try {
            xmlBytes = xmlMessage.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.err.println("UTF-8 encoding not supported");
            return;
        }

        Socket socket = null;
        DataOutputStream out = null;
        DataInputStream in = null;

        try {
            socket = new Socket(ipAddress, targetPort);
            System.out.println("Connected to forwarder.");
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            while (true) {
                // Send XML message
                try {
                    out.write(xmlBytes);
                    out.flush();
                    System.out.println("XML message sent.");
                } catch (IOException e) {
                    System.err.println("Error sending message: " + e.getMessage());
                    break;
                }

                int retries = 3;
                boolean success = false;

                for (int i = 0; i < retries; i++) {
                    long deadline = System.currentTimeMillis() + 1000000;
                    try {
                        // Read 4-byte length field
                        /*
                        byte[] lengthBytes = new byte[4];
                        int lengthBytesRead = 0;
                        while (lengthBytesRead < 4) {
                            if (System.currentTimeMillis() >= deadline) {
                                throw new SocketTimeoutException("Timeout waiting for acknowledgment length");
                            }
                            if (in.available() > 0) {
                                int count = in.read(lengthBytes, lengthBytesRead, 4 - lengthBytesRead);
                                if (count == -1) {
                                    throw new EOFException("End of stream reached");
                                }
                                lengthBytesRead += count;
                            } else {
                                Thread.sleep(100);
                            }
                        }
                        int ackLength = byteArrayToInt(lengthBytes);

                        // Read acknowledgment payload
                        byte[] ackBytes = new byte[ackLength];
                        int payloadBytesRead = 0;

                        while (payloadBytesRead < ackLength) {
                            if (System.currentTimeMillis() >= deadline) {
                                throw new SocketTimeoutException("Timeout waiting for acknowledgment payload");
                            }

                            if (in.available() > 0) {
                                int count = in.read(ackBytes, payloadBytesRead, ackLength - payloadBytesRead);
                                if (count == -1) {
                                    throw new EOFException("End of stream reached");
                                }
                                payloadBytesRead += count;
                            } else {
                                Thread.sleep(100);
                            }
                        }
                        */

                        byte[] bytesMessage = new byte[19200];
                        int bytesRead;
                        bytesRead = in.read(bytesMessage);

                        String message = new String(bytesMessage, "UTF-8");
                        System.out.println("Received acknowledgment: \n" + message);

                        bytesRead = in.read(bytesMessage);
                        message = new String(bytesMessage, "UTF-8");
                        System.out.println("Received Message: \n" + message);
                        success = true;

                        break;
                    } catch (SocketTimeoutException e) {
                        System.err.println("Attempt " + (i + 1) + ": " + e.getMessage());
 //                   } catch (InterruptedException e) {
   //                     Thread.currentThread().interrupt();
     //                   System.err.println("Thread interrupted during sleep");
       //                 break;
                    } catch (EOFException e) {
                        System.err.println("Connection closed by server: " + e.getMessage());
                        break;
                    }
                }

                if (!success) {
                    System.err.println("Failed to receive acknowledgment after " + retries + " attempts.");
                    break;
                }
                System.out.println("Press Enter to resend message");
                new BufferedReader(new InputStreamReader(System.in)).readLine();

                System.out.println("Sent Again");
            }
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        } finally {
            // Close resources in reverse order
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    System.err.println("Error closing output stream: " + e.getMessage());
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    System.err.println("Error closing input stream: " + e.getMessage());
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            }
        }
    }
}