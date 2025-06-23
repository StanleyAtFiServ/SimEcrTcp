package org.fiserv;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            System.err.println("Usage: java POSAppTCPClient <xml-file-path>");
            System.err.println("Example: java POSAppTCPClient payment.xml");
            return;
        }

        String fileName = args[0];
        File xmlFile = new File(fileName);

        String ipAddress = args[1];
        String tcpPort = args[2];
        String comPort = args[3];

        if (!xmlFile.exists()) {
            System.err.println("Error: File not found at path: " + xmlFile.getAbsolutePath());
            return;
        }

        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            System.out.println("-------FiMsgX------");
            System.out.println("Select Option (0-2)");
            System.out.println("1. TCP Client");
            System.out.println("2. USB Client");
            System.out.println("0. Exit");

            while (!scanner.hasNextInt()) {
                System.out.println("Invalid input, Enter again");
                scanner.next();
            }
            choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.print("TCP Client");
                    TcpClient tcpClient = new TcpClient(fileName, ipAddress, tcpPort);
                    tcpClient.process();
                    break;
                case 2:
                    System.out.println("USB Client");
                    Rs232Client rs232Client = new Rs232Client(fileName, comPort);
                    rs232Client.process();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Invalid input, enter again");
            }
            System.out.println();
        } while (choice != 0);

        scanner.close();

    }
}