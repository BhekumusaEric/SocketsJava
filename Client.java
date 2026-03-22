import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    // ANSI Escape codes for "fun" colors
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.username = username;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            closeEverything();
        }
    }

    public void sendMessage() {
        try {
            // First send the username to the server
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                bufferedWriter.write(messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
                
                if (messageToSend.equalsIgnoreCase("BYE")) {
                    closeEverything();
                    break;
                }
            }
        } catch (IOException e) {
            closeEverything();
        }
    }

    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;
                while (socket.isConnected()) {
                    try {
                        msgFromGroupChat = bufferedReader.readLine();
                        if (msgFromGroupChat == null) {
                            closeEverything();
                            break;
                        }
                        // Style the incoming messages
                        if (msgFromGroupChat.startsWith("SERVER:")) {
                            System.out.println(ANSI_YELLOW + msgFromGroupChat + ANSI_RESET);
                        } else {
                            System.out.println(ANSI_CYAN + msgFromGroupChat + ANSI_RESET);
                        }
                    } catch (IOException e) {
                        closeEverything();
                        break;
                    }
                }
            }
        }).start();
    }

    public BufferedReader getBufferedReader() {
        return bufferedReader;
    }

    public BufferedWriter getBufferedWriter() {
        return bufferedWriter;
    }

    public void closeEverything() {
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println(ANSI_PURPLE + "--- WELCOME TO SOCIAL CONNECT ---" + ANSI_RESET);
        Scanner scanner = new Scanner(System.in);
        System.out.print(ANSI_GREEN + "Enter your username for the group chat: " + ANSI_RESET);
        String username = scanner.nextLine();
        
        Socket socket = new Socket("localhost", 80);
        Client client = new Client(socket, username);
        
        System.out.println(ANSI_GREEN + "Connected successfully! Type your messages below." + ANSI_RESET);
        
        client.listenForMessage();
        client.sendMessage();
    }
}