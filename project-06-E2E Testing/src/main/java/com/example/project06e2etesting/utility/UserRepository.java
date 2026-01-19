package com.example.project06e2etesting.utility;

import com.example.project06e2etesting.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User,String> {

}
