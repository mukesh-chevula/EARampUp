package com.example.grpc.service;

import com.example.grpc.model.User;
import com.example.grpc.proto.*;
import com.example.grpc.repository.UserRepository;
import io.grpc.stub.StreamObserver;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.server.service.GrpcService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@GrpcService
public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            logger.info("CreateUser request received: firstName={}, lastName={}", request.getFirstName(), request.getLastName());
            
            User user = new User();
            user.setUserId(UUID.randomUUID().toString());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setCompany(request.getCompany());
            user.setAge(request.getAge());
            user.setPhoneNumber(request.getPhoneNumber());

            logger.info("Saving user with ID: {}", user.getUserId());
            User savedUser = userRepository.save(user);
            logger.info("User saved successfully with ID: {}", savedUser.getUserId());

            UserResponse response = UserResponse.newBuilder()
                    .setUser(convertToProtoUser(savedUser))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error in createUser: ", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asException());
        }
    }

    @Override
    public void updateUser(UpdateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            Optional<User> existingUser = userRepository.findById(request.getUserId());

            if (existingUser.isPresent()) {
                User user = existingUser.get();
                user.setFirstName(request.getFirstName());
                user.setLastName(request.getLastName());
                user.setCompany(request.getCompany());
                user.setAge(request.getAge());
                user.setPhoneNumber(request.getPhoneNumber());

                User updatedUser = userRepository.save(user);

                UserResponse response = UserResponse.newBuilder()
                        .setUser(convertToProtoUser(updatedUser))
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new RuntimeException("User not found with id: " + request.getUserId()));
            }
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void deleteUser(DeleteUserRequest request, StreamObserver<DeleteUserResponse> responseObserver) {
        try {
            Optional<User> existingUser = userRepository.findById(request.getUserId());

            if (existingUser.isPresent()) {
                userRepository.deleteById(request.getUserId());

                DeleteUserResponse response = DeleteUserResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("User deleted successfully")
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                DeleteUserResponse response = DeleteUserResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("User not found with id: " + request.getUserId())
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getUserById(GetUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            Optional<User> user = userRepository.findById(request.getUserId());

            if (user.isPresent()) {
                UserResponse response = UserResponse.newBuilder()
                        .setUser(convertToProtoUser(user.get()))
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new RuntimeException("User not found with id: " + request.getUserId()));
            }
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getAllUsers(GetAllUsersRequest request, StreamObserver<GetAllUsersResponse> responseObserver) {
        try {
            List<User> users = userRepository.findAll();

            GetAllUsersResponse.Builder responseBuilder = GetAllUsersResponse.newBuilder();
            
            for (User user : users) {
                responseBuilder.addUsers(convertToProtoUser(user));
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    private com.example.grpc.proto.User convertToProtoUser(User user) {
        return com.example.grpc.proto.User.newBuilder()
                .setUserId(user.getUserId())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setCompany(user.getCompany())
                .setAge(user.getAge())
                .setPhoneNumber(user.getPhoneNumber())
                .build();
    }
}
