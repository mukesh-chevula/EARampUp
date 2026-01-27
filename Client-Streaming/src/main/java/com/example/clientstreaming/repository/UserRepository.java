package com.example.clientstreaming.repository;

import com.example.clientstreaming.entity.UserEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CassandraRepository<UserEntity, String> {
}
