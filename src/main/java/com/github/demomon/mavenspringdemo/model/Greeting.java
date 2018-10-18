package com.github.demomon.mavenspringdemo.model;

import org.springframework.data.annotation.Id;

public class Greeting {

    @Id
    private String id;

    private String greeting;

    public String getId() {
        return id;
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }
}
