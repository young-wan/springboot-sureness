package com.young.sureness.sureness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class SurenessApplication {

    public static void main(String[] args) {
        SpringApplication.run(SurenessApplication.class, args);
    }

}
