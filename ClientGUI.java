import javax.swing.*;
import java.awt.Color;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;

public class ClientGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private Client client;
    private String username;

    public ClientGUI() {
        showLoginScreen();
    }

    private void showLoginScreen() {
        JFrame loginFrame = new JFrame("Connect Social - Join");
        loginFrame.setSize(350, 200);
        loginFrame.setLayout(new GridLayout(4, 1, 10, 10));
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLocationRelativeTo(null);

        JPanel userPanel = new JPanel(new FlowLayout());
        userPanel.add(new JLabel("Username:"));
        JTextField userField = new JTextField(15);
        userPanel.add(userField);

        JPanel serverPanel = new JPanel(new FlowLayout());
        serverPanel.add(new JLabel("Server IP:"));
        JTextField serverField = new JTextField("localhost", 15);
        serverPanel.add(serverField);

        JButton loginButton = new JButton("Connect to Chat");
        
        loginButton.addActionListener(e -> {
            username = userField.getText();
            String serverAddress = serverField.getText();
            if (!username.isEmpty() && !serverAddress.isEmpty()) {
                loginFrame.dispose();
                initializeChatGUI();
                connectToServer(serverAddress);
            }
        });

        loginFrame.add(new JLabel("Welcome to Social Connect!", SwingConstants.CENTER));
        loginFrame.add(userPanel);
        loginFrame.add(serverPanel);
        loginFrame.add(loginButton);
        loginFrame.setVisible(true);
    }

    private void initializeChatGUI() {
        setTitle("Social Connect - " + username);
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Styling
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(33, 33, 33));
        chatArea.setForeground(Color.CYAN);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(chatArea);
        
        messageField = new JTextField();
        messageField.setBackground(new Color(50, 50, 50));
        messageField.setForeground(Color.WHITE);
        messageField.setCaretColor(Color.WHITE);

        sendButton = new JButton("Send");
        sendButton.setBackground(new Color(0, 150, 136));
        sendButton.setForeground(Color.WHITE);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        setVisible(true);
    }

    private void connectToServer(String host) {
        try {
            Socket socket = new Socket(host, 80);
            client = new Client(socket, username);
            
            // Override the message listener to update the GUI
            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = client.getBufferedReader().readLine()) != null) {
                        final String finalMsg = msg;
                        SwingUtilities.invokeLater(() -> chatArea.append(finalMsg + "\n"));
                    }
                } catch (IOException e) {
                    chatArea.append("SYSTEM: Connection lost.\n");
                }
            }).start();

            // Send initial username
            client.getBufferedWriter().write(username);
            client.getBufferedWriter().newLine();
            client.getBufferedWriter().flush();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not connect to server!");
            System.exit(0);
        }
    }

    private void sendMessage() {
        String msg = messageField.getText();
        if (!msg.isEmpty()) {
            try {
                client.getBufferedWriter().write(msg);
                client.getBufferedWriter().newLine();
                client.getBufferedWriter().flush();
                chatArea.append("Me: " + msg + "\n");
                messageField.setText("");
                
                if (msg.equalsIgnoreCase("BYE")) {
                    System.exit(0);
                }
            } catch (IOException e) {
                chatArea.append("SYSTEM: Failed to send message.\n");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}
