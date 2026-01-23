package com.example.config;

import com.example.grpc.ScoreServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcServerConfig {

    @Value("${grpc.server.port:9091}")
    private int grpcPort;

    @Bean
    public Server grpcServer(ScoreServiceImpl service) throws Exception {
        Server server = ServerBuilder.forPort(grpcPort)
                .addService(service)
                .addService(ProtoReflectionService.newInstance())
                .build()
                .start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));

        return server;
    }
}
