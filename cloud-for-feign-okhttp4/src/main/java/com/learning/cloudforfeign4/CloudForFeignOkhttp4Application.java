package com.learning.cloudforfeign4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class CloudForFeignOkhttp4Application {

    public static void main(String[] args) {
        SpringApplication.run(CloudForFeignOkhttp4Application.class, args);
    }

}
