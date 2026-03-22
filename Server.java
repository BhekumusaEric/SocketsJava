import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    // List to keep track of all connected clients
    private static ArrayList<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        // Use PORT from environment variable (common in cloud hosts) or default to 80
        String portStr = System.getenv("PORT");
        int port = (portStr != null) ? Integer.parseInt(portStr) : 80;

        // Bind to 0.0.0.0 to allow external connections in the cloud
        ServerSocket serverSocket = new ServerSocket(port, 50, java.net.InetAddress.getByName("0.0.0.0"));
        System.out.println("Social Connect Server running on port " + port);
        
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New connection from: " + socket.getInetAddress());
                
                // Create a handler for each client and run it in a new thread
                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } finally {
            serverSocket.close();
        }
    }

    // Send a message to all clients except the sender
    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    // Remove client from the list when they disconnect
    public static void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            
            // First message from client should be their username
            this.clientUsername = bufferedReader.readLine();
            Server.broadcast("SERVER: " + clientUsername + " has joined the chat!", this);
            System.out.println(clientUsername + " joined.");
        } catch (IOException e) {
            closeEverything();
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                if (messageFromClient == null) {
                    closeEverything();
                    break;
                }
                Server.broadcast(clientUsername + ": " + messageFromClient, this);
            } catch (IOException e) {
                closeEverything();
                break;
            }
        }
    }

    public void sendMessage(String message) {
        try {
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything();
        }
    }

    private void closeEverything() {
        Server.removeClient(this);
        Server.broadcast("SERVER: " + clientUsername + " has left the chat.", this);
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
