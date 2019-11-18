import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Arrays;
import java.util.logging.Logger;

class UDPClient {
    private static int port;
    private static int serverPort;
    private static String name = "CLIENT";
    private static InetAddress ipAddress;
    private static Logger log = Logger.getLogger(UDPClient.class.getName());

    static {
        try {
            ipAddress = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            log.info(e.getMessage());
        }
    }

    private static DatagramSocket clientSocket;
    static {
        try {
            clientSocket = new DatagramSocket();
            port = clientSocket.getPort();
            clientSocket.setSoTimeout(500);
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
                    setServerPort(sentence);
                    setPort(sentence);
                    setName(sentence);
                    send(sentence, clientSocket);
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
                    clientSocket.receive(receivePacket);
                    byte[] message = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                    String sentence = new String(message);
                    serverPort = receivePacket.getPort();
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
            clientSocket = new DatagramSocket(port);
            clientSocket.setSoTimeout(500);
        }
    }

    private static void setServerPort(String command) throws SocketException {
        String[] words = command.split("\\s");
        if (words.length == 2 && words[0].equals("@serverPort")) {
            serverPort = Integer.parseInt(words[1]);
            log.info("Server port is change.\nNew port = " + serverPort);
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
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, ipAddress, serverPort);
            serverSocket.send(packet);
        }
    }

    private static void showPorts() {
        String s = "Client on port: " +  port;
        log.info(s);
    }

    private static void showCommand() {
        String s = "Client command list:\n@send - send message;\n@port - set port;\n@serverPort - set server port;\n@name - set server name;\n@quit - quite the server.";
        log.info(s);
    }
}
