package com.example.chat.client;

import com.example.chat.grpc.ChatMessage;
import com.example.chat.grpc.ChatServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Slf4j
public class ChatClient {
    
    private final JFrame frame;
    private final JTextArea messageHistoryArea;
    private final JTextField messageInputField;
    private final JButton sendButton;
    private final JTextField usernameField;
    
    private ManagedChannel channel;
    private StreamObserver<ChatMessage> requestObserver;
    private String username;

    public ChatClient() {
        // Create main frame
        frame = new JFrame("gRPC Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setLayout(new BorderLayout(10, 10));

        // Username panel
        JPanel usernamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        usernamePanel.add(new JLabel("Username:"));
        usernameField = new JTextField(15);
        usernameField.setText("User" + (int)(Math.random() * 1000));
        usernamePanel.add(usernameField);
        JButton connectButton = new JButton("Connect");
        usernamePanel.add(connectButton);
        
        // Message history area
        messageHistoryArea = new JTextArea();
        messageHistoryArea.setEditable(false);
        messageHistoryArea.setLineWrap(true);
        messageHistoryArea.setWrapStyleWord(true);
        messageHistoryArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(messageHistoryArea);
        
        // Message input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        messageInputField = new JTextField();
        messageInputField.setFont(new Font("Arial", Font.PLAIN, 14));
        messageInputField.setEnabled(false);
        sendButton = new JButton("Send");
        sendButton.setEnabled(false);
        inputPanel.add(messageInputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        // Add components to frame
        frame.add(usernamePanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);
        
        // Add padding
        ((JPanel)frame.getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Connect button action
        connectButton.addActionListener(e -> {
            username = usernameField.getText().trim();
            if (!username.isEmpty()) {
                connectToServer();
                usernameField.setEnabled(false);
                connectButton.setEnabled(false);
                messageInputField.setEnabled(true);
                sendButton.setEnabled(true);
                messageInputField.requestFocus();
            } else {
                JOptionPane.showMessageDialog(frame, "Please enter a username", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Send button action
        sendButton.addActionListener(e -> sendMessage());
        
        // Enter key action
        messageInputField.addActionListener(e -> sendMessage());
        
        // Window closing event
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnect();
            }
        });
        
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void connectToServer() {
        try {
            channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();
            
            ChatServiceGrpc.ChatServiceStub asyncStub = ChatServiceGrpc.newStub(channel);
            
            // Create response observer to handle incoming messages
            StreamObserver<ChatMessage> responseObserver = new StreamObserver<ChatMessage>() {
                @Override
                public void onNext(ChatMessage message) {
                    SwingUtilities.invokeLater(() -> {
                        String displayMessage = String.format("[%s]: %s\n", 
                            message.getUsername(), 
                            message.getMessage());
                        messageHistoryArea.append(displayMessage);
                        messageHistoryArea.setCaretPosition(messageHistoryArea.getDocument().getLength());
                    });
                }

                @Override
                public void onError(Throwable t) {
                    log.error("Error in response stream", t);
                    SwingUtilities.invokeLater(() -> {
                        messageHistoryArea.append("ERROR: Connection lost!\n");
                        JOptionPane.showMessageDialog(frame, "Connection error: " + t.getMessage(), 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    });
                }

                @Override
                public void onCompleted() {
                    log.info("Server completed the stream");
                    SwingUtilities.invokeLater(() -> {
                        messageHistoryArea.append("Server closed the connection.\n");
                    });
                }
            };
            
            // Start bidirectional streaming
            requestObserver = asyncStub.chat(responseObserver);
            
            messageHistoryArea.append("Connected to chat server!\n");
            messageHistoryArea.append("You can now send messages.\n\n");
            
        } catch (Exception e) {
            log.error("Failed to connect to server", e);
            JOptionPane.showMessageDialog(frame, 
                "Failed to connect to server: " + e.getMessage(), 
                "Connection Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendMessage() {
        String messageText = messageInputField.getText().trim();
        if (!messageText.isEmpty() && requestObserver != null) {
            ChatMessage message = ChatMessage.newBuilder()
                .setUsername(username)
                .setMessage(messageText)
                .setTimestamp(System.currentTimeMillis())
                .build();
            
            try {
                requestObserver.onNext(message);
                messageInputField.setText("");
            } catch (Exception e) {
                log.error("Failed to send message", e);
                messageHistoryArea.append("ERROR: Failed to send message!\n");
            }
        }
    }

    private void disconnect() {
        if (requestObserver != null) {
            try {
                requestObserver.onCompleted();
            } catch (Exception e) {
                log.error("Error completing request stream", e);
            }
        }
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClient::new);
    }
}
