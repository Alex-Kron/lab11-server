import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Arrays;
import java.util.logging.Logger;

public class UDPServer {
    private static int port;
    private static int clientPort;
    private static String name = "SERVER";
    private static Logger log = Logger.getLogger(UDPServer.class.getName());
    private static DatagramSocket serverSocket;
    static {
        try {
            serverSocket = new DatagramSocket();
            port = serverSocket.getPort();
            serverSocket.setSoTimeout(500);
        } catch (SocketException e) {
            log.info(e.getMessage());
        }
    }


    public static void main(String[] args) throws Exception {
        byte[] receiveData = new byte[1024];
        showPorts();

        Thread sendListener = new Thread(() -> {
            showCommand();
            String sentence = "";
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            try {
                while (quit(sentence)) {
                    sentence = inFromUser.readLine();
                    setPort(sentence);
                    setClientPort(sentence);
                    setName(sentence);
                    send(sentence, serverSocket);
                }
                inFromUser.close();
            } catch (IOException e) {
                log.info(e.getMessage());
            }
        });

        Thread receiveListener = new Thread(() -> {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            while (sendListener.isAlive()) {
                try {
                    serverSocket.receive(receivePacket);
                    byte[] message = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                    String sentence = new String(message);
                    clientPort = receivePacket.getPort();
                    System.out.println(sentence);
                } catch (SocketTimeoutException e) {

                } catch (IOException e) {
                    log.info(e.getMessage());
                }
            }
        });

        receiveListener.start();
        sendListener.start();
        receiveListener.join();
        sendListener.join();
    }


    private static boolean quit(String command) {
        String quit = "@quit";
        return !command.equals(quit);
    }

    private static void setName(String command) {
        String[] words = command.split("\\s");
        if (words.length == 2 && words[0].equals("@name")) {
            name = words[1];
            log.info("Name is change.\nNew name = " + name);
        }
    }

    private static void setPort(String command) throws SocketException {
        String[] words = command.split("\\s");
        if (words.length == 2 && words[0].equals("@port")) {
            port = Integer.parseInt(words[1]);
            log.info("Port is change.\nNew port = " + port);
            serverSocket = new DatagramSocket(port);
            serverSocket.setSoTimeout(500);
        }
    }

    private static void setClientPort(String command) throws SocketException {
        String[] words = command.split("\\s");
        if (words.length == 2 && words[0].equals("@clientPort")) {
            clientPort = Integer.parseInt(words[1]);
            log.info("Client port is change.\nNew port = " + clientPort);
        }
    }

    private static void send(String message, DatagramSocket serverSocket) throws IOException {
        String[] words = message.split("\\s");
        if (words[0].equals("@send")) {
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            sb.append(": ");
            for (int i = 1; i < words.length; i++) {
                sb.append(words[i]);
                sb.append(" ");
            }
            message = sb.toString();
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, new InetSocketAddress(clientPort));
            serverSocket.send(packet);
        }
    }

    private static void showPorts() {
        String s = "Server on port: " +  port;
        log.info(s);
    }

    private static void showCommand() {
        String s = "Server command list:\n@send - send messege;\n@port - set port;\n@clientPort - set client port;\n@name - set server name;\n@quit - quite the server.";
        log.info(s);
    }
}

