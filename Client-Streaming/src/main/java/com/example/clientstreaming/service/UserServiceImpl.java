package com.example.clientstreaming.service;

import com.demo.grpc.Empty;
import com.demo.grpc.User;
import com.demo.grpc.UserField;
import com.demo.grpc.UserRequest;
import com.demo.grpc.UserResponse;
import com.demo.grpc.UserServiceGrpc;
import com.demo.grpc.Users;
import com.example.clientstreaming.entity.UserEntity;
import com.example.clientstreaming.repository.UserRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@GrpcService
public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public StreamObserver<UserField> createUser(StreamObserver<UserResponse> responseObserver) {
        Map<String, String> fields = new HashMap<>();
        return new StreamObserver<>() {
            @Override
            public void onNext(UserField value) {
                fields.put(value.getField(), value.getValue());
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                log.info("createUser collected fields: {}", fields);
                String id = fields.getOrDefault("id", UUID.randomUUID().toString());
                UserEntity entity = UserEntity.builder()
                        .id(id)
                        .firstname(fields.get("firstname"))
                        .lastname(fields.get("lastname"))
                        .email(fields.get("email"))
                        .phone(fields.get("phone"))
                        .build();
                userRepository.save(entity);
                responseObserver.onNext(UserResponse.newBuilder()
                        .setStatus("OK")
                        .setMessage("User created: " + id)
                        .build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<UserField> updateUser(StreamObserver<UserResponse> responseObserver) {
        Map<String, String> fields = new HashMap<>();
        return new StreamObserver<>() {
            @Override
            public void onNext(UserField value) {
                fields.put(value.getField(), value.getValue());
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                log.info("updateUser collected fields: {}", fields);
                String id = fields.get("id");
                if (id == null) {
                    responseObserver.onError(Status.INVALID_ARGUMENT
                            .withDescription("Missing id for update")
                            .asRuntimeException());
                    return;
                }

                Optional<UserEntity> existing = userRepository.findById(id);
                if (existing.isEmpty()) {
                    responseObserver.onNext(UserResponse.newBuilder()
                            .setStatus("NOT_FOUND")
                            .setMessage("User not found")
                            .build());
                    responseObserver.onCompleted();
                    return;
                }

                UserEntity entity = existing.get();
                if (fields.containsKey("firstname")) {
                    entity.setFirstname(fields.get("firstname"));
                }
                if (fields.containsKey("lastname")) {
                    entity.setLastname(fields.get("lastname"));
                }
                if (fields.containsKey("email")) {
                    entity.setEmail(fields.get("email"));
                }
                if (fields.containsKey("phone")) {
                    entity.setPhone(fields.get("phone"));
                }

                userRepository.save(entity);
                responseObserver.onNext(UserResponse.newBuilder()
                        .setStatus("OK")
                        .setMessage("User updated")
                        .build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<UserField> deleteUser(StreamObserver<UserResponse> responseObserver) {
        final String[] idHolder = {null};
        return new StreamObserver<>() {
            @Override
            public void onNext(UserField value) {
                idHolder[0] = value.getValue();
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                if (idHolder[0] == null) {
                    responseObserver.onError(Status.INVALID_ARGUMENT
                            .withDescription("Missing id for delete")
                            .asRuntimeException());
                    return;
                }
                userRepository.deleteById(idHolder[0]);
                responseObserver.onNext(UserResponse.newBuilder()
                        .setStatus("OK")
                        .setMessage("User deleted")
                        .build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void getUser(UserRequest request, StreamObserver<User> responseObserver) {
        userRepository.findById(request.getId())
                .ifPresentOrElse(entity -> {
                    responseObserver.onNext(mapToUser(entity));
                    responseObserver.onCompleted();
                }, () -> responseObserver.onError(Status.NOT_FOUND
                        .withDescription("User not found")
                        .asRuntimeException()));
    }

    @Override
    public void getAllUsers(Empty request, StreamObserver<Users> responseObserver) {
        Users.Builder builder = Users.newBuilder();
        userRepository.findAll().forEach(entity -> builder.addUsers(mapToUser(entity)));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    private User mapToUser(UserEntity entity) {
        return User.newBuilder()
                .setId(entity.getId())
                .setFirstname(nullToEmpty(entity.getFirstname()))
                .setLastname(nullToEmpty(entity.getLastname()))
                .setEmail(nullToEmpty(entity.getEmail()))
                .setPhone(nullToEmpty(entity.getPhone()))
                .build();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
