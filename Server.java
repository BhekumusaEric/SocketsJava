import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {
            while (true) {
                Socket socket;
                System.out.println("Server running at port 80");
                socket = new ServerSocket(80).accept();
                System.out.println("Client connected from :" + socket.getInetAddress());
                InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

                while (true) {
                    String msgFrormClient = bufferedReader.readLine();
                    System.out.println("Client : " + msgFrormClient);
                    bufferedWriter.write("Message received ");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();

                    if (msgFrormClient.equalsIgnoreCase("BYE")) {
                        break;
                    }
                }
            }
        }
    }
