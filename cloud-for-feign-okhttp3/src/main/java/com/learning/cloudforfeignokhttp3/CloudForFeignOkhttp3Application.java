package com.learning.cloudforfeignokhttp3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class CloudForFeignOkhttp3Application {

    public static void main(String[] args) {
        SpringApplication.run(CloudForFeignOkhttp3Application.class, args);
    }

}
