package com.example.chat.server;

import com.example.chat.grpc.ChatMessage;
import com.example.chat.service.ChatServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Slf4j
@Component
public class GrpcServer implements CommandLineRunner {

    @Value("${grpc.server.port:9090}")
    private int grpcPort;

    @Autowired
    private ChatServiceImpl chatService;

    @Override
    public void run(String... args) throws Exception {
        Server server = ServerBuilder.forPort(grpcPort)
            .addService(chatService)
            .build()
            .start();

        log.info("gRPC Server started on port: {}", grpcPort);
        log.info("Chat application is ready!");
        log.info("════════════════════════════════════════════════════════════");
        log.info("Server terminal ready. Type messages and press Enter to send");
        log.info("════════════════════════════════════════════════════════════");

        // Start a thread for terminal input
        startTerminalInputThread();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down gRPC server");
            server.shutdown();
        }));

        server.awaitTermination();
    }

    private void startTerminalInputThread() {
        new Thread(() -> {
            try {
                Scanner scanner = new Scanner(System.in);

                while (scanner.hasNextLine()) {
                    String input = scanner.nextLine();
                    if (input != null && !input.trim().isEmpty()) {
                        log.info("★ SERVER MESSAGE: '{}'", input);
                        
                        // Create the message
                        ChatMessage serverMessage = ChatMessage.newBuilder()
                            .setUsername("Server")
                            .setMessage(input)
                            .setTimestamp(System.currentTimeMillis())
                            .build();
                        
                        // Broadcast via the service
                        chatService.broadcastServerMessage(serverMessage);
                        
                        log.info("✓ Broadcasted to all connected clients");
                    }
                }
                scanner.close();
            } catch (Exception e) {
                log.error("Error in terminal input thread: {}", e.getMessage());
            }
        }).start();
    }
}
