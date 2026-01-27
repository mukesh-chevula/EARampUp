package com.example.chat.service;

import com.example.chat.entity.ChatMessageEntity;
import com.example.chat.grpc.ChatMessage;
import com.example.chat.grpc.ChatServiceGrpc;
import com.example.chat.repository.ChatMessageRepository;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class ChatServiceImpl extends ChatServiceGrpc.ChatServiceImplBase {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private final List<StreamObserver<ChatMessage>> observers = new CopyOnWriteArrayList<>();

    @Override
    public StreamObserver<ChatMessage> chat(StreamObserver<ChatMessage> responseObserver) {
        log.info("New client connected for bidirectional streaming");
        
        // Add this observer to the list of active observers
        observers.add(responseObserver);
        log.info("Total connected clients: {}", observers.size());

        return new StreamObserver<ChatMessage>() {
            @Override
            public void onNext(ChatMessage message) {
                log.info("Received message from {}: {}", message.getUsername(), message.getMessage());
                
                // Save to Cassandra
                ChatMessageEntity entity = new ChatMessageEntity(
                    message.getUsername(),
                    message.getMessage(),
                    message.getTimestamp()
                );
                chatMessageRepository.save(entity);
                
                // Broadcast to all connected clients
                for (StreamObserver<ChatMessage> observer : observers) {
                    try {
                        observer.onNext(message);
                        log.debug("Message broadcasted to a client");
                    } catch (Exception e) {
                        log.error("Error sending message to client", e);
                        observers.remove(observer);
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("Error in chat stream", t);
                observers.remove(responseObserver);
                log.info("Client disconnected. Total clients: {}", observers.size());
            }

            @Override
            public void onCompleted() {
                log.info("Client disconnected gracefully");
                observers.remove(responseObserver);
                log.info("Total connected clients: {}", observers.size());
                responseObserver.onCompleted();
            }
        };
    }

    public void broadcastServerMessage(ChatMessage serverMessage) {
        if (observers.isEmpty()) {
            log.warn("No connected clients to broadcast to");
            return;
        }

        // Save to Cassandra
        ChatMessageEntity entity = new ChatMessageEntity(
            serverMessage.getUsername(),
            serverMessage.getMessage(),
            serverMessage.getTimestamp()
        );
        chatMessageRepository.save(entity);
        log.debug("Server message saved to Cassandra");

        // Broadcast to all connected clients
        int clientCount = 0;
        log.info("Broadcasting server message to {} observers", observers.size());
        for (StreamObserver<ChatMessage> observer : observers) {
            try {
                observer.onNext(serverMessage);
                clientCount++;
                log.info("✓ Server message sent to client #{}", clientCount);
            } catch (Exception e) {
                log.error("✗ Error sending message to client: {}", e.getMessage());
                observers.remove(observer);
            }
        }

        log.info("★ Server message broadcasted to {} client(s)", clientCount);
    }
}
