package com.example.project04mongodb_crud.utility;

import com.example.project04mongodb_crud.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User,String> {

}
