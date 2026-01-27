package com.example.clientstreaming;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.example.clientstreaming.repository.UserRepository;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration,org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration",
    "spring.data.cassandra.repositories.enabled=false",
    "spring.main.allow-bean-definition-overriding=true"
})
class ClientStreamingApplicationTests {

    @TestConfiguration
    static class TestBeans {
        @Bean
        UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }
    }

    @Test
    void contextLoads() {
    }

}
