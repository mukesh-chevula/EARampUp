package com.example.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("chat_messages")
public class ChatMessageEntity {
    
    @PrimaryKey
    private UUID id;
    
    private String username;
    private String message;
    private Long timestamp;
    
    public ChatMessageEntity(String username, String message, Long timestamp) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
    }
}
