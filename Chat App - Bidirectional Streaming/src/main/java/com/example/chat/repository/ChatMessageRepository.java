package com.example.chat.repository;

import com.example.chat.entity.ChatMessageEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChatMessageRepository extends CassandraRepository<ChatMessageEntity, UUID> {
}
