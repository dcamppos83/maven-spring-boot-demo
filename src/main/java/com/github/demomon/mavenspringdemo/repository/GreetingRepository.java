package com.github.demomon.mavenspringdemo.repository;

import com.github.demomon.mavenspringdemo.model.Greeting;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "greetings", path = "greetings")
public interface GreetingRepository extends MongoRepository<Greeting, String> {

}
