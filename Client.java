import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    //static Socket socket = null; 
    static OutputStreamWriter  out = null;
    static InputStreamReader in = null;
    static BufferedReader bufferedReader = null;
    static BufferedWriter bufferedWriter = null;

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 80) ;
        out = new OutputStreamWriter(socket.getOutputStream());
        in = new InputStreamReader(socket.getInputStream());
        bufferedReader = new BufferedReader(in);
        bufferedWriter = new BufferedWriter(out);
        Scanner scaner = new Scanner(System.in);
        while (true){
            String msgToSend = scaner.nextLine();
            bufferedWriter.write(msgToSend);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            System.out.println("server :" +  bufferedReader.readLine());
            if (msgToSend.contains("Good Bye")) {
                break;
            };
        }
    }
}